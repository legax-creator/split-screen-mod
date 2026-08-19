package com.dogmod.capability;

import net.minecraft.nbt.NbtCompound;
import java.util.UUID;

public class DogData {
    private boolean isDog = false;
    private UUID ownerUUID = null;
    private boolean tamed = false;
    private boolean sitting = false;
    private boolean leashed = false;
    private boolean leashFixed = false;
    private double leashAnchorX, leashAnchorY, leashAnchorZ;

    public boolean isDog() { return isDog; }
    public void setDog(boolean isDog) { this.isDog = isDog; }
    public boolean isTamed() { return tamed; }
    public void setTamed(boolean tamed) { this.tamed = tamed; }
    public UUID getOwnerUUID() { return ownerUUID; }
    public void setOwnerUUID(UUID ownerUUID) { this.ownerUUID = ownerUUID; }
    public boolean isOwner(UUID uuid) { return ownerUUID != null && ownerUUID.equals(uuid); }
    public boolean isSitting() { return sitting; }
    public void setSitting(boolean sitting) { this.sitting = sitting; }
    public boolean isLeashed() { return leashed; }
    public void setLeashed(boolean leashed) { this.leashed = leashed; }
    public boolean isLeashFixed() { return leashFixed; }
    public void setLeashFixed(boolean leashFixed) { this.leashFixed = leashFixed; }
    public double getLeashAnchorX() { return leashAnchorX; }
    public double getLeashAnchorY() { return leashAnchorY; }
    public double getLeashAnchorZ() { return leashAnchorZ; }

    public void setLeashAnchor(double x, double y, double z) {
        this.leashAnchorX = x; this.leashAnchorY = y; this.leashAnchorZ = z;
        this.leashed = true;
    }

    public void clearLeash() {
        this.leashed = false;
        this.leashFixed = false;
        this.leashAnchorX = 0; this.leashAnchorY = 0; this.leashAnchorZ = 0;
    }

    public void writeToNbt(NbtCompound nbt) {
        nbt.putBoolean("isDog", isDog);
        nbt.putBoolean("tamed", tamed);
        nbt.putBoolean("sitting", sitting);
        nbt.putBoolean("leashed", leashed);
        nbt.putBoolean("leashFixed", leashFixed);
        if (ownerUUID != null) nbt.putUuid("ownerUUID", ownerUUID);
        if (leashed) {
            nbt.putDouble("leashX", leashAnchorX);
            nbt.putDouble("leashY", leashAnchorY);
            nbt.putDouble("leashZ", leashAnchorZ);
        }
    }

    public void readFromNbt(NbtCompound nbt) {
        this.isDog = nbt.getBoolean("isDog");
        this.tamed = nbt.getBoolean("tamed");
        this.sitting = nbt.getBoolean("sitting");
        this.leashed = nbt.getBoolean("leashed");
        this.leashFixed = nbt.getBoolean("leashFixed");
        if (nbt.containsUuid("ownerUUID")) this.ownerUUID = nbt.getUuid("ownerUUID");
        if (leashed) {
            this.leashAnchorX = nbt.getDouble("leashX");
            this.leashAnchorY = nbt.getDouble("leashY");
            this.leashAnchorZ = nbt.getDouble("leashZ");
        }
    }
}
