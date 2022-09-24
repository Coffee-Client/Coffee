package coffee.client.login.mojang;

import java.util.UUID;

public record MinecraftToken(String accessToken, String username, UUID uuid, boolean isMicrosoft) {

}
