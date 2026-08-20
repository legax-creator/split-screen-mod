package com.dogmod.event;

import com.dogmod.DogMod;
import com.dogmod.capability.DogData;
import com.dogmod.capability.DogDataManager;
import com.dogmod.capability.DogOriginChecker;
import com.dogmod.disguise.DogDisguise;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.block.FenceBlock;
import net.minecraft.block.FenceGateBlock;
import net.minecraft.block.WallBlock;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.entity.player.HungerManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;
import java.util.Set;

public class DogEventHandler {

    private static final int TELEPORT_CHECK_INTERVAL = 40;
    private static final double TELEPORT_DISTANCE = 20.0;
    private static final double LEASH_MAX_DISTANCE = 7.0;
    private static final double BREED_DISTANCE = 3.0; // Çiftleşme mesafesi

    private static final Set<net.minecraft.item.Item> MEAT_ITEMS = Set.of(
        Items.BEEF, Items.COOKED_BEEF,
        Items.PORKCHOP, Items.COOKED_PORKCHOP,
        Items.MUTTON, Items.COOKED_MUTTON,
        Items.CHICKEN, Items.COOKED_CHICKEN,
        Items.RABBIT, Items.COOKED_RABBIT
    );

    private static int tickCounter = 0;

    public static void register() {
        UseEntityCallback.EVENT.register(DogEventHandler::onUseEntity);
        UseBlockCallback.EVENT.register(DogEventHandler::onUseBlock);
        ServerTickEvents.END_SERVER_TICK.register(DogEventHandler::onServerTick);
        ServerLivingEntityEvents.AFTER_DEATH.register(DogEventHandler::onPlayerDeath);

        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            ServerPlayerEntity player = handler.getPlayer();
            DogDisguise.remove(player);
            DogDataManager.remove(player.getUuid());
        });

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayerEntity player = handler.getPlayer();
            if (DogOriginChecker.isDogPlayer(player)) DogDisguise.apply(player);
        });

        DogMod.LOGGER.info("DogEventHandler kayıt edildi");
    }

    private static ActionResult onUseEntity(PlayerEntity player, World world, Hand hand,
            net.minecraft.entity.Entity entity, EntityHitResult hitResult) {
        if (world.isClient) return ActionResult.PASS;
        if (!(player instanceof ServerPlayerEntity interactor)) return ActionResult.PASS;
        if (!(entity instanceof ServerPlayerEntity target)) return ActionResult.PASS;
        if (!DogOriginChecker.isDogPlayer(target)) return ActionResult.PASS;

        ItemStack held = interactor.getStackInHand(hand);
        DogData data = DogDataManager.get(target);

        if (!data.isTamed() && held.isOf(Items.BONE))
            return tame(interactor, target, data, held);

        if (!data.isTamed() || !data.isOwner(interactor.getUuid()))
            return ActionResult.PASS;

        if (held.isOf(Items.LEAD)) return toggleLeash(interactor, target, data, held);
        if (interactor.isSneaking()) return toggleSit(interactor, target, data);

        if (MEAT_ITEMS.contains(held.getItem())) {
            // Aşk modu - açlık doluysa ve cooldown bittiyse
            if (data.isTamed() && data.canBreed()
                    && target.getHungerManager().getFoodLevel() >= 18) {
                return enterLoveMode(interactor, target, data, held);
            }
            // Normal besleme - açlık dolmamışsa
            if (target.getHungerManager().getFoodLevel() < 20) {
                return feed(interactor, target, data, held);
            }
        }

        return ActionResult.PASS;
    }

    private static ActionResult onUseBlock(PlayerEntity player, World world, Hand hand,
            BlockHitResult hitResult) {
        if (world.isClient) return ActionResult.PASS;
        if (!(player instanceof ServerPlayerEntity interactor)) return ActionResult.PASS;
        if (!interactor.getStackInHand(hand).isOf(Items.LEAD)) return ActionResult.PASS;

        for (ServerPlayerEntity dog : world.getServer().getPlayerManager().getPlayerList()) {
            if (!DogOriginChecker.isDogPlayer(dog)) continue;
            DogData data = DogDataManager.get(dog);
            if (!data.isTamed() || !data.isOwner(interactor.getUuid())) continue;
            if (!data.isLeashed()) continue;

            BlockPos pos = hitResult.getBlockPos();
            net.minecraft.block.BlockState state = world.getBlockState(pos);
            boolean isFence = state.getBlock() instanceof FenceBlock
                || state.getBlock() instanceof FenceGateBlock
                || state.getBlock() instanceof WallBlock;

            if (isFence) {
                data.setLeashAnchor(pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5);
                data.setLeashFixed(true);
                if (!interactor.isCreative())
                    interactor.getStackInHand(hand).decrement(1);
                world.playSoundAtBlockCenter(pos, SoundEvents.ENTITY_LEASH_KNOT_PLACE,
                    SoundCategory.BLOCKS, 1.0f, 1.0f, false);
                dog.sendMessage(Text.literal("§7Bir direğe bağlandın."), true);
                interactor.sendMessage(Text.literal("§7" + dog.getName().getString()
                    + " direğe bağlandı."), false);
                return ActionResult.SUCCESS;
            }
        }
        return ActionResult.PASS;
    }

    // ── EVCİLLEŞTİRME ────────────────────────────────────────────────────────

    private static ActionResult tame(ServerPlayerEntity owner, ServerPlayerEntity dog,
            DogData data, ItemStack bone) {
        if (!owner.isCreative()) bone.decrement(1);
        if (Math.random() < 0.5) {
            data.setTamed(true);
            data.setOwnerUUID(owner.getUuid());
            playSound(dog, SoundEvents.ENTITY_WOLF_AMBIENT, 1.5f);
            dog.sendMessage(Text.literal("§a❤ " + owner.getName().getString()
                + " seni evcilleştirdi!"), false);
            owner.sendMessage(Text.literal("§a❤ " + dog.getName().getString()
                + " artık senin köpeğin!"), false);
            DogDisguise.refresh(dog);
        } else {
            playSound(dog, SoundEvents.ENTITY_WOLF_HURT, 1.0f);
            owner.sendMessage(Text.literal("§c" + dog.getName().getString()
                + " kemikle ilgilenmedi..."), false);
        }
        return ActionResult.SUCCESS;
    }

    // ── OTURMA ───────────────────────────────────────────────────────────────

    private static ActionResult toggleSit(ServerPlayerEntity owner, ServerPlayerEntity dog,
            DogData data) {
        boolean nowSitting = !data.isSitting();
        data.setSitting(nowSitting);
        if (nowSitting) {
            playSound(dog, SoundEvents.ENTITY_WOLF_WHINE, 1.0f);
            dog.sendMessage(Text.literal("§7Oturdun."), true);
            owner.sendMessage(Text.literal("§7" + dog.getName().getString() + " oturdu."), false);
        } else {
            playSound(dog, SoundEvents.ENTITY_WOLF_AMBIENT, 1.0f);
            dog.sendMessage(Text.literal("§7Kalktın!"), true);
            owner.sendMessage(Text.literal("§7" + dog.getName().getString() + " kalktı."), false);
        }
        DogDisguise.refresh(dog);
        return ActionResult.SUCCESS;
    }

    // ── BESLEME ───────────────────────────────────────────────────────────────

    private static ActionResult feed(ServerPlayerEntity owner, ServerPlayerEntity dog,
            DogData data, ItemStack meat) {
        HungerManager hunger = dog.getHungerManager();
        int newFood = Math.min(20, hunger.getFoodLevel() + 4);
        hunger.setFoodLevel(newFood);
        hunger.setSaturationLevel(Math.min(hunger.getSaturationLevel() + 2.0f, newFood));
        if (!owner.isCreative()) meat.decrement(1);
        playSound(dog, SoundEvents.ENTITY_WOLF_AMBIENT, 1.2f);
        dog.sendMessage(Text.literal("§c🍖 Sahibin seni besledi!"), true);
        owner.sendMessage(Text.literal("§a" + dog.getName().getString() + " beslendi!"), false);
        return ActionResult.SUCCESS;
    }

    // ── AŞK MODU & ÇİFTLEŞME ─────────────────────────────────────────────────

    private static ActionResult enterLoveMode(ServerPlayerEntity owner, ServerPlayerEntity dog,
            DogData data, ItemStack meat) {
        if (!owner.isCreative()) meat.decrement(1);
        data.setInLove(true);

        // Kalp partikülü
        if (dog.getWorld() instanceof ServerWorld sw) {
            sw.spawnParticles(ParticleTypes.HEART,
                dog.getX(), dog.getY() + 1.0, dog.getZ(),
                5, 0.5, 0.5, 0.5, 0.0);
        }

        playSound(dog, SoundEvents.ENTITY_WOLF_AMBIENT, 1.8f);
        dog.sendMessage(Text.literal("§d❤ Çiftleşmeye hazırsın!"), true);
        owner.sendMessage(Text.literal("§d❤ " + dog.getName().getString()
            + " çiftleşmeye hazır!"), false);

        return ActionResult.SUCCESS;
    }

    // ── TASMA ────────────────────────────────────────────────────────────────

    private static ActionResult toggleLeash(ServerPlayerEntity owner, ServerPlayerEntity dog,
            DogData data, ItemStack lead) {
        if (data.isLeashed()) {
            data.clearLeash();
            playSound(dog, SoundEvents.ENTITY_LEASH_KNOT_BREAK, 1.0f);
            dog.sendMessage(Text.literal("§7Tasman çözüldü."), true);
            owner.sendMessage(Text.literal("§7" + dog.getName().getString()
                + " tasmadan kurtuldu."), false);
        } else {
            data.setLeashAnchor(owner.getX(), owner.getY(), owner.getZ());
            data.setLeashFixed(false);
            if (!owner.isCreative()) lead.decrement(1);
            playSound(dog, SoundEvents.ENTITY_LEASH_KNOT_PLACE, 1.0f);
            dog.sendMessage(Text.literal("§7Sahibin sana tasma taktı."), true);
            owner.sendMessage(Text.literal("§7" + dog.getName().getString()
                + " tasmaya bağlandı."), false);
        }
        return ActionResult.SUCCESS;
    }

    // ── SERVER TICK ───────────────────────────────────────────────────────────

    private static void onServerTick(MinecraftServer server) {
        tickCounter++;

        // Çiftleşme kontrolü (her 20 tick = 1 saniye)
        if (tickCounter % 20 == 0) {
            checkBreeding(server);
            checkOwnerSleeping(server);
        }

        // Işınlanma kontrolü (her 2 saniye)
        if (tickCounter % TELEPORT_CHECK_INTERVAL != 0) return;

        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            if (!DogOriginChecker.isDogPlayer(player)) continue;
            DogData data = DogDataManager.get(player);
            if (!data.isTamed() || data.getOwnerUUID() == null) continue;

            ServerPlayerEntity owner = server.getPlayerManager().getPlayer(data.getOwnerUUID());
            if (owner == null) continue;

            if (data.isLeashed()) { tickLeash(player, owner, data); continue; }
            if (data.isSitting()) continue;

            double dist = player.squaredDistanceTo(owner);
            if (dist > TELEPORT_DISTANCE * TELEPORT_DISTANCE
                || !player.getWorld().getRegistryKey().equals(owner.getWorld().getRegistryKey()))
                teleportToOwner(player, owner);
        }
    }

    // ── ÇİFTLEŞME ────────────────────────────────────────────────────────────

    private static void checkBreeding(MinecraftServer server) {
        List<ServerPlayerEntity> dogPlayers = server.getPlayerManager().getPlayerList()
            .stream()
            .filter(p -> DogOriginChecker.isDogPlayer(p)
                && DogDataManager.get(p).isInLove())
            .toList();

        // Köpek oyuncu + köpek oyuncu
        for (int i = 0; i < dogPlayers.size(); i++) {
            for (int j = i + 1; j < dogPlayers.size(); j++) {
                ServerPlayerEntity dogA = dogPlayers.get(i);
                ServerPlayerEntity dogB = dogPlayers.get(j);

                if (dogA.distanceTo(dogB) > BREED_DISTANCE) continue;
                if (!dogA.getWorld().getRegistryKey()
                    .equals(dogB.getWorld().getRegistryKey())) continue;

                spawnBabyWolf(dogA, dogB);
            }
        }

        // Köpek oyuncu + vanilla wolf
        for (ServerPlayerEntity dog : dogPlayers) {
            if (!(dog.getWorld() instanceof ServerWorld sw)) continue;

            List<WolfEntity> nearbyWolves = sw.getEntitiesByClass(
                WolfEntity.class,
                dog.getBoundingBox().expand(BREED_DISTANCE),
                w -> w.isTamed() && w.isInLove()
            );

            if (!nearbyWolves.isEmpty()) {
                spawnBabyWolf(dog, nearbyWolves.get(0));
            }
        }
    }

    private static void spawnBabyWolf(ServerPlayerEntity dogA, ServerPlayerEntity dogB) {
        DogData dataA = DogDataManager.get(dogA);
        DogData dataB = DogDataManager.get(dogB);
        dataA.setBreedCooldown();
        dataB.setBreedCooldown();

        // Yavru wolf spawn et
        if (!(dogA.getWorld() instanceof ServerWorld sw)) return;

        WolfEntity baby = EntityType.WOLF.create(sw);
        if (baby == null) return;

        baby.setBaby(true);
        baby.setTamed(true);
        baby.refreshPositionAndAngles(
            dogA.getX(), dogA.getY(), dogA.getZ(),
            dogA.getYaw(), 0
        );
        sw.spawnEntity(baby);

        // Kalp partikülü
        sw.spawnParticles(ParticleTypes.HEART,
            dogA.getX(), dogA.getY() + 1.0, dogA.getZ(),
            10, 0.5, 0.5, 0.5, 0.0);

        dogA.sendMessage(Text.literal("§d🐺 Bir yavru dünyaya geldi!"), false);
        dogB.sendMessage(Text.literal("§d🐺 Bir yavru dünyaya geldi!"), false);
    }

    private static void spawnBabyWolf(ServerPlayerEntity dog, WolfEntity wolf) {
        DogData data = DogDataManager.get(dog);
        data.setBreedCooldown();
        wolf.setLoveTicks(0);

        if (!(dog.getWorld() instanceof ServerWorld sw)) return;

        WolfEntity baby = EntityType.WOLF.create(sw);
        if (baby == null) return;

        baby.setBaby(true);
        baby.setTamed(true);
        baby.refreshPositionAndAngles(
            dog.getX(), dog.getY(), dog.getZ(),
            dog.getYaw(), 0
        );
        sw.spawnEntity(baby);

        sw.spawnParticles(ParticleTypes.HEART,
            dog.getX(), dog.getY() + 1.0, dog.getZ(),
            10, 0.5, 0.5, 0.5, 0.0);

        dog.sendMessage(Text.literal("§d🐺 Bir yavru dünyaya geldi!"), false);
    }

    // ── SAHİP UYUYUNCA ────────────────────────────────────────────────────────

    private static void checkOwnerSleeping(MinecraftServer server) {
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            if (!DogOriginChecker.isDogPlayer(player)) continue;
            DogData data = DogDataManager.get(player);
            if (!data.isTamed() || data.getOwnerUUID() == null) continue;

            ServerPlayerEntity owner = server.getPlayerManager().getPlayer(data.getOwnerUUID());
            if (owner == null) continue;

            if (owner.isSleeping() && !data.isSitting()) {
                // Sahibi uyuyor - köpeği oturt
                data.setSitting(true);
                player.addStatusEffect(new net.minecraft.entity.effect.StatusEffectInstance(
                    net.minecraft.entity.effect.StatusEffects.BLINDNESS,
                    60, 0, false, false, false
                ));
                player.sendMessage(Text.literal("§8Sahibin uyudu, sen de dinleniyorsun..."), true);
                DogDisguise.refresh(player);
            } else if (!owner.isSleeping() && data.isSitting()) {
                // Sahibi uyandı - köpeği kaldır (sadece uyuma sebebiyle otururken)
                data.setSitting(false);
                player.sendMessage(Text.literal("§7Sahibin uyandı!"), true);
                DogDisguise.refresh(player);
            }
        }
    }

    // ── YARDIMCI ─────────────────────────────────────────────────────────────

    private static void tickLeash(ServerPlayerEntity dog, ServerPlayerEntity owner, DogData data) {
        double anchorX = data.getLeashAnchorX();
        double anchorY = data.getLeashAnchorY();
        double anchorZ = data.getLeashAnchorZ();

        if (!data.isLeashFixed()) {
            if (!dog.getWorld().getRegistryKey().equals(owner.getWorld().getRegistryKey())) {
                data.clearLeash();
                dog.sendMessage(Text.literal("§cTasman koptu!"), true);
                return;
            }
            anchorX = owner.getX();
            anchorY = owner.getY();
            anchorZ = owner.getZ();
            data.setLeashAnchor(anchorX, anchorY, anchorZ);
        }

        double dx = anchorX - dog.getX();
        double dy = anchorY - dog.getY();
        double dz = anchorZ - dog.getZ();
        double dist = Math.sqrt(dx*dx + dy*dy + dz*dz);

        if (dist > LEASH_MAX_DISTANCE) {
            dog.setVelocity(dx/dist*0.4, dy/dist*0.1+0.1, dz/dist*0.4);
            dog.velocityModified = true;
        }
    }

    private static void teleportToOwner(ServerPlayerEntity dog, ServerPlayerEntity owner) {
        ServerWorld ownerWorld = (ServerWorld) owner.getWorld();
        dog.teleport(ownerWorld, owner.getX()+1.0, owner.getY(), owner.getZ(),
            dog.getYaw(), dog.getPitch());
        playSound(dog, SoundEvents.ENTITY_ENDERMAN_TELEPORT, 1.5f);
        dog.sendMessage(Text.literal("§7Sahibinin yanına ışınlandın."), true);
    }

    private static void onPlayerDeath(net.minecraft.entity.LivingEntity entity,
            DamageSource source) {
        if (!(entity instanceof ServerPlayerEntity dead)) return;
        if (!DogOriginChecker.isDogPlayer(dead)) return;
        DogData data = DogDataManager.get(dead);
        if (!data.isTamed() || data.getOwnerUUID() == null) return;
        ServerPlayerEntity owner = dead.getServer().getPlayerManager()
            .getPlayer(data.getOwnerUUID());
        if (owner != null)
            owner.sendMessage(Text.literal("§c💔 Köpeğin "
                + dead.getName().getString() + " öldü!"), false);
    }

    private static void playSound(ServerPlayerEntity player,
            net.minecraft.sound.SoundEvent sound, float pitch) {
        player.getWorld().playSoundFromEntity(null, player, sound,
            SoundCategory.PLAYERS, 1.0f, pitch);
    }
}
