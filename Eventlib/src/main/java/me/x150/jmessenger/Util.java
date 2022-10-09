/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors.
 * Some rights reserved, refer to LICENSE file.
 */

package me.x150.jmessenger;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class Util {
    private static final Map<Class<?>, String> signatures = make(new HashMap<>(), signatures -> {
        signatures.put(boolean.class, "Z");
        signatures.put(byte.class, "B");
        signatures.put(char.class, "C");
        signatures.put(short.class, "S");
        signatures.put(int.class, "I");
        signatures.put(long.class, "J");
        signatures.put(float.class, "F");
        signatures.put(double.class, "D");
        signatures.put(void.class, "V");
    });

    @SuppressWarnings("unchecked")
    public static <T extends Annotation> T getAnnotationFrom(Method method, Class<T> annotationType) {
        for (Annotation declaredAnnotation : method.getDeclaredAnnotations()) {
            if (annotationType.isInstance(declaredAnnotation)) {
                return (T) declaredAnnotation;
            }
        }
        return null;
    }

    public static <T> T make(T inst, Consumer<T> modifier) {
        modifier.accept(inst);
        return inst;
    }

    private static Class<?> recursiveComponentType(Class<?> t) {
        Class<?> p = t;
        while (p.isArray()) {
            p = p.componentType();
        }
        return p;
    }

    private static int determineArrayDepth(Class<?> t) {
        int a = 0;
        Class<?> p = t;
        while (p.isArray()) {
            a++;
            p = p.componentType();
        }
        return a;
    }

    private static String genericSignatureOf(Class<?> t) {
        Class<?> a = recursiveComponentType(t);
        return String.format("L%s;", a.getName().replaceAll("\\.", "/"));
    }

    private static String signatureOf(Class<?> t) {
        String sig = signatures.containsKey(t) ? signatures.get(t) : genericSignatureOf(t);
        return "[".repeat(determineArrayDepth(t)) + sig;
    }

    public static String signatureOf(Method m) {
        Class<?>[] parameterTypes = m.getParameterTypes();
        String params = Arrays.stream(parameterTypes).map(Util::signatureOf).collect(Collectors.joining());
        String ret = Util.signatureOf(m.getReturnType());
        return String.format("(%s)%s", params, ret);
    }
}
