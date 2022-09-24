package coffee.client.login.mojang;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class AuthResponse {
    @Getter
    @Setter
    public static class User {

        @SerializedName("id")
        @Expose
        private String id;
        @SerializedName("username")
        @Expose
        private String username;

    }
    @Getter
    @Setter
    public static class SelectedProfile {

        @SerializedName("name")
        @Expose
        private String name;
        @SerializedName("id")
        @Expose
        private String id;

    }
    @SerializedName("user")
    @Expose
    private User user;
    @SerializedName("clientToken")
    @Expose
    private String clientToken;
    @SerializedName("accessToken")
    @Expose
    private String accessToken;
    @SerializedName("selectedProfile")
    @Expose
    private SelectedProfile selectedProfile;
    @SerializedName("availableProfiles")
    @Expose
    private List<AvailableProfile> availableProfiles = null;

    @Getter
    @Setter
    public static class AvailableProfile {

        @SerializedName("name")
        @Expose
        private String name;
        @SerializedName("id")
        @Expose
        private String id;
    }
}
