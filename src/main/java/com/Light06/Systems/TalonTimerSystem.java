package com.Light06.Systems;

import com.Light06.Components.TalonPlayerComponent;
import com.hypixel.hytale.component.*;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.server.core.asset.type.entityeffect.config.EntityEffect;
import com.hypixel.hytale.server.core.entity.effect.EffectControllerComponent;
import com.hypixel.hytale.server.core.modules.time.TimeResource;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;

public class TalonTimerSystem extends EntityTickingSystem<EntityStore> {

    @Nonnull
    @Override
    public Query<EntityStore> getQuery() {
        return Query.and(TalonPlayerComponent.getComponentType());
    }

    @Override
    public void tick(float dt, int index, @Nonnull ArchetypeChunk<EntityStore> chunk, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> cb) {
        TalonPlayerComponent playerTrack = (TalonPlayerComponent) chunk.getComponent(index, TalonPlayerComponent.getComponentType());
        Ref<EntityStore> playerRef = chunk.getReferenceTo(index);

        if (playerTrack == null) return;

        if (!playerTrack.IsActive) {
            EffectControllerComponent effectController = store.getComponent(playerRef, EffectControllerComponent.getComponentType());

            if (effectController != null) {
                int effectIndex = EntityEffect.getAssetMap().getIndex("Effect_SFX_Ultimate_Part2");
                if (effectIndex != 0 && effectController.hasEffect(effectIndex)) {
                    effectController.removeEffect(playerRef, effectIndex, cb);
                }
            }

            cb.removeComponent(playerRef, TalonPlayerComponent.getComponentType());
            return;
        }

        TimeResource timeResource = cb.getResource(TimeResource.getResourceType());
        long now = timeResource.getNow().toEpochMilli();

        if (playerTrack.isDaggerHovering(now) && !playerTrack.hoverSoundPlayed) {
            EffectControllerComponent effectController = store.getComponent(playerRef, EffectControllerComponent.getComponentType());
            EntityEffect hoverEffect = EntityEffect.getAssetMap().getAsset("Effect_SFX_Ultimate_Part2");

            if (effectController != null && hoverEffect != null) {
                effectController.addEffect(playerRef, hoverEffect, cb);
            }
            playerTrack.hoverSoundPlayed = true;
        }

        if (now >= playerTrack.hoverEndTime) {
            playerTrack.IsActive = false;
        }
    }
}