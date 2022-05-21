/*
 * Copyright (c) 2022 Coffee client, 0x150 and contributors. See copyright file in project root.
 */

package cf.coffee.client.helper.event;

import cf.coffee.client.CoffeeMain;
import cf.coffee.client.helper.event.events.base.Event;
import org.apache.logging.log4j.Level;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class Events {
    static final List<ListenerEntry> entries = new CopyOnWriteArrayList<>();

    public static ListenerEntry registerEventHandler(int uniqueId, EventType event, Consumer<? extends Event> handler) {
        return registerEventHandler(uniqueId, event, handler, Events.class);
    }

    public static ListenerEntry registerEventHandler(int uniqueId, EventType event, Consumer<? extends Event> handler, Class<?> owner) {
        if (entries.stream().noneMatch(listenerEntry -> listenerEntry.id == uniqueId)) {
            ListenerEntry le = new ListenerEntry(uniqueId, event, handler, owner);
            entries.add(le);
            return le;
        } else {
            CoffeeMain.log(Level.WARN, uniqueId + " tried to register " + event.name() + " multiple times, unregistering previous and adding new");
            unregister(uniqueId);
            return registerEventHandler(uniqueId, event, handler, owner); // yes this is recursive and no this will not repeat again because we unregistered
        }
    }

    public static void unregister(int id) {
        entries.removeIf(listenerEntry -> listenerEntry.id == id);
    }

    public static ListenerEntry registerEventHandler(EventType event, Consumer<? extends Event> handler) {
        return registerEventHandler((int) Math.floor(Math.random() * 0xFFFFFF), event, handler);
    }

    public static void unregisterEventHandlerClass(Object instance) {
        for (ListenerEntry entry : new ArrayList<>(entries)) {
            if (entry.owner.equals(instance.getClass())) {
                CoffeeMain.log(Level.INFO, "Unregistering " + entry.type + ":" + entry.id);
                entries.remove(entry);
            }
        }
    }

    public static void registerEventHandlerClass(Object instance) {
        for (Method declaredMethod : instance.getClass().getDeclaredMethods()) {
            for (Annotation declaredAnnotation : declaredMethod.getDeclaredAnnotations()) {
                if (declaredAnnotation.annotationType() == EventListener.class) {
                    EventListener ev = (EventListener) declaredAnnotation;
                    Class<?>[] params = declaredMethod.getParameterTypes();
                    if (params.length != 1 || !params[0].isAssignableFrom(ev.type().getExpectedType())) {
                        throw new IllegalArgumentException(String.format("Invalid signature: Expected %s.%s(%s) -> void, got %s.%s(%s) -> %s. Listener: %s", instance.getClass()
                                .getSimpleName(), declaredMethod.getName(), ev.type()
                                .getExpectedType()
                                .getSimpleName(), instance.getClass()
                                .getSimpleName(), declaredMethod.getName(), Arrays.stream(params)
                                .map(Class::getSimpleName)
                                .collect(Collectors.joining(", ")), declaredMethod.getReturnType().getName(), ev.type()
                                .name()));
                    } else {
                        declaredMethod.setAccessible(true);

                        ListenerEntry l = registerEventHandler((instance.getClass()
                                .getName() + declaredMethod.getName()).hashCode(), ev.type(), event -> {
                            try {
                                declaredMethod.invoke(instance, event);
                            } catch (IllegalAccessException | InvocationTargetException e) {
                                e.printStackTrace();
                            }
                        }, instance.getClass());
                        CoffeeMain.log(Level.INFO, "Registered event handler " + declaredMethod + " with id " + l.id);
                    }
                }
            }
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static boolean fireEvent(EventType event, Event argument) {
        if (!event.getExpectedType().equals(argument.getClass()))
            throw new IllegalArgumentException(String.format("Attempted to invoke event %s with %s as event data, expected %s", event.name(), argument.getClass()
                    .getName(), event.getExpectedType().getName()));
        List<ListenerEntry> le = entries.stream().filter(listenerEntry -> listenerEntry.type == event).toList();
        if (le.size() == 0) {
            return false;
        }
        for (ListenerEntry entry : le) {
            ((Consumer) entry.eventListener()).accept(argument);
        }
        return argument.isCancelled();
    }

    record ListenerEntry(int id, EventType type, Consumer<? extends Event> eventListener, Class<?> owner) {
    }
}
