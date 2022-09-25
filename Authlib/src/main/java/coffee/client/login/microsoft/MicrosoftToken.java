/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors.
 * Some rights reserved, refer to LICENSE file.
 */

package coffee.client.login.microsoft;

import coffee.client.struct.AuthToken;
import lombok.Getter;

@Getter
public class MicrosoftToken extends AuthToken {
    protected String token;
    protected String refreshToken;

    public MicrosoftToken(String token, String refreshToken) {
        this.token = token;
        this.refreshToken = refreshToken;
    }
}
