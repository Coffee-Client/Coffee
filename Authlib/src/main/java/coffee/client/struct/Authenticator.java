/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors.
 * Some rights reserved, refer to LICENSE file.
 */

package coffee.client.struct;

import com.google.gson.Gson;

import java.net.http.HttpClient;
import java.time.Duration;

public abstract class Authenticator<T> {
    protected final Gson gson = new Gson();
    protected final HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10L)).build();

    public Authenticator() {
    }

    public abstract T login(String var1, String var2);
}
