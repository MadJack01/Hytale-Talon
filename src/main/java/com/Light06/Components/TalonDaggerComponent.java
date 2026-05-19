package com.Light06.Components;

import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nullable;


public class TalonDaggerComponent implements Component<EntityStore> {
    public enum DaggerState {
        OUTWARD,
        HOVERING,
        HOMING
    }

    public DaggerState state = DaggerState.OUTWARD;
    public Ref<EntityStore> ownerRef = null;
    public Ref<EntityStore> targetRef = null;

    public double px, py, pz;
    public double vx, vy, vz;

    public float damage = 15.0f;
    public float stateTimer = 0.0f;
    public boolean isReturningToOwner = false;

    public double speed = 40.0;
    public double turnRate = 12.0;

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
        copy.stateTimer = this.stateTimer;
        copy.isReturningToOwner = this.isReturningToOwner;
        copy.speed = this.speed;
        copy.turnRate = this.turnRate;
        return copy;
    }
}
