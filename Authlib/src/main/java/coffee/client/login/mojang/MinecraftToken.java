/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors.
 * Some rights reserved, refer to LICENSE file.
 */

package coffee.client.login.mojang;

import java.util.UUID;

public record MinecraftToken(String accessToken, String username, UUID uuid, boolean isMicrosoft) {

}
