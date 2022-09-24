

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
