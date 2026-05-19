package com.Light06.Components;

import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nullable;

public class TalonPlayerComponent implements Component<EntityStore> {
    public Ref<EntityStore> lockedTarget = null;
    public float remainingHoverTime = 10.0f;
    public boolean IsActive = true;

    public TalonPlayerComponent() {}

    @Override
    public @Nullable Component<EntityStore> clone() {
        TalonPlayerComponent copy = new TalonPlayerComponent();
        copy.lockedTarget = this.lockedTarget;
        copy.remainingHoverTime = this.remainingHoverTime;
        copy.IsActive = this.IsActive;
        return copy;
    }
}
