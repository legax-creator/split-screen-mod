package com.example;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MovementType;
import net.minecraft.util.math.Vec3d;

import java.util.UUID;

/**
 * 2. oyuncu artik sunucu tarafinda "FakePlayer" olarak DEGIL,
 * dogrudan istemci (client) dunyasinda gorunur bir OtherClientPlayerEntity
 * olarak tutuluyor.
 *
 * FakePlayer'in sorunu: network paketiyle senkronize edilmedigi icin
 * istemci onun varligindan haberdar olmuyordu -> model gorunmuyordu ve
 * hareketi de ekrana yansimiyordu. OtherClientPlayerEntity ise zaten
 * vanilla'nin "diger oyunculari" cizmek icin kullandigi sinif; onu
 * dogrudan ClientWorld'e eklersek normal render dongusu onu otomatik cizer.
 */
public class SecondPlayerManager {
    private static final UUID PLAYER2_UUID = UUID.fromString("11111111-2222-3333-4444-555555555555");
    private static final String PLAYER2_NAME = "Oyuncu2";

    public static OtherClientPlayerEntity player2 = null;

    /** 2. oyuncuyu istemci dunyasina gorsel olarak ekler. */
    public static void spawnPlayer2(ClientWorld world, ClientPlayerEntity host) {
        if (world == null || host == null || player2 != null) return; // zaten eklenmis

        GameProfile profile = new GameProfile(PLAYER2_UUID, PLAYER2_NAME);
        player2 = new OtherClientPlayerEntity(world, profile);

        // 1. oyuncunun 2 blok yanina dogsun
        player2.setPosition(host.getX() + 2, host.getY(), host.getZ());
        player2.setYaw(host.getYaw());
        player2.setPitch(host.getPitch());
        player2.setHeadYaw(host.getYaw());

        // ClientWorld'e ekleyince WorldRenderer onu otomatik olarak sahneye cizer
        world.addEntity(player2.getId(), player2);
    }

    // Dusey hiz (yercekimi/ziplama icin)
    private static double velocityY = 0.0;

    /**
     * Her tick cagrilmali (splitScreenActive oldugu surece).
     * Artik dogrudan setPosition yerine Entity.move(...) kullaniyoruz:
     * bu, dunyanin collision (carpisma) kutularina bakarak hareketi
     * bloklara gore keser/kaydirir -> artik bloklarin icinden gecmez,
     * merdiven/esik gibi tek bloklu yukseklikleri tirmanabilir.
     *
     * @param forward   ileri(+)/geri(-) girdisi
     * @param strafe    saga(+)/sola(-) girdisi
     * @param yawDelta  bakis acisi degisimi (derece)
     * @param jump      ziplama/yuzerken yukari cikma tusu
     * @param sprint    kosma tusu
     * @param sneak     egilme (crouch) tusu
     */
    public static void tickPlayer2(double forward, double strafe, float yawDelta, boolean jump, boolean sprint, boolean sneak) {
        if (player2 == null) return;

        player2.setYaw(player2.getYaw() + yawDelta);
        player2.setHeadYaw(player2.getYaw());

        // Animasyon/poz icin: kosma ve egilme durumunu modele bildir
        player2.setSprinting(sprint && !sneak);
        player2.setSneaking(sneak);

        // Hiz carpani: egilirken yavas, kosarken hizli
        double speedMultiplier = sneak ? 0.3 : (sprint ? 1.6 : 1.0);

        // Yerel (ileri/yan) hareketi, oyuncunun bakis acisina gore dunya eksenine cevir
        float yawRad = (float) Math.toRadians(player2.getYaw());
        double sin = Math.sin(yawRad);
        double cos = Math.cos(yawRad);
        double worldDx = (strafe * cos - forward * sin) * speedMultiplier;
        double worldDz = (forward * cos + strafe * sin) * speedMultiplier;

        boolean inWater = player2.isTouchingWater();

        if (inWater) {
            // Suda yuzme: yercekimi zayif, ziplama tusu yukari yuzmeyi saglar
            velocityY += jump ? 0.04 : -0.02;
            velocityY = Math.max(-0.2, Math.min(velocityY, 0.2));
        } else if (player2.isOnGround()) {
            if (jump) {
                velocityY = 0.42;
            } else if (velocityY < 0) {
                velocityY = 0.0;
            }
        } else {
            velocityY -= 0.08;
            velocityY *= 0.98;
        }

        // Entity.move -> collision (carpisma) hesaplarini dunyaya gore otomatik yapar
        player2.move(MovementType.SELF, new Vec3d(worldDx, velocityY, worldDz));
    }

    /** Split-screen kapatildiginda 2. oyuncuyu dunyadan kaldirir (istege bagli). */
    public static void despawnPlayer2(ClientWorld world) {
        if (player2 == null || world == null) return;
        world.removeEntity(player2.getId(), Entity.RemovalReason.DISCARDED);
        player2 = null;
        velocityY = 0.0;
    }
}
