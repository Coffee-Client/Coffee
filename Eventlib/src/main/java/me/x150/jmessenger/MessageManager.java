/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors.
 * Some rights reserved, refer to LICENSE file.
 */

package me.x150.jmessenger;

import me.x150.jmessenger.exception.InvalidSubscriberException;
import me.x150.jmessenger.exception.SubscriberAlreadyRegisteredException;
import me.x150.jmessenger.impl.SubscriberRegisterEvent;
import me.x150.jmessenger.impl.SubscriberUnregisterEvent;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * A message manager
 */
public class MessageManager {
    protected final List<Handler> handlers = new CopyOnWriteArrayList<>();

    /**
     * Registers a new handler. Will sort all handlers based on priority, and emit an {@link SubscriberRegisterEvent}
     *
     * @param handler The handler to register
     */
    protected void register(Handler handler) {
        this.handlers.add(handler);
        sortHandlers();
        send(new SubscriberRegisterEvent(handler));
    }

    private void sortHandlers() {
        this.handlers.sort(Comparator.comparingInt(Handler::priority));
    }

    /**
     * Gets all subscribers listening for {@code subscriptionType}
     *
     * @param subscriptionType The message type to search for
     *
     * @return The list of handlers listening for {@code subscriptionType}
     */
    protected List<Handler> getSubscribersByType(Class<?> subscriptionType) {
        return handlers.stream().filter(handler -> handler.subscriptionType.isAssignableFrom(subscriptionType)).toList();
    }

    /**
     * Sends a message to all handlers listening for it.
     *
     * @param o The message to send. Can be any object.
     */
    public void send(Object o) {
        try {
            Class<?> aClass = o.getClass();
            for (Handler handler : getSubscribersByType(aClass)) {
                handler.invoke(o);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Removes all handlers from the handler class of object {@code instance}
     *
     * @param instance The instance of the handler class, whose handlers to remove
     *
     * @implNote This calls {@link #unregister(Class)} with the class type of {@code instance}
     */
    public void unregister(Object instance) {
        unregister(instance.getClass());
    }

    /**
     * Removes all handlers from the class {@code i}
     *
     * @param i The class owning the handlers which to remove
     */
    public void unregister(Class<?> i) {
        this.handlers.removeIf(handler -> handler.ownerClass.equals(i));
        send(new SubscriberUnregisterEvent(i));
    }

    /**
     * Registers all methods annotated with {@link MessageSubscription} as handlers for specific messages.<p>
     * Note that all handlers must have exactly one argument of the type of message they wish to listen for, and no return type (V)<p>
     * A method handler not following these rules will cause an {@link InvalidSubscriberException} to be thrown, explaining which method violated the rule, and how.
     *
     * @param instance The instance of the class containing the handler methods.
     */
    public void registerSubscribers(Object instance) {
        Class<?> aClass = instance.getClass();
        Method[] declaredMethods = aClass.getDeclaredMethods();
        for (Method declaredMethod : declaredMethods) {
            MessageSubscription annotationFrom = Util.getAnnotationFrom(declaredMethod, MessageSubscription.class);
            if (annotationFrom == null) {
                continue;
            }
            Class<?>[] parameterTypes = declaredMethod.getParameterTypes();
            if (parameterTypes.length != 1) {
                throw new InvalidSubscriberException(String.format("Handler %s.%s%s has an invalid signature: Expected 1 argument, found %s",
                    aClass.getName(),
                    declaredMethod.getName(),
                    Util.signatureOf(declaredMethod),
                    parameterTypes.length));
            }
            Class<?> listenerType = parameterTypes[0];
            Handler handler = new Handler(instance, aClass, declaredMethod, listenerType, annotationFrom.priority());
            if (this.handlers.contains(handler)) {
                throw new SubscriberAlreadyRegisteredException(String.format("Handler %s.%s%s is already registered",
                    aClass.getName(),
                    declaredMethod.getName(),
                    Util.signatureOf(declaredMethod)));
            }
            register(handler);
        }
    }

    /**
     * A message handler
     *
     * @param ownerInstance    The instance of the owning class, or null if handler is static
     * @param ownerClass       The type of the owning class
     * @param callee           The handler method
     * @param subscriptionType The object type being subscribed to
     * @param priority         The priority of this handler
     */
    public record Handler(Object ownerInstance, Class<?> ownerClass, Method callee, Class<?> subscriptionType, int priority) {
        /**
         * Invokes this handler
         *
         * @param message The message being sent to this handler
         *
         * @throws InvocationTargetException When {@link Method#invoke(Object, Object...)} throws
         * @throws IllegalAccessException    When {@link Method#invoke(Object, Object...)} throws
         * @implNote The object {@code message} is not instance checked with {@link #subscriptionType}, and may be an arbitrary object.
         */
        public void invoke(Object message) throws InvocationTargetException, IllegalAccessException {
            callee.setAccessible(true);
            callee.invoke(ownerInstance, message);
        }

        @Override
        public String toString() {
            return "Handler{" + "ownerInstance=" + ownerInstance + ", ownerClass=" + ownerClass + ", callee=" + callee + ", subscriptionType=" + subscriptionType + ", priority=" + priority + '}';
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof Handler h)) {
                return false;
            }
            return callee.equals(h.callee);
        }
    }
}
