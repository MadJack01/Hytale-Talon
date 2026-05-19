package com.Light06.Systems;

import com.Light06.TalonPlugin;
import com.Light06.Components.TalonPlayerComponent;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;

public class TalonTimerSystem extends EntityTickingSystem<EntityStore> {

    @Nonnull
    @Override
    public Query<EntityStore> getQuery() {
        return Query.and(TalonPlugin.getTalonPlayerComponentType());
    }

    @Override
    public void tick(float dt, int index, @Nonnull ArchetypeChunk<EntityStore> chunk, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> cb) {
        TalonPlayerComponent playerTrack = (TalonPlayerComponent) chunk.getComponent(index, TalonPlugin.getTalonPlayerComponentType());

        if (playerTrack == null || !playerTrack.IsActive) return;

        playerTrack.remainingHoverTime -= dt;
        if (playerTrack.remainingHoverTime <= 0.0f) {
            playerTrack.IsActive = false;
        }
    }

}