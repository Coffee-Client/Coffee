/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors.
 * Some rights reserved, refer to LICENSE file.
 */

package coffee.client.login.microsoft;

import coffee.client.struct.AuthToken;
import lombok.Getter;

@Getter
public class XboxLiveToken extends AuthToken {
    protected String token;
    protected String uhs;

    public XboxLiveToken(String token, String uhs) {
        this.token = token;
        this.uhs = uhs;
    }
}
