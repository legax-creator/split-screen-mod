package com.dogmod.mixin;

import com.dogmod.capability.DogData;
import com.dogmod.capability.DogDataManager;
import com.dogmod.capability.DogOriginChecker;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BoatEntity.class)
public class BoatMixin {

    // Yakındaki köpek oyuncuyu otomatik bindir
    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        BoatEntity boat = (BoatEntity)(Object)this;
        if (boat.getWorld().isClient) return;
        if (!boat.getPassengerList().isEmpty()) return;

        boat.getWorld().getEntitiesByClass(
            ServerPlayerEntity.class,
            boat.getBoundingBox().expand(2.0),
            p -> DogOriginChecker.isDogPlayer(p) && !p.hasVehicle()
        ).forEach(dog -> dog.startRiding(boat, true));
    }

    // Köpek oyuncu inmek isterse engelle - sadece sahip indirebilir
    @Inject(method = "removeAllPassengers", at = @At("HEAD"), cancellable = true)
    private void onRemovePassengers(CallbackInfo ci) {
        BoatEntity boat = (BoatEntity)(Object)this;
        if (boat.getWorld().isClient) return;

        for (net.minecraft.entity.Entity passenger : boat.getPassengerList()) {
            if (!(passenger instanceof ServerPlayerEntity dog)) continue;
            if (!DogOriginChecker.isDogPlayer(dog)) continue;

            DogData data = DogDataManager.get(dog);
            if (!data.isTamed() || data.getOwnerUUID() == null) continue;

            // Sahibi mi indiriyor kontrol et
            ServerPlayerEntity owner = dog.getServer()
                .getPlayerManager().getPlayer(data.getOwnerUUID());

            // Sahip botta değilse veya indirme komutu vermemişse engelle
            if (owner == null || !owner.isRiding()) {
                ci.cancel();
            }
        }
    }
}
