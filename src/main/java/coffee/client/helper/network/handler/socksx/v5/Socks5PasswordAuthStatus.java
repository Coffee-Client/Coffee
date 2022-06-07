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
 * The status of {@link Socks5PasswordAuthResponse}.
 */
public class Socks5PasswordAuthStatus implements Comparable<Socks5PasswordAuthStatus> {

    public static final Socks5PasswordAuthStatus SUCCESS = new Socks5PasswordAuthStatus(0x00, "SUCCESS");
    public static final Socks5PasswordAuthStatus FAILURE = new Socks5PasswordAuthStatus(0xFF, "FAILURE");
    private final byte byteValue;
    private final String name;
    private String text;
    public Socks5PasswordAuthStatus(int byteValue) {
        this(byteValue, "UNKNOWN");
    }

    public Socks5PasswordAuthStatus(int byteValue, String name) {
        this.name = ObjectUtil.checkNotNull(name, "name");
        this.byteValue = (byte) byteValue;
    }

    public static Socks5PasswordAuthStatus valueOf(byte b) {
        return switch (b) {
            case 0x00 -> SUCCESS;
            case (byte) 0xFF -> FAILURE;
            default -> new Socks5PasswordAuthStatus(b);
        };

    }

    public byte byteValue() {
        return byteValue;
    }

    public boolean isSuccess() {
        return byteValue == 0;
    }

    @Override
    public int hashCode() {
        return byteValue;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Socks5PasswordAuthStatus)) {
            return false;
        }

        return byteValue == ((Socks5PasswordAuthStatus) obj).byteValue;
    }

    @Override
    public int compareTo(Socks5PasswordAuthStatus o) {
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
