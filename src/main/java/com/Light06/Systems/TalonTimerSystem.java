package com.Light06.Systems;

import com.Light06.Components.TalonPlayerComponent;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
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
            cb.removeComponent(playerRef, TalonPlayerComponent.getComponentType());
            return;
        }

        TimeResource timeResource = cb.getResource(TimeResource.getResourceType());
        long now = timeResource.getNow().toEpochMilli();

        if (now >= playerTrack.hoverEndTime) {
            playerTrack.IsActive = false;
        }
    }
}