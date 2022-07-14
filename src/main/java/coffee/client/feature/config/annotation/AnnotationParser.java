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
                    throw new RuntimeException(e);
                }
            }).get();
            for (Method declaredMethod : inst.getClass().getDeclaredMethods()) {
                //                System.out.println(declaredMethod.toString());
                for (Annotation declaredAnnotation : declaredMethod.getDeclaredAnnotations()) {
                    if (declaredAnnotation instanceof VisibilitySpecifier visSpec) {
                        if (declaredMethod.getParameterCount() != 0 || declaredMethod.getReturnType() != boolean.class) {
                            throw new RuntimeException("Invalid visibility specifier for " + settingName + ": " + declaredMethod);
                        }
                        if (visSpec.value().equals(settingName)) {
                            declaredMethod.setAccessible(true);
                            b.showIf(() -> {
                                try {
                                    return (boolean) declaredMethod.invoke(inst);
                                } catch (IllegalAccessException | InvocationTargetException e) {
                                    throw new RuntimeException(e);
                                }
                            });
                        }
                    }
                }
            }
            config.create(b);
        }
    }
}
