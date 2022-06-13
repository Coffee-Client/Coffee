/*
 * Copyright 2013 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package coffee.client.helper.network.handler.socksx.v5;

import io.netty.util.internal.ObjectUtil;

/**
 * The type of {@link Socks5CommandRequest}.
 */
public class Socks5CommandType implements Comparable<Socks5CommandType> {

    public static final Socks5CommandType CONNECT = new Socks5CommandType(0x01, "CONNECT");
    public static final Socks5CommandType BIND = new Socks5CommandType(0x02, "BIND");
    public static final Socks5CommandType UDP_ASSOCIATE = new Socks5CommandType(0x03, "UDP_ASSOCIATE");
    private final byte byteValue;
    private final String name;
    private String text;

    public Socks5CommandType(int byteValue) {
        this(byteValue, "UNKNOWN");
    }

    public Socks5CommandType(int byteValue, String name) {
        this.name = ObjectUtil.checkNotNull(name, "name");
        this.byteValue = (byte) byteValue;
    }

    public static Socks5CommandType valueOf(byte b) {
        return switch (b) {
            case 0x01 -> CONNECT;
            case 0x02 -> BIND;
            case 0x03 -> UDP_ASSOCIATE;
            default -> new Socks5CommandType(b);
        };

    }

    public byte byteValue() {
        return byteValue;
    }

    @Override
    public int hashCode() {
        return byteValue;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Socks5CommandType)) {
            return false;
        }

        return byteValue == ((Socks5CommandType) obj).byteValue;
    }

    @Override
    public int compareTo(Socks5CommandType o) {
        return byteValue - o.byteValue;
    }

    @Override
    public String toString() {
        String text = this.text;
        if (text == null) {
            this.text = text = name + '(' + (byteValue & 0xFF) + ')';
        }
        return text;
    }
}
