/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors. All rights reserved.
 */

package coffee.client.feature.config.annotation;

import coffee.client.feature.config.ModuleConfig;
import coffee.client.feature.config.SettingBase;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class AnnotationParser {
    final Object inst;
    final ModuleConfig config;

    public AnnotationParser(Object instance, ModuleConfig mc) {
        this.inst = instance;
        this.config = mc;
    }

    public void runParse() throws Exception {
        for (Field declaredField : inst.getClass().getDeclaredFields()) {
            Setting annotation = null;
            for (Annotation declaredAnnotation : declaredField.getDeclaredAnnotations()) {
                if (declaredAnnotation instanceof Setting setting) {
                    annotation = setting;
                    break;
                }
            }
            if (annotation == null) {
                continue;
            }
            declaredField.setAccessible(true);
            Object defaultValue = declaredField.get(inst);
            if (defaultValue == null) {
                throw new NullPointerException("Field annotated with @Setting needs a value as default");
            }
            SettingType typeToParse = null;
            for (SettingType value : SettingType.values()) {
                if (value.getAcceptedType().isAssignableFrom(declaredField.getType())) {
                    typeToParse = value;
                    break;
                }
            }
            if (typeToParse == null) {
                throw new IllegalArgumentException("Type " + declaredField.getType().getName() + " is not recognized as setting type");
            }
            String settingName = annotation.name();
            SettingBase.Builder<?, ?, ?> base = typeToParse.getProvider().getExtern(annotation, declaredField, inst);
            SettingBase<?> b = base.name(annotation.name()).description(annotation.description()).onChanged(o -> {
                try {
                    declaredField.set(inst, o);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                    throw new RuntimeException(e);
                }
            }).get();
            for (Method declaredMethod : inst.getClass().getDeclaredMethods()) {
                for (Annotation declaredAnnotation : declaredMethod.getDeclaredAnnotations()) {
                    System.out.println(declaredAnnotation);
                    if (declaredAnnotation instanceof VisibilitySpecifier visSpec) {
                        parseAnnotation(visSpec, declaredMethod, b);
                    } else if (declaredAnnotation instanceof VisibilitySpecifiers visSpecs) {
                        for (VisibilitySpecifier visibilitySpecifier : visSpecs.value()) {
                            parseAnnotation(visibilitySpecifier, declaredMethod, b);
                        }
                    }
                }
            }
            config.create(b);
        }
    }

    private void parseAnnotation(VisibilitySpecifier spec, Method declaredMethod, SettingBase<?> base) {
        if (declaredMethod.getParameterCount() != 0 || declaredMethod.getReturnType() != boolean.class) {
            throw new RuntimeException("Invalid visibility specifier for " + base.getName() + ": " + declaredMethod);
        }
        if (spec.value().equals(base.getName())) {
            declaredMethod.setAccessible(true);
            base.showIf(() -> {
                try {
                    return (boolean) declaredMethod.invoke(inst);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }
}
