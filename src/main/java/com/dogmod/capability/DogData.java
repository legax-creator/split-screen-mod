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
    private boolean inLove = false;
    private long loveCooldown = 0;
    private static final long LOVE_COOLDOWN_MS = 300000;
    private String dogName = null; // Köpek adı
    private int collarColor = 14; // Varsayılan kırmızı

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
    public boolean isInLove() { return inLove; }
    public void setInLove(boolean inLove) { this.inLove = inLove; }
    public String getDogName() { return dogName; }
    public void setDogName(String dogName) { this.dogName = dogName; }
    public int getCollarColor() { return collarColor; }
    public void setCollarColor(int collarColor) { this.collarColor = collarColor; }

    public boolean canBreed() {
        return System.currentTimeMillis() - loveCooldown > LOVE_COOLDOWN_MS;
    }
    public void setBreedCooldown() {
        this.loveCooldown = System.currentTimeMillis();
        this.inLove = false;
    }

    public void setLeashAnchor(double x, double y, double z) {
        this.leashAnchorX = x; this.leashAnchorY = y; this.leashAnchorZ = z;
        this.leashed = true;
    }
    public void clearLeash() {
        this.leashed = false; this.leashFixed = false;
        this.leashAnchorX = 0; this.leashAnchorY = 0; this.leashAnchorZ = 0;
    }

    public void writeToNbt(NbtCompound nbt) {
        nbt.putBoolean("isDog", isDog);
        nbt.putBoolean("tamed", tamed);
        nbt.putBoolean("sitting", sitting);
        nbt.putBoolean("leashed", leashed);
        nbt.putBoolean("leashFixed", leashFixed);
        nbt.putBoolean("inLove", inLove);
        nbt.putLong("loveCooldown", loveCooldown);
        nbt.putInt("collarColor", collarColor);
        if (ownerUUID != null) nbt.putUuid("ownerUUID", ownerUUID);
        if (dogName != null) nbt.putString("dogName", dogName);
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
        this.inLove = nbt.getBoolean("inLove");
        this.loveCooldown = nbt.getLong("loveCooldown");
        this.collarColor = nbt.contains("collarColor") ? nbt.getInt("collarColor") : 14;
        if (nbt.containsUuid("ownerUUID")) this.ownerUUID = nbt.getUuid("ownerUUID");
        if (nbt.contains("dogName")) this.dogName = nbt.getString("dogName");
        if (leashed) {
            this.leashAnchorX = nbt.getDouble("leashX");
            this.leashAnchorY = nbt.getDouble("leashY");
            this.leashAnchorZ = nbt.getDouble("leashZ");
        }
    }
}
