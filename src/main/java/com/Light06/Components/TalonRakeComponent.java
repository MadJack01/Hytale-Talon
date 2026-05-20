package com.Light06.Components;

import com.Light06.TalonPlugin;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Set;

public class TalonRakeComponent implements Component<EntityStore> {
    public enum RakeState {
        OUTWARD,
        HOVERING,
        RETURNING
    }

    public RakeState state = RakeState.OUTWARD;
    public Ref<EntityStore> ownerRef = null;

    public Set<Ref<EntityStore>> outwardHits = new HashSet<>();
    public Set<Ref<EntityStore>> returnHits = new HashSet<>();

    public double px, py, pz;
    public double vx, vy, vz;

    public float damage = 20.0f;
    public long stateStartTime = 0L;

    public double returnSpeed = 35.0;
    public double turnRate = 15.0;

    public static ComponentType<EntityStore, TalonRakeComponent> getComponentType() {
        return TalonPlugin.get().getTalonRakeComponentType();
    }

    public TalonRakeComponent() {}

    public TalonRakeComponent(double px, double py, double pz, double vx, double vy, double vz, Ref<EntityStore> ownerRef, float damage) {
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
        TalonRakeComponent copy = new TalonRakeComponent(px, py, pz, vx, vy, vz, ownerRef, damage);
        copy.state = this.state;
        copy.stateStartTime = this.stateStartTime;
        copy.returnSpeed = this.returnSpeed;
        copy.turnRate = this.turnRate;

        // Copy both sets
        copy.outwardHits = new HashSet<>(this.outwardHits);
        copy.returnHits = new HashSet<>(this.returnHits);
        return copy;
    }
}