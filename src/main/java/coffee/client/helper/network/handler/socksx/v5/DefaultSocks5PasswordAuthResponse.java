/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors. All rights reserved.
 */
package coffee.client.helper.network.handler.socksx.v5;

import io.netty.handler.codec.DecoderResult;
import io.netty.util.internal.ObjectUtil;
import io.netty.util.internal.StringUtil;

/**
 * The default {@link Socks5PasswordAuthResponse}.
 */
public class DefaultSocks5PasswordAuthResponse extends AbstractSocks5Message implements Socks5PasswordAuthResponse {

    private final Socks5PasswordAuthStatus status;

    public DefaultSocks5PasswordAuthResponse(Socks5PasswordAuthStatus status) {
        this.status = ObjectUtil.checkNotNull(status, "status");
    }

    @Override
    public Socks5PasswordAuthStatus status() {
        return status;
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder(StringUtil.simpleClassName(this));

        DecoderResult decoderResult = decoderResult();
        if (!decoderResult.isSuccess()) {
            buf.append("(decoderResult: ");
            buf.append(decoderResult);
            buf.append(", status: ");
        } else {
            buf.append("(status: ");
        }
        buf.append(status());
        buf.append(')');

        return buf.toString();
    }
}
