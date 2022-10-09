/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors.
 * Some rights reserved, refer to LICENSE file.
 */

package me.x150.jmessenger.exception;

import me.x150.jmessenger.MessageSubscription;

/**
 * Thrown when a method with {@link MessageSubscription} has an invalid handler signature
 * <p>
 * A method annotated with {@link MessageSubscription} should have one argument, the class being listened for.<p>
 * Note that objects extending the listener type of the handler will also be sent to your handler, allowing you to receive all events by listening to {@link Object}
 * <p>
 * Working example:
 * <blockquote><pre>
 *    {@literal @}MessageSubscription
 *     void handle(Message message) {
 *         System.out.println("Received message " + message);
 *     }
 * </pre></blockquote>
 */
public class InvalidSubscriberException extends RuntimeException {
    public InvalidSubscriberException(String cause) {
        super(cause);
    }
}
