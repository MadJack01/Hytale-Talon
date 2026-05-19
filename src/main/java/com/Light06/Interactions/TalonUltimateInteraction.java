package com.Light06.Interactions;

import com.Light06.TalonPlugin;
import com.Light06.Components.TalonDaggerComponent;
import com.Light06.Components.TalonPlayerComponent;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInstantInteraction;
import com.hypixel.hytale.server.core.modules.projectile.ProjectileModule;
import com.hypixel.hytale.server.core.modules.projectile.config.ProjectileConfig;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;

public class TalonUltimateInteraction extends SimpleInstantInteraction {
    public static final BuilderCodec<TalonUltimateInteraction> CODEC;

    static {
        CODEC = BuilderCodec.builder(TalonUltimateInteraction.class, TalonUltimateInteraction::new, SimpleInstantInteraction.CODEC)
                .documentation("Fires a ring of daggers outward that freeze and then track targets damaged by the player.")
                .append(new KeyedCodec<>("Dagger Count", Codec.INTEGER),
                        (interaction, o) -> interaction.daggerCount = o,
                        (interaction) -> interaction.daggerCount).add()
                .append(new KeyedCodec<>("Hover Duration", Codec.FLOAT),
                        (interaction, o) -> interaction.hoverDuration = o,
                        (interaction) -> interaction.hoverDuration).add()
                .append(new KeyedCodec<>("Damage", Codec.FLOAT),
                        (interaction, o) -> interaction.damage = o,
                        (interaction) -> interaction.damage).add()
                .append(new KeyedCodec<>("Projectile Asset", Codec.STRING),
                        (interaction, o) -> interaction.projectileAsset = o,
                        (interaction) -> interaction.projectileAsset).add()
                .build();
    }

    protected Integer daggerCount = 12;
    protected float hoverDuration = 10.0f;
    protected float damage = 15.0f;
    protected String projectileAsset = "";

    @Override
    protected void firstRun(@Nonnull InteractionType interactionType, @Nonnull InteractionContext interactionContext, @Nonnull CooldownHandler cooldownHandler) {
        CommandBuffer<EntityStore> cb = interactionContext.getCommandBuffer();
        Ref<EntityStore> playerRef = interactionContext.getOwningEntity();
        Store<EntityStore> store = playerRef.getStore();

        TransformComponent trans = store.getComponent(playerRef, TransformComponent.getComponentType());
        if (trans == null) return;
        TalonPlayerComponent talonPlayerComponent = store.getComponent(playerRef, TalonPlugin.getTalonPlayerComponentType());
        if (talonPlayerComponent != null) {
            cb.removeComponent(playerRef, TalonPlugin.getTalonPlayerComponentType());
        }

        TalonPlayerComponent newPlayerTrack = new TalonPlayerComponent();
        newPlayerTrack.remainingHoverTime = this.hoverDuration;
        newPlayerTrack.IsActive = true;
        cb.addComponent(playerRef, TalonPlugin.getTalonPlayerComponentType(), newPlayerTrack);

        ProjectileConfig config = (ProjectileConfig) ProjectileConfig.getAssetMap().getAsset(this.projectileAsset);
        if (config == null) return;

        Vector3d spawnPos = new Vector3d(
                trans.getPosition().x,
                trans.getPosition().y + 1.0,
                trans.getPosition().z
        );

        double outwardSpeed = 16.0;

        for (int i = 0; i < this.daggerCount; i++) {
            double angle = i * (2.0 * Math.PI / this.daggerCount);
            double vx = Math.cos(angle) * outwardSpeed;
            double vz = Math.sin(angle) * outwardSpeed;
            double vy = 0.0;

            Vector3d velocityDirection = new Vector3d(vx, vy, vz);
            Ref<EntityStore> daggerRef = ProjectileModule.get().spawnProjectile(playerRef, cb, config, spawnPos, velocityDirection);

            TalonDaggerComponent daggerComp = new TalonDaggerComponent(
                    spawnPos.x, spawnPos.y, spawnPos.z,
                    vx, vy, vz, playerRef, this.damage
            );

            cb.addComponent(daggerRef, TalonPlugin.getTalonDaggerComponentType(), daggerComp);
        }
    }
}