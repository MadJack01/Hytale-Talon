package com.Light06.Interactions;

import com.Light06.Components.TalonRakeComponent;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.protocol.SoundCategory;
import com.hypixel.hytale.server.core.asset.type.soundevent.config.SoundEvent;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInstantInteraction;
import com.hypixel.hytale.server.core.modules.projectile.ProjectileModule;
import com.hypixel.hytale.server.core.modules.projectile.config.ProjectileConfig;
import com.hypixel.hytale.server.core.modules.time.TimeResource;
import com.hypixel.hytale.server.core.universe.world.SoundUtil;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.Set;

public class TalonRakeInteraction extends SimpleInstantInteraction {
    public static final BuilderCodec<TalonRakeInteraction> CODEC;

    static {
        CODEC = BuilderCodec.builder(TalonRakeInteraction.class, TalonRakeInteraction::new, SimpleInstantInteraction.CODEC)
                .documentation("Throws 3 daggers in a tight cone that hover for 1 second, then return to the player.")
                .append(new KeyedCodec<>("Damage", Codec.FLOAT),
                        (interaction, o) -> interaction.damage = o,
                        (interaction) -> interaction.damage).add()
                .append(new KeyedCodec<>("Projectile Asset", Codec.STRING),
                        (interaction, o) -> interaction.projectileAsset = o,
                        (interaction) -> interaction.projectileAsset).add()
                .build();
    }

    protected float damage = 20.0f;
    protected String projectileAsset = "Projectile_Config_Dagger";

    @Override
    protected void firstRun(@Nonnull InteractionType interactionType, @Nonnull InteractionContext interactionContext, @Nonnull CooldownHandler cooldownHandler) {
        CommandBuffer<EntityStore> cb = interactionContext.getCommandBuffer();
        Ref<EntityStore> playerRef = interactionContext.getOwningEntity();
        Store<EntityStore> store = playerRef.getStore();

        TransformComponent trans = store.getComponent(playerRef, TransformComponent.getComponentType());
        if (trans == null) return;

        TimeResource timeResource = cb.getResource(TimeResource.getResourceType());
        long now = timeResource.getNow().toEpochMilli();

        ProjectileConfig config = (ProjectileConfig) ProjectileConfig.getAssetMap().getAsset(this.projectileAsset);
        if (config == null) return;

        int part1SoundIndex = SoundEvent.getAssetMap().getIndex("SFX_Rake_Part1");
        if (part1SoundIndex != 0) {
            SoundUtil.playSoundEvent3d(part1SoundIndex, SoundCategory.SFX, trans.getPosition().x, trans.getPosition().y, trans.getPosition().z, store);
        }

        float playerYaw = trans.getSentTransform().lookOrientation.yaw;
        float playerPitch = trans.getSentTransform().lookOrientation.pitch;

        double spreadRadians = Math.toRadians(18.0);
        double baseOutwardSpeed = 22.0;

        Set<Ref<EntityStore>> sharedOutwardHits = new HashSet<>();
        Set<Ref<EntityStore>> sharedReturnHits = new HashSet<>();

        for (int i = -1; i <= 1; i++) {
            Vector3d spawnPos = new Vector3d(
                    trans.getPosition().x,
                    trans.getPosition().y + 1.5,
                    trans.getPosition().z
            );

            double daggerYaw = playerYaw + (i * spreadRadians);

            double dirX = -Math.sin(daggerYaw) * Math.cos(playerPitch);
            double dirY = Math.sin(playerPitch);
            double dirZ = -Math.cos(daggerYaw) * Math.cos(playerPitch);

            Vector3d normalizedDir = new Vector3d(dirX, dirY, dirZ);

            Ref<EntityStore> daggerRef = ProjectileModule.get().spawnProjectile(playerRef, cb, config, spawnPos, normalizedDir);

            double currentSpeed = (i == 0) ? (baseOutwardSpeed * 1.15) : baseOutwardSpeed;

            double vx = dirX * currentSpeed;
            double vy = dirY * currentSpeed;
            double vz = dirZ * currentSpeed;

            TalonRakeComponent rakeComp = new TalonRakeComponent(
                    spawnPos.x, spawnPos.y, spawnPos.z,
                    vx, vy, vz, playerRef, this.damage
            );
            rakeComp.stateStartTime = now;

            rakeComp.outwardHits = sharedOutwardHits;
            rakeComp.returnHits = sharedReturnHits;

            cb.addComponent(daggerRef, TalonRakeComponent.getComponentType(), rakeComp);
        }
    }
}