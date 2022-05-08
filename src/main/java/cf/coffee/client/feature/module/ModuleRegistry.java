/*
 * Copyright (c) 2022 Coffee client, 0x150 and contributors. See copyright file in project root.
 */

package cf.coffee.client.feature.module;


import cf.coffee.client.CoffeeMain;
import cf.coffee.client.feature.addon.Addon;
import cf.coffee.client.feature.module.impl.combat.AimAssist;
import cf.coffee.client.feature.module.impl.combat.AutoAttack;
import cf.coffee.client.feature.module.impl.combat.AutoTrap;
import cf.coffee.client.feature.module.impl.combat.Criticals;
import cf.coffee.client.feature.module.impl.combat.FireballDeflector;
import cf.coffee.client.feature.module.impl.combat.Killaura;
import cf.coffee.client.feature.module.impl.combat.Reach;
import cf.coffee.client.feature.module.impl.combat.ShulkerDeflector;
import cf.coffee.client.feature.module.impl.combat.TpRange;
import cf.coffee.client.feature.module.impl.combat.Velocity;
import cf.coffee.client.feature.module.impl.exploit.AntiAntiXray;
import cf.coffee.client.feature.module.impl.exploit.AntiRDI;
import cf.coffee.client.feature.module.impl.exploit.BoatCrash;
import cf.coffee.client.feature.module.impl.exploit.BrandSpoof;
import cf.coffee.client.feature.module.impl.exploit.CarpetBomb;
import cf.coffee.client.feature.module.impl.exploit.ChunkCrash;
import cf.coffee.client.feature.module.impl.exploit.InstaBow;
import cf.coffee.client.feature.module.impl.exploit.OffhandCrash;
import cf.coffee.client.feature.module.impl.exploit.PingSpoof;
import cf.coffee.client.feature.module.impl.grief.Annihilator;
import cf.coffee.client.feature.module.impl.grief.AutoIgnite;
import cf.coffee.client.feature.module.impl.grief.AutoTNT;
import cf.coffee.client.feature.module.impl.grief.Decimator;
import cf.coffee.client.feature.module.impl.misc.AdBlock;
import cf.coffee.client.feature.module.impl.misc.AllowFormatCodes;
import cf.coffee.client.feature.module.impl.misc.AntiCrash;
import cf.coffee.client.feature.module.impl.misc.AntiOffhandCrash;
import cf.coffee.client.feature.module.impl.misc.AntiPacketKick;
import cf.coffee.client.feature.module.impl.misc.ClientSettings;
import cf.coffee.client.feature.module.impl.misc.DiscordRPC;
import cf.coffee.client.feature.module.impl.misc.InfChatLength;
import cf.coffee.client.feature.module.impl.misc.MoreChatHistory;
import cf.coffee.client.feature.module.impl.misc.NoTitles;
import cf.coffee.client.feature.module.impl.misc.PortalGUI;
import cf.coffee.client.feature.module.impl.misc.SpinAutism;
import cf.coffee.client.feature.module.impl.misc.Test;
import cf.coffee.client.feature.module.impl.misc.Timer;
import cf.coffee.client.feature.module.impl.misc.XCarry;
import cf.coffee.client.feature.module.impl.movement.AirJump;
import cf.coffee.client.feature.module.impl.movement.AntiAnvil;
import cf.coffee.client.feature.module.impl.movement.AutoElytra;
import cf.coffee.client.feature.module.impl.movement.Backtrack;
import cf.coffee.client.feature.module.impl.movement.Blink;
import cf.coffee.client.feature.module.impl.movement.BlocksMCFlight;
import cf.coffee.client.feature.module.impl.movement.BoatPhase;
import cf.coffee.client.feature.module.impl.movement.Boost;
import cf.coffee.client.feature.module.impl.movement.ClickTP;
import cf.coffee.client.feature.module.impl.movement.EdgeJump;
import cf.coffee.client.feature.module.impl.movement.EdgeSneak;
import cf.coffee.client.feature.module.impl.movement.EntityFly;
import cf.coffee.client.feature.module.impl.movement.Flight;
import cf.coffee.client.feature.module.impl.movement.IgnoreWorldBorder;
import cf.coffee.client.feature.module.impl.movement.InventoryWalk;
import cf.coffee.client.feature.module.impl.movement.Jesus;
import cf.coffee.client.feature.module.impl.movement.LongJump;
import cf.coffee.client.feature.module.impl.movement.MoonGravity;
import cf.coffee.client.feature.module.impl.movement.NoFall;
import cf.coffee.client.feature.module.impl.movement.NoJumpCool;
import cf.coffee.client.feature.module.impl.movement.NoLevitation;
import cf.coffee.client.feature.module.impl.movement.NoPush;
import cf.coffee.client.feature.module.impl.movement.Phase;
import cf.coffee.client.feature.module.impl.movement.Sprint;
import cf.coffee.client.feature.module.impl.movement.Step;
import cf.coffee.client.feature.module.impl.movement.Swing;
import cf.coffee.client.feature.module.impl.movement.VanillaSpeed;
import cf.coffee.client.feature.module.impl.render.BlockHighlighting;
import cf.coffee.client.feature.module.impl.render.CaveMapper;
import cf.coffee.client.feature.module.impl.render.ChestHighlighter;
import cf.coffee.client.feature.module.impl.render.ClickGUI;
import cf.coffee.client.feature.module.impl.render.ESP;
import cf.coffee.client.feature.module.impl.render.FakeHacker;
import cf.coffee.client.feature.module.impl.render.FreeLook;
import cf.coffee.client.feature.module.impl.render.Freecam;
import cf.coffee.client.feature.module.impl.render.Fullbright;
import cf.coffee.client.feature.module.impl.render.Hud;
import cf.coffee.client.feature.module.impl.render.ItemByteSize;
import cf.coffee.client.feature.module.impl.render.MouseEars;
import cf.coffee.client.feature.module.impl.render.NameTags;
import cf.coffee.client.feature.module.impl.render.NoLiquidFog;
import cf.coffee.client.feature.module.impl.render.Radar;
import cf.coffee.client.feature.module.impl.render.ShowTntPrime;
import cf.coffee.client.feature.module.impl.render.Spotlight;
import cf.coffee.client.feature.module.impl.render.TabGui;
import cf.coffee.client.feature.module.impl.render.TargetHud;
import cf.coffee.client.feature.module.impl.render.Theme;
import cf.coffee.client.feature.module.impl.render.Tracers;
import cf.coffee.client.feature.module.impl.render.Trail;
import cf.coffee.client.feature.module.impl.render.Zoom;
import cf.coffee.client.feature.module.impl.world.AirPlace;
import cf.coffee.client.feature.module.impl.world.AnyPlacer;
import cf.coffee.client.feature.module.impl.world.AutoFish;
import cf.coffee.client.feature.module.impl.world.AutoLavacast;
import cf.coffee.client.feature.module.impl.world.AutoSign;
import cf.coffee.client.feature.module.impl.world.AutoTool;
import cf.coffee.client.feature.module.impl.world.BlockTagViewer;
import cf.coffee.client.feature.module.impl.world.Boom;
import cf.coffee.client.feature.module.impl.world.FastUse;
import cf.coffee.client.feature.module.impl.world.Flattener;
import cf.coffee.client.feature.module.impl.world.GodBridge;
import cf.coffee.client.feature.module.impl.world.InstantBreak;
import cf.coffee.client.feature.module.impl.world.MassUse;
import cf.coffee.client.feature.module.impl.world.NoBreakDelay;
import cf.coffee.client.feature.module.impl.world.Nuker;
import cf.coffee.client.feature.module.impl.world.Scaffold;
import cf.coffee.client.feature.module.impl.world.SurvivalNuker;
import cf.coffee.client.feature.module.impl.world.XRAY;
import org.apache.logging.log4j.Level;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class ModuleRegistry {
    static final List<Module> vanillaModules = new ArrayList<>();
    static final List<AddonModuleEntry> customModules = new ArrayList<>();
    static final List<Module> sharedModuleList = new ArrayList<>();
    static final AtomicBoolean reloadInProgress = new AtomicBoolean(false);
    static final AtomicBoolean initialized = new AtomicBoolean(false);

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
            if (customModule.addon == addon && customModule.module.isEnabled()) customModule.module.setEnabled(false);
        }
        customModules.removeIf(addonModuleEntry -> addonModuleEntry.addon == addon);
        rebuildSharedModuleList();
    }

    private static void rebuildSharedModuleList() {
        reloadInProgress.set(true);
        sharedModuleList.clear();
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
        if (initialized.get()) return;
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
        registerModule(Theme.class);
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
        registerModule(ClickTP.class);
        registerModule(ChestHighlighter.class);
        registerModule(MoreChatHistory.class);
        registerModule(Radar.class);

        rebuildSharedModuleList();

        for (Module module : getModules()) {
            module.postModuleInit();
        }
        CoffeeMain.log(Level.INFO, "Initialized modules. Vanilla modules:",vanillaModules.size(),"Addon modules:",customModules.size());
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
        awaitLockOpen();
        for (Module module : getModules()) {
            if (module.getClass() == clazz) {
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
