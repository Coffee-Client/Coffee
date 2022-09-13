/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors. Some rights reserved.
 */
package coffee.client.helper.network.handler.socksx.v5;

/**
 * A SOCKS5 request detail message, as defined in
 * <a href="https://tools.ietf.org/html/rfc1928#section-4">the section 4, RFC1928</a>.
 */
public interface Socks5CommandRequest extends Socks5Message {

    /**
     * Returns the type of this request.
     */
    Socks5CommandType type();

    /**
     * Returns the type of the {@code DST.ADDR} field of this request.
     */
    Socks5AddressType dstAddrType();

    /**
     * Returns the {@code DST.ADDR} field of this request.
     */
    String dstAddr();

    /**
     * Returns the {@code DST.PORT} field of this request.
     */
    int dstPort();
}
