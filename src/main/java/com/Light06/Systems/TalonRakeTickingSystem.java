package com.Light06.Systems;

import com.Light06.Components.TalonDaggerComponent;
import com.Light06.TalonPlugin;
import com.Light06.Components.TalonRakeComponent;
import com.Light06.Components.TalonRakeComponent.RakeState;
import com.hypixel.hytale.component.*;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.protocol.SoundCategory;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.entityeffect.config.EntityEffect;
import com.hypixel.hytale.server.core.asset.type.soundevent.config.SoundEvent;
import com.hypixel.hytale.server.core.entity.effect.EffectControllerComponent;
import com.hypixel.hytale.server.core.modules.entity.component.BoundingBox;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageCause;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageSystems;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathComponent;
import com.hypixel.hytale.server.core.modules.projectile.config.StandardPhysicsProvider;
import com.hypixel.hytale.server.core.modules.time.TimeResource;
import com.hypixel.hytale.server.core.universe.world.SoundUtil;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import java.util.Set;

public class TalonRakeTickingSystem extends EntityTickingSystem<EntityStore> {

    @Nonnull
    @Override
    public Query<EntityStore> getQuery() {
        return Query.and(
                TalonRakeComponent.getComponentType(),
                TransformComponent.getComponentType()
        );
    }

    @Override
    public void tick(float dt, int index, @Nonnull ArchetypeChunk<EntityStore> chunk, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> cb) {
        Ref<EntityStore> daggerRef = chunk.getReferenceTo(index);
        TalonRakeComponent dagger = (TalonRakeComponent) chunk.getComponent(index, TalonRakeComponent.getComponentType());
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

        TimeResource timeResource = cb.getResource(TimeResource.getResourceType());
        long now = timeResource.getNow().toEpochMilli();

        switch (dagger.state) {
            case OUTWARD:
                double outNextX = dagger.px + dagger.vx * dt;
                double outNextY = dagger.py + dagger.vy * dt;
                double outNextZ = dagger.pz + dagger.vz * dt;

                if (now >= dagger.stateStartTime + 400L || isHittingWall(outNextX, outNextY, outNextZ, store)) {
                    dagger.vx = 0;
                    dagger.vy = 0;
                    dagger.vz = 0;
                    dagger.state = RakeState.HOVERING;
                    dagger.stateStartTime = now;

                    int part2SoundIndex = SoundEvent.getAssetMap().getIndex("SFX_Rake_Part2");
                    if (part2SoundIndex != 0) {
                        SoundUtil.playSoundEvent3d(part2SoundIndex, SoundCategory.SFX, dagger.px, dagger.py, dagger.pz, store);
                    }
                }
                executePassThroughDamage(dagger, store, cb);
                break;

            case HOVERING:
                float currentYaw = tc.getRotation().getYaw();
                tc.getRotation().setYaw(currentYaw + (15.0f * dt));

                if (now >= dagger.stateStartTime + 1000L) {
                    dagger.state = RakeState.RETURNING;

                    int part3SoundIndex = SoundEvent.getAssetMap().getIndex("SFX_Rake_Part3");
                    if (part3SoundIndex != 0) {
                        SoundUtil.playSoundEvent3d(part3SoundIndex, SoundCategory.SFX, dagger.px, dagger.py, dagger.pz, store);
                    }
                }
                break;

            case RETURNING:
                TransformComponent ownerTc = (TransformComponent) store.getComponent(dagger.ownerRef, TransformComponent.getComponentType());
                if (ownerTc != null) {
                    double targetX = ownerTc.getPosition().x;
                    double targetZ = ownerTc.getPosition().z;
                    double targetY = ownerTc.getPosition().y + 1.0;

                    double dx = targetX - dagger.px;
                    double dy = targetY - dagger.py;
                    double dz = targetZ - dagger.pz;
                    double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);

                    if (dist < 1.5) {
                        cb.removeEntity(daggerRef, RemoveReason.REMOVE);
                        return;
                    }

                    executePassThroughDamage(dagger, store, cb);

                    if (dist > 0.1) {
                        double ndx = dx / dist; double ndy = dy / dist; double ndz = dz / dist;
                        double speed = Math.sqrt(dagger.vx * dagger.vx + dagger.vy * dagger.vy + dagger.vz * dagger.vz);
                        if (speed < 1.0) speed = dagger.returnSpeed;

                        double cvx = dagger.vx / speed; double cvy = dagger.vy / speed; double cvz = dagger.vz / speed;
                        double t = Math.min(1.0, dagger.turnRate * dt);

                        double nvx = cvx * (1.0 - t) + ndx * t;
                        double nvy = cvy * (1.0 - t) + ndy * t;
                        double nvz = cvz * (1.0 - t) + ndz * t;

                        double nlen = Math.sqrt(nvx * nvx + nvy * nvy + nvz * nvz);
                        if (nlen > 0.001) {
                            dagger.vx = (nvx / nlen) * dagger.returnSpeed;
                            dagger.vy = (nvy / nlen) * dagger.returnSpeed;
                            dagger.vz = (nvz / nlen) * dagger.returnSpeed;
                        }
                    }

                    double retNextX = dagger.px + dagger.vx * dt;
                    double retNextY = dagger.py + dagger.vy * dt;
                    double retNextZ = dagger.pz + dagger.vz * dt;

                    if (isHittingWall(retNextX, retNextY, retNextZ, store)) {
                        cb.removeEntity(daggerRef, RemoveReason.REMOVE);
                        return;
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

    private void executePassThroughDamage(TalonRakeComponent dagger, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {

        Set<Ref<EntityStore>> activeHitSet = (dagger.state == RakeState.RETURNING) ? dagger.returnHits : dagger.outwardHits;

        store.forEachChunk(Query.and(TransformComponent.getComponentType(), BoundingBox.getComponentType()), (c, unused) -> {
            int size = c.size();
            for (int i = 0; i < size; ++i) {
                Ref<EntityStore> hitRef = c.getReferenceTo(i);

                if (hitRef.equals(dagger.ownerRef) || activeHitSet.contains(hitRef)) continue;
                if (c.getComponent(i, DeathComponent.getComponentType()) != null) continue;

                TalonRakeComponent talonRakeComponent = store.getComponent(hitRef, TalonRakeComponent.getComponentType());
                if (talonRakeComponent != null) continue;
                TalonDaggerComponent talonDaggerComponent = store.getComponent(hitRef, TalonPlugin.get().getTalonDaggerComponentType());
                if (talonDaggerComponent != null) continue;

                TransformComponent hitTc = (TransformComponent) c.getComponent(i, TransformComponent.getComponentType());
                BoundingBox hitBb = (BoundingBox) c.getComponent(i, BoundingBox.getComponentType());

                double hx = hitTc.getPosition().x;
                double hz = hitTc.getPosition().z;
                double hy = hitTc.getPosition().y + (hitBb.getBoundingBox().getMin().y + hitBb.getBoundingBox().getMax().y) * 0.5;

                double hdx = hx - dagger.px;
                double hdy = hy - dagger.py;
                double hdz = hz - dagger.pz;
                double hDistSq = hdx * hdx + hdy * hdy + hdz * hdz;

                if (hDistSq < 2.25) {
                    activeHitSet.add(hitRef);
                    DamageSystems.executeDamage(hitRef, cb, new Damage(new Damage.EntitySource(dagger.ownerRef), DamageCause.PHYSICAL, dagger.damage));

                    String soundName = (dagger.state == RakeState.RETURNING) ? "SFX_Rake_Second_Hit" : "SFX_Rake_First_Hit";
                    int hitSoundIndex = SoundEvent.getAssetMap().getIndex(soundName);

                    if (hitSoundIndex != 0) {
                        SoundUtil.playSoundEvent3d(hitSoundIndex, SoundCategory.SFX, hx, hy, hz, store);
                    }

                    if (dagger.state == RakeState.RETURNING) {
                        EffectControllerComponent effectController = store.getComponent(hitRef, EffectControllerComponent.getComponentType());
                        if (effectController != null) {
                            EntityEffect slowEffect = EntityEffect.getAssetMap().getAsset("Rake_Slow");
                            if (slowEffect != null) {
                                effectController.addEffect(hitRef, slowEffect, cb);
                            }
                        }
                    }
                }
            }
        });
    }

    private boolean isHittingWall(double nextX, double nextY, double nextZ, Store<EntityStore> store) {
        World world = store.getExternalData().getWorld();
        if (world == null) return false;

        BlockType blockType = world.getBlockType((int) Math.floor(nextX), (int) Math.floor(nextY), (int) Math.floor(nextZ));
        if (blockType == null) return false;

        String id = blockType.getId().toLowerCase();
        if (id.contains("air") || id.contains("water") || id.contains("empty")) {
            return false;
        }

        if (id.startsWith("plant_")) {
            return false;
        }

        return true;
    }
}