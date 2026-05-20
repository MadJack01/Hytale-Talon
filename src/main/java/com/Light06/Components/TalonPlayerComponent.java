package com.Light06.Components;

import com.Light06.TalonPlugin;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nullable;

public class TalonPlayerComponent implements Component<EntityStore> {
    public Ref<EntityStore> lockedTarget = null;
    public boolean IsActive = true;

    public long castTime = 0L;
    public long hoverEndTime = 0L;
    public long outwardTime = 400L;


    public static ComponentType<EntityStore, TalonPlayerComponent> getComponentType() {
        return TalonPlugin.get().getTalonPlayerComponentType();
    }

    public TalonPlayerComponent() {}

    @Override
    public @Nullable Component<EntityStore> clone() {
        TalonPlayerComponent copy = new TalonPlayerComponent();
        copy.lockedTarget = this.lockedTarget;
        copy.IsActive = this.IsActive;
        copy.castTime = this.castTime;
        copy.hoverEndTime = this.hoverEndTime;
        copy.outwardTime = this.outwardTime;
        return copy;
    }
    public boolean isDaggerHovering(long now) {
        return now >= this.castTime + outwardTime;
    }
}

