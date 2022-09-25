/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors.
 * Some rights reserved, refer to LICENSE file.
 */

package coffee.client.exception;

public class AuthFailureException extends RuntimeException {
    public AuthFailureException(String message) {
        super(message);
    }
}
