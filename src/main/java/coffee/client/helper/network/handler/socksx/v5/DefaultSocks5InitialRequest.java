/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors. Some rights reserved.
 */
package coffee.client.helper.network.handler.socksx.v5;

import io.netty.handler.codec.DecoderResult;
import io.netty.util.internal.ObjectUtil;
import io.netty.util.internal.StringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static io.netty.util.internal.ObjectUtil.checkNonEmpty;

/**
 * The default {@link Socks5InitialRequest}.
 */
public class DefaultSocks5InitialRequest extends AbstractSocks5Message implements Socks5InitialRequest {

    private final List<Socks5AuthMethod> authMethods;

    public DefaultSocks5InitialRequest(Socks5AuthMethod... authMethods) {
        ObjectUtil.checkNotNull(authMethods, "authMethods");

        List<Socks5AuthMethod> list = new ArrayList<>(authMethods.length);
        for (Socks5AuthMethod m : authMethods) {
            if (m == null) {
                break;
            }
            list.add(m);
        }

        this.authMethods = Collections.unmodifiableList(checkNonEmpty(list, "list"));
    }

    public DefaultSocks5InitialRequest(Iterable<Socks5AuthMethod> authMethods) {
        ObjectUtil.checkNotNull(authMethods, "authSchemes");

        List<Socks5AuthMethod> list = new ArrayList<>();
        for (Socks5AuthMethod m : authMethods) {
            if (m == null) {
                break;
            }
            list.add(m);
        }

        this.authMethods = Collections.unmodifiableList(checkNonEmpty(list, "list"));
    }

    @Override
    public List<Socks5AuthMethod> authMethods() {
        return authMethods;
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder(StringUtil.simpleClassName(this));

        DecoderResult decoderResult = decoderResult();
        if (!decoderResult.isSuccess()) {
            buf.append("(decoderResult: ");
            buf.append(decoderResult);
            buf.append(", authMethods: ");
        } else {
            buf.append("(authMethods: ");
        }
        buf.append(authMethods());
        buf.append(')');

        return buf.toString();
    }
}
