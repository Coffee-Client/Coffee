/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors. All rights reserved.
 */

package coffee.client.feature.module;

import coffee.client.CoffeeMain;
import coffee.client.feature.config.BooleanSetting;
import coffee.client.feature.config.DoubleSetting;
import coffee.client.feature.config.ModuleConfig;
import coffee.client.feature.gui.notifications.Notification;
import coffee.client.helper.event.Events;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;

import java.lang.annotation.Annotation;

public abstract class Module {

    protected static final MinecraftClient client = CoffeeMain.client;
    public final ModuleConfig config;
    public final DoubleSetting keybind;
    private final String name;
    private final String description;
    private final ModuleType moduleType;
    private final BooleanSetting toasts;
    private boolean enabled = false;

    @Getter
    @Setter
    private boolean disabled = false;

    @Getter
    @Setter
    private String disabledReason = "";

    public Module(String n, String d, ModuleType type) {
        this.name = n;
        this.description = d;
        this.moduleType = type;
        this.config = new ModuleConfig();
        this.keybind = this.config.create(new DoubleSetting.Builder(-1).name("Keybind").description("The keybind to toggle the module with").min(-1).max(65535).precision(0).get());
        //        this.keybind.showIf(() -> false);
        boolean hasAnnotation = false;
        for (Annotation declaredAnnotation : this.getClass().getDeclaredAnnotations()) {
            if (declaredAnnotation.annotationType() == NoNotificationDefault.class) {
                hasAnnotation = true;
            }
        }
        this.toasts = this.config.create(new BooleanSetting.Builder(!hasAnnotation).name("Toasts").description("Whether to show enabled / disabled toasts").get());
        Events.registerTransientEventHandlerClassEvents(this);
    }

    public final void postModuleInit() {
        this.config.addSettingsFromAnnotations(this);
    }

    public final ModuleType getModuleType() {
        return moduleType;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }


    public abstract void tick();

    public abstract void enable();

    public abstract void disable();

    public abstract String getContext();

    public abstract void onWorldRender(MatrixStack matrices);

    public abstract void onHudRender();

    public void postInit() {

    }

    public void onFastTick() {

    }

    public void onFastTick_NWC() {

    }

    public void toggle() {
        setEnabled(!enabled);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        if (isDisabled()) {
            this.enabled = false;
            return;
        }
        this.enabled = enabled;
        if (toasts.getValue()) {
            Notification.create(1000, "Module toggle", Notification.Type.INFO, (this.enabled ? "§aEn" : "§cDis") + "abled §r" + this.getName());
        }
        if (this.enabled) {
            Events.registerEventHandlerClass(this);
            this.enable();
        } else {
            this.disable();
            Events.unregisterEventHandlerClass(this);
        }
    }

}
