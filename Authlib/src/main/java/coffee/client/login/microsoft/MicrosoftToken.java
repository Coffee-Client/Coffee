

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
