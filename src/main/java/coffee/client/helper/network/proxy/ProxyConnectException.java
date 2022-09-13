/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors.
 * Some rights reserved, refer to LICENSE file.
 */

package coffee.client.helper.network.proxy;


import java.io.Serial;
import java.net.ConnectException;

public class ProxyConnectException extends ConnectException {
    @Serial
    private static final long serialVersionUID = 5211364632246265538L;

    public ProxyConnectException() {
    }

    public ProxyConnectException(String msg) {
        super(msg);
    }

    public ProxyConnectException(Throwable cause) {
        initCause(cause);
    }

    public ProxyConnectException(String msg, Throwable cause) {
        super(msg);
        initCause(cause);
    }
}
