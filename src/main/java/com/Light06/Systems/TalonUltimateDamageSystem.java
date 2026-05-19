package com.Light06.Systems;

import com.Light06.TalonPlugin;
import com.Light06.Components.TalonPlayerComponent;
import com.hypixel.hytale.component.*;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageEventSystem;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TalonUltimateDamageSystem extends DamageEventSystem {

    @Override
    public @Nullable Query<EntityStore> getQuery() {
        return Query.any();
    }

    @Override
    public void handle(int i, @NotNull ArchetypeChunk<EntityStore> archetypeChunk, @NotNull Store<EntityStore> store, @NotNull CommandBuffer<EntityStore> commandBuffer, @NotNull Damage damage) {
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