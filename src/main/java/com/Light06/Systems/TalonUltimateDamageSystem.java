package com.Light06.Systems;

import com.Light06.TalonPlugin;
import com.Light06.Components.TalonPlayerComponent;
import com.hypixel.hytale.component.*;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageEventSystem;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nullable;
import javax.annotation.Nonnull;


public class TalonUltimateDamageSystem extends DamageEventSystem {

    @Override
    public @Nullable Query<EntityStore> getQuery() {
        return Query.any();
    }

    @Override
    public void handle(int i, @Nonnull ArchetypeChunk<EntityStore> archetypeChunk, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer, @Nonnull Damage damage) {
        Ref<EntityStore> targetRef = archetypeChunk.getReferenceTo(i);
        Damage.Source source = damage.getSource();

        if (source instanceof Damage.EntitySource entitySource) {
            Ref<EntityStore> attackerRef = entitySource.getRef();

            if (attackerRef != null && attackerRef.isValid() && !attackerRef.equals(targetRef)) {

                TalonPlayerComponent talonPlayerComponent = store.getComponent(attackerRef, TalonPlugin.getTalonPlayerComponentType());
                if (talonPlayerComponent != null && talonPlayerComponent.IsActive && talonPlayerComponent.lockedTarget == null) {

                    talonPlayerComponent.lockedTarget = targetRef;
                }
            }
        }
    }
}