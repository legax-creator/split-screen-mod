package com.dogmod.event;

import com.dogmod.DogMod;
import com.dogmod.capability.DogData;
import com.dogmod.capability.DogDataManager;
import com.dogmod.capability.DogOriginChecker;
import com.dogmod.disguise.DogDisguise;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.world.World;
import java.util.Set;

public class DogEventHandler {
    private static final int TELEPORT_CHECK_INTERVAL = 40;
    private static final double TELEPORT_DISTANCE = 20.0;
    private static final double LEASH_MAX_DISTANCE = 7.0;
    private static final Set<net.minecraft.item.Item> MEAT_ITEMS = Set.of(
        Items.BEEF, Items.COOKED_BEEF, Items.PORKCHOP, Items.COOKED_PORKCHOP,
        Items.MUTTON, Items.COOKED_MUTTON, Items.CHICKEN, Items.COOKED_CHICKEN,
        Items.RABBIT, Items.COOKED_RABBIT
    );
    private static int tickCounter = 0;

    public static void register() {
        UseEntityCallback.EVENT.register(DogEventHandler::onUseEntity);
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

        if (!data.isTamed() && held.isOf(Items.BONE)) return tame(interactor, target, data, held);
        if (!data.isTamed() || !data.isOwner(interactor.getUuid())) return ActionResult.PASS;
        if (held.isOf(Items.LEAD)) return toggleLeash(interactor, target, data, held);
        if (interactor.isSneaking()) return toggleSit(interactor, target, data);
        if (MEAT_ITEMS.contains(held.getItem()) && target.getHealth() < target.getMaxHealth())
            return feed(interactor, target, data, held);
        return ActionResult.PASS;
    }

    private static ActionResult tame(ServerPlayerEntity owner, ServerPlayerEntity dog,
            DogData data, ItemStack bone) {
        if (!owner.isCreative()) bone.decrement(1);
        if (Math.random() < 0.5) {
            data.setTamed(true);
            data.setOwnerUUID(owner.getUuid());
            playSound(dog, SoundEvents.ENTITY_WOLF_AMBIENT, 1.5f);
            dog.sendMessage(Text.literal("§a❤ " + owner.getName().getString() + " seni evcilleştirdi!"), false);
            owner.sendMessage(Text.literal("§a❤ " + dog.getName().getString() + " artık senin köpeğin!"), false);
            DogDisguise.refresh(dog);
        } else {
            playSound(dog, SoundEvents.ENTITY_WOLF_HURT, 1.0f);
            owner.sendMessage(Text.literal("§c" + dog.getName().getString() + " kemikle ilgilenmedi..."), false);
        }
        return ActionResult.SUCCESS;
    }

    private static ActionResult toggleSit(ServerPlayerEntity owner, ServerPlayerEntity dog, DogData data) {
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

    private static ActionResult feed(ServerPlayerEntity owner, ServerPlayerEntity dog,
            DogData data, ItemStack meat) {
        dog.heal(4.0f);
        if (!owner.isCreative()) meat.decrement(1);
        playSound(dog, SoundEvents.ENTITY_WOLF_AMBIENT, 1.2f);
        dog.sendMessage(Text.literal("§c❤ Sahibin seni besledi! +4 can"), true);
        owner.sendMessage(Text.literal("§a" + dog.getName().getString() + " beslendi!"), false);
        return ActionResult.SUCCESS;
    }

    private static ActionResult toggleLeash(ServerPlayerEntity owner, ServerPlayerEntity dog,
            DogData data, ItemStack lead) {
        if (data.isLeashed()) {
            data.clearLeash();
            playSound(dog, SoundEvents.ENTITY_LEASH_KNOT_BREAK, 1.0f);
            dog.sendMessage(Text.literal("§7Tasman çözüldü."), true);
            owner.sendMessage(Text.literal("§7" + dog.getName().getString() + " tasmadan kurtuldu."), false);
        } else {
            data.setLeashAnchor(owner.getX(), owner.getY(), owner.getZ());
            if (!owner.isCreative()) lead.decrement(1);
            playSound(dog, SoundEvents.ENTITY_LEASH_KNOT_PLACE, 1.0f);
            dog.sendMessage(Text.literal("§7Sahibin sana tasma taktı."), true);
            owner.sendMessage(Text.literal("§7" + dog.getName().getString() + " tasmaya bağlandı."), false);
        }
        return ActionResult.SUCCESS;
    }

    private static void onServerTick(MinecraftServer server) {
        tickCounter++;
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

    private static void tickLeash(ServerPlayerEntity dog, ServerPlayerEntity owner, DogData data) {
        if (!dog.getWorld().getRegistryKey().equals(owner.getWorld().getRegistryKey())) {
            data.clearLeash();
            dog.sendMessage(Text.literal("§cTasman koptu!"), true);
            return;
        }
        double dist = dog.distanceTo(owner);
        if (dist > LEASH_MAX_DISTANCE) {
            double dx = owner.getX() - dog.getX();
            double dy = owner.getY() - dog.getY();
            double dz = owner.getZ() - dog.getZ();
            double len = Math.sqrt(dx*dx + dy*dy + dz*dz);
            dog.setVelocity(dx/len*0.4, dy/len*0.1+0.1, dz/len*0.4);
            dog.velocityModified = true;
        }
    }

    private static void teleportToOwner(ServerPlayerEntity dog, ServerPlayerEntity owner) {
        ServerWorld ownerWorld = (ServerWorld) owner.getWorld();
        dog.teleport(ownerWorld, owner.getX()+1.0, owner.getY(), owner.getZ(), dog.getYaw(), dog.getPitch());
        playSound(dog, SoundEvents.ENTITY_ENDERMAN_TELEPORT, 1.5f);
        dog.sendMessage(Text.literal("§7Sahibinin yanına ışınlandın."), true);
    }

    private static void onPlayerDeath(net.minecraft.entity.LivingEntity entity, DamageSource source) {
        if (!(entity instanceof ServerPlayerEntity dead)) return;
        if (!DogOriginChecker.isDogPlayer(dead)) return;
        DogData data = DogDataManager.get(dead);
        if (!data.isTamed() || data.getOwnerUUID() == null) return;
        ServerPlayerEntity owner = dead.getServer().getPlayerManager().getPlayer(data.getOwnerUUID());
        if (owner != null)
            owner.sendMessage(Text.literal("§c💔 Köpeğin " + dead.getName().getString() + " öldü!"), false);
    }

    private static void playSound(ServerPlayerEntity player, net.minecraft.sound.SoundEvent sound, float pitch) {
        player.getWorld().playSoundFromEntity(null, player, sound, SoundCategory.PLAYERS, 1.0f, pitch);
    }
}
