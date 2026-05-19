package com.Light06.Systems;

import com.Light06.TalonPlugin;
import com.Light06.Components.TalonDaggerComponent;
import com.Light06.Components.TalonDaggerComponent.DaggerState;
import com.Light06.Components.TalonPlayerComponent;
import com.hypixel.hytale.component.*;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.math.shape.Box;
import com.hypixel.hytale.server.core.modules.entity.component.BoundingBox;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageCause;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageSystems;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathComponent;
import com.hypixel.hytale.server.core.modules.projectile.config.StandardPhysicsProvider;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;

public class TalonDaggerTickingSystem extends EntityTickingSystem<EntityStore> {

    @Nonnull
    @Override
    public Query<EntityStore> getQuery() {
        return Query.and(
                TalonPlugin.getTalonDaggerComponentType(),
                TransformComponent.getComponentType()
        );
    }

    @Override
    public void tick(float dt, int index, @Nonnull ArchetypeChunk<EntityStore> chunk, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> cb) {
        Ref<EntityStore> daggerRef = chunk.getReferenceTo(index);
        TalonDaggerComponent dagger = (TalonDaggerComponent) chunk.getComponent(index, TalonPlugin.getTalonDaggerComponentType());
        TransformComponent tc = (TransformComponent) chunk.getComponent(index, TransformComponent.getComponentType());

        if (dagger == null || tc == null) return;

        StandardPhysicsProvider spp = (StandardPhysicsProvider) store.getComponent(daggerRef, StandardPhysicsProvider.getComponentType());
        if (spp != null && spp.getState() != StandardPhysicsProvider.STATE.INACTIVE) {
            spp.setState(StandardPhysicsProvider.STATE.INACTIVE);
        }

        if (!dagger.ownerRef.isValid() || store.getComponent(dagger.ownerRef, DeathComponent.getComponentType()) != null) {
            cb.removeEntity(daggerRef, RemoveReason.REMOVE);
            return;
        }

        dagger.stateTimer += dt;
        TalonPlayerComponent playerTrack = store.getComponent(dagger.ownerRef, TalonPlugin.getTalonPlayerComponentType());

        switch (dagger.state) {
            case OUTWARD:
                if (dagger.stateTimer >= 0.35f) {
                    dagger.vx = 0; dagger.vy = 0; dagger.vz = 0;
                    dagger.state = DaggerState.HOVERING;
                    dagger.stateTimer = 0.0f;
                }
                break;

            case HOVERING:
                if (playerTrack != null && playerTrack.lockedTarget != null && playerTrack.lockedTarget.isValid()) {
                    dagger.state = DaggerState.HOMING;
                    dagger.targetRef = playerTrack.lockedTarget;
                    dagger.isReturningToOwner = false;
                    TalonPlayerComponent talonPlayerComponent = store.getComponent(dagger.ownerRef, TalonPlugin.getTalonPlayerComponentType());
                    if (talonPlayerComponent != null) {
                        cb.removeComponent(dagger.ownerRef, TalonPlugin.getTalonPlayerComponentType());
                    }
                }
                else if (dagger.stateTimer >= 10.0f || (playerTrack != null && !playerTrack.IsActive)) {
                    dagger.state = DaggerState.HOMING;
                    dagger.targetRef = dagger.ownerRef;
                    dagger.isReturningToOwner = true;
                    TalonPlayerComponent talonPlayerComponent = store.getComponent(dagger.ownerRef, TalonPlugin.getTalonPlayerComponentType());
                    if (talonPlayerComponent != null) {
                        cb.removeComponent(dagger.ownerRef, TalonPlugin.getTalonPlayerComponentType());
                    }
                }
                break;

            case HOMING:
                if (dagger.targetRef == null || !dagger.targetRef.isValid() || store.getComponent(dagger.targetRef, DeathComponent.getComponentType()) != null) {
                    if (!dagger.isReturningToOwner) {
                        dagger.targetRef = dagger.ownerRef;
                        dagger.isReturningToOwner = true;
                    } else {
                        cb.removeEntity(daggerRef, RemoveReason.REMOVE);
                        return;
                    }
                }

                TransformComponent targetTc = (TransformComponent) store.getComponent(dagger.targetRef, TransformComponent.getComponentType());
                if (targetTc != null) {
                    double tx = targetTc.getPosition().x;
                    double tz = targetTc.getPosition().z;
                    double ty = targetTc.getPosition().y + 1.0;

                    BoundingBox bb = (BoundingBox) store.getComponent(dagger.targetRef, BoundingBox.getComponentType());
                    if (bb != null) {
                        Box box = bb.getBoundingBox();
                        ty = targetTc.getPosition().y + (box.getMin().y + box.getMax().y) * 0.5;
                    }

                    double dx = tx - dagger.px;
                    double dy = ty - dagger.py;
                    double dz = tz - dagger.pz;
                    double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);

                    if (dist < 1.3) {
                        if (!dagger.isReturningToOwner) {
                            DamageSystems.executeDamage(dagger.targetRef, cb, new Damage(new Damage.EntitySource(dagger.ownerRef), DamageCause.PHYSICAL, dagger.damage));
                        }
                        cb.removeEntity(daggerRef, RemoveReason.REMOVE);
                        return;
                    }

                    if (dist > 0.1) {
                        double ndx = dx / dist; double ndy = dy / dist; double ndz = dz / dist;
                        double speed = Math.sqrt(dagger.vx * dagger.vx + dagger.vy * dagger.vy + dagger.vz * dagger.vz);
                        if (speed < 1.0) speed = dagger.speed;

                        double cvx = dagger.vx / speed; double cvy = dagger.vy / speed; double cvz = dagger.vz / speed;
                        double t = Math.min(1.0, dagger.turnRate * dt);

                        double nvx = cvx * (1.0 - t) + ndx * t;
                        double nvy = cvy * (1.0 - t) + ndy * t;
                        double nvz = cvz * (1.0 - t) + ndz * t;

                        double nlen = Math.sqrt(nvx * nvx + nvy * nvy + nvz * nvz);
                        if (nlen > 0.001) {
                            dagger.vx = (nvx / nlen) * dagger.speed;
                            dagger.vy = (nvy / nlen) * dagger.speed;
                            dagger.vz = (nvz / nlen) * dagger.speed;
                        }
                    }
                }
                break;
        }

        dagger.px += dagger.vx * dt;
        dagger.py += dagger.vy * dt;
        dagger.pz += dagger.vz * dt;

        tc.getPosition().assign(dagger.px, dagger.py, dagger.pz);

        double hSpeedSq = dagger.vx * dagger.vx + dagger.vz * dagger.vz;
        if (hSpeedSq > 1.0E-10) {
            float yaw = (float) Math.atan2(-dagger.vx, -dagger.vz);
            float pitch = (float) Math.atan2(dagger.vy, Math.sqrt(hSpeedSq));
            tc.getRotation().setYaw(yaw);
            tc.getRotation().setPitch(pitch);
        }
    }
}