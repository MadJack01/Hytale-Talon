package com.Light06;


import com.Light06.Components.TalonDaggerComponent;
import com.Light06.Components.TalonPlayerComponent;
import com.Light06.Interactions.TalonUltimateInteraction;
import com.Light06.Systems.TalonDaggerTickingSystem;
import com.Light06.Systems.TalonDamageListenerSystem;
import com.Light06.Systems.TalonTimerSystem;
import com.Light06.Systems.TalonUltimateDamageSystem;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public class TalonPlugin extends JavaPlugin {
    public static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    public static TalonPlugin instance;

    private static ComponentType<EntityStore, TalonPlayerComponent> talonPlayerComponentType;
    private static ComponentType<EntityStore, TalonDaggerComponent> talonDaggerComponentType;

    public TalonPlugin(JavaPluginInit init) {
        super(init);
        instance = this;
    }

    public static TalonPlugin get() {
        return instance;
    }

    public static ComponentType<EntityStore, TalonPlayerComponent> getTalonPlayerComponentType() {
        return talonPlayerComponentType;
    }

    public static ComponentType<EntityStore, TalonDaggerComponent> getTalonDaggerComponentType() {
        return talonDaggerComponentType;
    }

    @Override
    protected void start() {
        getEntityStoreRegistry().registerSystem(new TalonDaggerTickingSystem());
        getEntityStoreRegistry().registerSystem(new TalonTimerSystem());
        getEntityStoreRegistry().registerSystem(new TalonUltimateDamageSystem());



    }

    @Override
    protected void setup() {
        talonPlayerComponentType = getEntityStoreRegistry().registerComponent(TalonPlayerComponent.class, TalonPlayerComponent::new);
        talonDaggerComponentType = getEntityStoreRegistry().registerComponent(TalonDaggerComponent.class, TalonDaggerComponent::new);

        this.getCodecRegistry(Interaction.CODEC).register("TalonUltimateInteraction", TalonUltimateInteraction.class, TalonUltimateInteraction.CODEC);
    }
}