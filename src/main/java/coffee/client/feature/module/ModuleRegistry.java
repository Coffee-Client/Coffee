/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors.
 * Some rights reserved, refer to LICENSE file.
 */

package coffee.client.feature.module;


import coffee.client.CoffeeMain;
import coffee.client.feature.addon.Addon;
import coffee.client.feature.module.impl.combat.AimAssist;
import coffee.client.feature.module.impl.combat.AutoAttack;
import coffee.client.feature.module.impl.combat.AutoTrap;
import coffee.client.feature.module.impl.combat.Criticals;
import coffee.client.feature.module.impl.combat.FireballDeflector;
import coffee.client.feature.module.impl.combat.Killaura;
import coffee.client.feature.module.impl.combat.Reach;
import coffee.client.feature.module.impl.combat.ShulkerDeflector;
import coffee.client.feature.module.impl.combat.TpRange;
import coffee.client.feature.module.impl.combat.Velocity;
import coffee.client.feature.module.impl.exploit.AntiAntiXray;
import coffee.client.feature.module.impl.exploit.AntiRDI;
import coffee.client.feature.module.impl.exploit.BoatCrash;
import coffee.client.feature.module.impl.exploit.BrandSpoof;
import coffee.client.feature.module.impl.exploit.CarpetBomb;
import coffee.client.feature.module.impl.exploit.ChunkCrash;
import coffee.client.feature.module.impl.exploit.Girlboss;
import coffee.client.feature.module.impl.exploit.InstaBow;
import coffee.client.feature.module.impl.exploit.LecternCrash;
import coffee.client.feature.module.impl.exploit.OffhandCrash;
import coffee.client.feature.module.impl.exploit.PingSpoof;
import coffee.client.feature.module.impl.misc.AdBlock;
import coffee.client.feature.module.impl.misc.AllowFormatCodes;
import coffee.client.feature.module.impl.misc.AntiCrash;
import coffee.client.feature.module.impl.misc.AntiOffhandCrash;
import coffee.client.feature.module.impl.misc.AntiPacketKick;
import coffee.client.feature.module.impl.misc.ClientSettings;
import coffee.client.feature.module.impl.misc.Debugger;
import coffee.client.feature.module.impl.misc.DiscordRPC;
import coffee.client.feature.module.impl.misc.GamemodeAlert;
import coffee.client.feature.module.impl.misc.InfChatLength;
import coffee.client.feature.module.impl.misc.MoreChatHistory;
import coffee.client.feature.module.impl.misc.NoTitles;
import coffee.client.feature.module.impl.misc.PortalGUI;
import coffee.client.feature.module.impl.misc.SpinAutism;
import coffee.client.feature.module.impl.misc.Test;
import coffee.client.feature.module.impl.misc.Timer;
import coffee.client.feature.module.impl.misc.XCarry;
import coffee.client.feature.module.impl.movement.AirJump;
import coffee.client.feature.module.impl.movement.AntiAnvil;
import coffee.client.feature.module.impl.movement.AutoElytra;
import coffee.client.feature.module.impl.movement.Backtrack;
import coffee.client.feature.module.impl.movement.Blink;
import coffee.client.feature.module.impl.movement.BlocksMCFlight;
import coffee.client.feature.module.impl.movement.BoatPhase;
import coffee.client.feature.module.impl.movement.BoingBoing;
import coffee.client.feature.module.impl.movement.Boost;
import coffee.client.feature.module.impl.movement.EdgeJump;
import coffee.client.feature.module.impl.movement.EdgeSneak;
import coffee.client.feature.module.impl.movement.ElytraFly;
import coffee.client.feature.module.impl.movement.EntityFly;
import coffee.client.feature.module.impl.movement.Flight;
import coffee.client.feature.module.impl.movement.IgnoreWorldBorder;
import coffee.client.feature.module.impl.movement.InventoryWalk;
import coffee.client.feature.module.impl.movement.Jesus;
import coffee.client.feature.module.impl.movement.LongJump;
import coffee.client.feature.module.impl.movement.MoonGravity;
import coffee.client.feature.module.impl.movement.NoFall;
import coffee.client.feature.module.impl.movement.NoJumpCool;
import coffee.client.feature.module.impl.movement.NoLevitation;
import coffee.client.feature.module.impl.movement.NoPush;
import coffee.client.feature.module.impl.movement.NoSlow;
import coffee.client.feature.module.impl.movement.Phase;
import coffee.client.feature.module.impl.movement.Slippy;
import coffee.client.feature.module.impl.movement.Sprint;
import coffee.client.feature.module.impl.movement.Step;
import coffee.client.feature.module.impl.movement.Swing;
import coffee.client.feature.module.impl.movement.VanillaSpeed;
import coffee.client.feature.module.impl.render.BlockHighlighting;
import coffee.client.feature.module.impl.render.CaveMapper;
import coffee.client.feature.module.impl.render.ChestHighlighter;
import coffee.client.feature.module.impl.render.ClickGUI;
import coffee.client.feature.module.impl.render.ESP;
import coffee.client.feature.module.impl.render.FakeHacker;
import coffee.client.feature.module.impl.render.FreeLook;
import coffee.client.feature.module.impl.render.Freecam;
import coffee.client.feature.module.impl.render.Fullbright;
import coffee.client.feature.module.impl.render.Hud;
import coffee.client.feature.module.impl.render.ItemByteSize;
import coffee.client.feature.module.impl.render.MouseEars;
import coffee.client.feature.module.impl.render.NameTags;
import coffee.client.feature.module.impl.render.NoLiquidFog;
import coffee.client.feature.module.impl.render.NoMessageIndicators;
import coffee.client.feature.module.impl.render.Radar;
import coffee.client.feature.module.impl.render.ShowTntPrime;
import coffee.client.feature.module.impl.render.Spotlight;
import coffee.client.feature.module.impl.render.SuperheroFX;
import coffee.client.feature.module.impl.render.TabGui;
import coffee.client.feature.module.impl.render.TargetHud;
import coffee.client.feature.module.impl.render.Tracers;
import coffee.client.feature.module.impl.render.Trail;
import coffee.client.feature.module.impl.render.Waypoints;
import coffee.client.feature.module.impl.render.Zoom;
import coffee.client.feature.module.impl.world.AirPlace;
import coffee.client.feature.module.impl.world.Annihilator;
import coffee.client.feature.module.impl.world.AnyPlacer;
import coffee.client.feature.module.impl.world.AutoFish;
import coffee.client.feature.module.impl.world.AutoIgnite;
import coffee.client.feature.module.impl.world.AutoLavacast;
import coffee.client.feature.module.impl.world.AutoSign;
import coffee.client.feature.module.impl.world.AutoTNT;
import coffee.client.feature.module.impl.world.AutoTool;
import coffee.client.feature.module.impl.world.BlockTagViewer;
import coffee.client.feature.module.impl.world.Boom;
import coffee.client.feature.module.impl.world.Decimator;
import coffee.client.feature.module.impl.world.FastUse;
import coffee.client.feature.module.impl.world.Flattener;
import coffee.client.feature.module.impl.world.GodBridge;
import coffee.client.feature.module.impl.world.InstantBreak;
import coffee.client.feature.module.impl.world.MassUse;
import coffee.client.feature.module.impl.world.NoBreakDelay;
import coffee.client.feature.module.impl.world.Nuker;
import coffee.client.feature.module.impl.world.Scaffold;
import coffee.client.feature.module.impl.world.SurvivalNuker;
import coffee.client.feature.module.impl.world.XRAY;
import org.apache.logging.log4j.Level;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class ModuleRegistry {
    static final List<Module> vanillaModules = new ArrayList<>();
    static final List<AddonModuleEntry> customModules = new ArrayList<>();
    static final List<Module> sharedModuleList = new ArrayList<>();
    static final AtomicBoolean reloadInProgress = new AtomicBoolean(false);
    static final AtomicBoolean initialized = new AtomicBoolean(false);
    static final Map<Class<? extends Module>, Module> cachedModuleClassMap = new ConcurrentHashMap<>();

    public static List<AddonModuleEntry> getCustomModules() {
        return customModules;
    }

    public static void registerAddonModule(Addon source, Module module) {
        for (AddonModuleEntry customModule : customModules) {
            if (customModule.module.getClass() == module.getClass()) {
                throw new IllegalStateException("Module " + module.getClass().getSimpleName() + " already registered");
            }
        }
        customModules.add(new AddonModuleEntry(source, module));
        rebuildSharedModuleList();
    }

    public static void clearCustomModules(Addon addon) {
        for (AddonModuleEntry customModule : customModules) {
            if (customModule.addon == addon && customModule.module.isEnabled()) {
                customModule.module.setEnabled(false);
            }
        }
        customModules.removeIf(addonModuleEntry -> addonModuleEntry.addon == addon);
        rebuildSharedModuleList();
    }

    private static void rebuildSharedModuleList() {
        awaitLockOpen();
        reloadInProgress.set(true);
        sharedModuleList.clear();
        cachedModuleClassMap.clear();
        sharedModuleList.addAll(vanillaModules);
        for (AddonModuleEntry customModule : customModules) {
            sharedModuleList.add(customModule.module);
        }
        reloadInProgress.set(false);
    }

    public static void init() {
        try {
            initInner();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void registerModule(Class<? extends Module> moduleClass) {
        Module instance = null;
        for (Constructor<?> declaredConstructor : moduleClass.getDeclaredConstructors()) {
            if (declaredConstructor.getParameterCount() != 0) {
                throw new IllegalArgumentException(moduleClass.getName() + " has invalid constructor: expected " + moduleClass.getName() + "(), got " + declaredConstructor);
            }
            try {
                instance = (Module) declaredConstructor.newInstance();
            } catch (Exception e) {
                throw new IllegalArgumentException("Failed to make instance of " + moduleClass.getName(), e);
            }
        }
        if (instance == null) {
            throw new IllegalArgumentException("Failed to make instance of " + moduleClass.getName());
        }
        //CoffeeMain.log(Level.INFO, "Initialized " + instance.getName() + " via " + moduleClass.getName());
        vanillaModules.add(instance);
    }

    private static void initInner() {
        if (initialized.get()) {
            return;
        }
        initialized.set(true);
        vanillaModules.clear();

        registerModule(Flight.class);
        registerModule(Sprint.class);
        registerModule(Fullbright.class);
        registerModule(Hud.class);
        registerModule(TargetHud.class);
        registerModule(AntiOffhandCrash.class);
        registerModule(AntiPacketKick.class);
        registerModule(AntiRDI.class);
        registerModule(BoatPhase.class);
        registerModule(BoatCrash.class);
        registerModule(Boom.class);
        registerModule(CaveMapper.class);
        registerModule(InstaBow.class);
        registerModule(ChunkCrash.class);
        registerModule(OffhandCrash.class);
        registerModule(Phase.class);
        registerModule(BrandSpoof.class);
        registerModule(XRAY.class);
        registerModule(Decimator.class);
        registerModule(ClickGUI.class);
        registerModule(TpRange.class);
        registerModule(AnyPlacer.class);
        registerModule(FireballDeflector.class);
        registerModule(ShulkerDeflector.class);
        registerModule(CarpetBomb.class);
        registerModule(AutoTrap.class);
        registerModule(AutoTNT.class);
        registerModule(FakeHacker.class);
        registerModule(NoFall.class);
        registerModule(ESP.class);
        registerModule(Tracers.class);
        registerModule(VanillaSpeed.class);
        registerModule(AntiAnvil.class);
        registerModule(Swing.class);
        registerModule(AimAssist.class);
        registerModule(Criticals.class);
        registerModule(Killaura.class);
        registerModule(Velocity.class);
        registerModule(AntiAntiXray.class);
        registerModule(PingSpoof.class);
        registerModule(AutoAttack.class);
        registerModule(MouseEars.class);
        registerModule(SpinAutism.class);
        registerModule(AllowFormatCodes.class);
        registerModule(InfChatLength.class);
        registerModule(NoTitles.class);
        registerModule(PortalGUI.class);
        registerModule(Timer.class);
        registerModule(XCarry.class);
        registerModule(AirJump.class);
        registerModule(AutoElytra.class);
        registerModule(Blink.class);
        registerModule(Boost.class);
        registerModule(EdgeJump.class);
        registerModule(EdgeSneak.class);
        registerModule(EntityFly.class);
        registerModule(IgnoreWorldBorder.class);
        registerModule(InventoryWalk.class);
        registerModule(Jesus.class);
        registerModule(LongJump.class);
        registerModule(MoonGravity.class);
        registerModule(NoJumpCool.class);
        registerModule(NoLevitation.class);
        registerModule(NoPush.class);
        registerModule(Step.class);
        registerModule(Freecam.class);
        registerModule(FreeLook.class);
        registerModule(ItemByteSize.class);
        registerModule(Zoom.class);
        registerModule(AutoTool.class);
        registerModule(BlockTagViewer.class);
        registerModule(Annihilator.class);
        registerModule(FastUse.class);
        registerModule(Flattener.class);
        registerModule(GodBridge.class);
        registerModule(InstantBreak.class);
        registerModule(MassUse.class);
        registerModule(NoBreakDelay.class);
        registerModule(SurvivalNuker.class);
        registerModule(Nuker.class);
        registerModule(Scaffold.class);
        registerModule(Test.class);
        registerModule(BlocksMCFlight.class);
        registerModule(NameTags.class);
        registerModule(Trail.class);
        registerModule(AdBlock.class);
        registerModule(AutoLavacast.class);
        registerModule(Backtrack.class);
        registerModule(TabGui.class);
        registerModule(AntiCrash.class);
        registerModule(ClientSettings.class);
        registerModule(NoLiquidFog.class);
        registerModule(Spotlight.class);
        registerModule(ShowTntPrime.class);
        registerModule(BlockHighlighting.class);
        registerModule(AutoIgnite.class);
        registerModule(DiscordRPC.class);
        registerModule(AirPlace.class);
        registerModule(AutoFish.class);
        registerModule(Reach.class);
        registerModule(AutoSign.class);
        registerModule(ChestHighlighter.class);
        registerModule(MoreChatHistory.class);
        registerModule(Radar.class);
        registerModule(SuperheroFX.class);
        registerModule(ElytraFly.class);
        registerModule(BoingBoing.class);
        registerModule(Slippy.class);
        registerModule(Girlboss.class);
        registerModule(Waypoints.class);
        registerModule(NoMessageIndicators.class);
        registerModule(Debugger.class);
        registerModule(NoSlow.class);
        registerModule(GamemodeAlert.class);
        registerModule(LecternCrash.class);

        rebuildSharedModuleList();

        for (Module module : getModules()) {
            module.postModuleInit();
        }
        CoffeeMain.log(Level.INFO, "Initialized modules. Vanilla modules:", vanillaModules.size(), "Addon modules:", customModules.size());
    }

    public static List<Module> getModules() {
        if (!initialized.get()) {
            init();
        }
        awaitLockOpen();
        return sharedModuleList;
    }

    private static void awaitLockOpen() {
        if (reloadInProgress.get()) {
            CoffeeMain.log(Level.INFO, "Locking for some time for reload to complete");
            long lockStart = System.currentTimeMillis();
            long lockStartns = System.nanoTime();
            while (reloadInProgress.get()) {
                Thread.onSpinWait();
            }
            CoffeeMain.log(Level.INFO, "Lock opened within " + (System.currentTimeMillis() - lockStart) + " ms (" + (System.nanoTime() - lockStartns) + " ns)");
        }

    }

    @SuppressWarnings("unchecked")
    public static <T extends Module> T getByClass(Class<T> clazz) {
        if (!initialized.get()) {
            init();
        }
        if (cachedModuleClassMap.containsKey(clazz)) {
            return (T) cachedModuleClassMap.get(clazz);
        }
        awaitLockOpen();
        for (Module module : getModules()) {
            if (module.getClass() == clazz) {
                cachedModuleClassMap.put(clazz, module);
                return (T) module;
            }
        }
        throw new IllegalStateException("Unregistered module: " + clazz.getName());
    }

    public static Module getByName(String n) {
        if (!initialized.get()) {
            init();
        }
        awaitLockOpen();
        for (Module module : getModules()) {
            if (module.getName().equalsIgnoreCase(n)) {
                return module;
            }
        }
        return null;
    }

    public record AddonModuleEntry(Addon addon, Module module) {
    }
}
