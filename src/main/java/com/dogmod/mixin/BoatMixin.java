package com.dogmod.mixin;

import com.dogmod.capability.DogData;
import com.dogmod.capability.DogDataManager;
import com.dogmod.capability.DogOriginChecker;
import net.minecraft.entity.Entity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BoatEntity.class)
public class BoatMixin {

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

    @Inject(
        method = "removePassenger(Lnet/minecraft/entity/Entity;)V",
        at = @At("HEAD"),
        cancellable = true
    )
    private void onRemovePassenger(Entity passenger, CallbackInfo ci) {
        BoatEntity boat = (BoatEntity)(Object)this;
        if (boat.getWorld().isClient) return;
        if (!(passenger instanceof ServerPlayerEntity dog)) return;
        if (!DogOriginChecker.isDogPlayer(dog)) return;

        DogData data = DogDataManager.get(dog);
        if (!data.isTamed() || data.getOwnerUUID() == null) return;

        // Köpek kendisi inmek isterse engelle
        ci.cancel();
    }
}
