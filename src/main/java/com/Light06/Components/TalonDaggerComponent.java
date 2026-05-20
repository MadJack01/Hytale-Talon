package com.Light06.Components;

import com.Light06.TalonPlugin;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Set;

public class TalonDaggerComponent implements Component<EntityStore> {
    public enum DaggerState {
        OUTWARD,
        HOVERING,
        HOMING
    }

    public DaggerState state = DaggerState.OUTWARD;
    public Ref<EntityStore> ownerRef = null;
    public Ref<EntityStore> targetRef = null;

    public Set<Ref<EntityStore>> hitEntities = new HashSet<>();

    public double px, py, pz;
    public double vx, vy, vz;

    public double lastTx, lastTy, lastTz;
    public boolean hasLastTarget = false;

    public float damage = 15.0f;
    public long stateStartTime = 0L;
    public boolean isReturningToOwner = false;

    public double speed = 40.0;
    public double turnRate = 12.0;

    public static ComponentType<EntityStore, TalonDaggerComponent> getComponentType() {
        return TalonPlugin.get().getTalonDaggerComponentType();
    }

    public TalonDaggerComponent() {}

    public TalonDaggerComponent(double px, double py, double pz, double vx, double vy, double vz, Ref<EntityStore> ownerRef, float damage) {
        this.px = px;
        this.py = py;
        this.pz = pz;
        this.vx = vx;
        this.vy = vy;
        this.vz = vz;
        this.ownerRef = ownerRef;
        this.damage = damage;
    }

    @Override
    public @Nullable Component<EntityStore> clone() {
        TalonDaggerComponent copy = new TalonDaggerComponent(px, py, pz, vx, vy, vz, ownerRef, damage);
        copy.state = this.state;
        copy.targetRef = this.targetRef;
        copy.stateStartTime = this.stateStartTime;
        copy.isReturningToOwner = this.isReturningToOwner;
        copy.speed = this.speed;
        copy.turnRate = this.turnRate;
        copy.hitEntities = new HashSet<>(this.hitEntities);
        copy.lastTx = this.lastTx;
        copy.lastTy = this.lastTy;
        copy.lastTz = this.lastTz;
        copy.hasLastTarget = this.hasLastTarget;

        return copy;
    }
}
