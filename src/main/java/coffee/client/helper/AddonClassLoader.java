/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors.
 * Some rights reserved, refer to LICENSE file.
 */

package coffee.client.helper;

import coffee.client.CoffeeMain;
import org.apache.logging.log4j.Level;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AddonClassLoader extends URLClassLoader {
    final Map<String, URI> resourceMap = new ConcurrentHashMap<>();

    public AddonClassLoader(ClassLoader parent) {
        super(new URL[0], parent);
    }

    public Class<?> defineAndGetClass(byte[] classBytes) {
        return super.defineClass(null, classBytes, 0, classBytes.length);
    }

    public void defineResource(String name, URL location) {
        try {
            URI uri = location.toURI();
            CoffeeMain.log(Level.DEBUG, "Registering texture " + name + " to URL " + uri);
            resourceMap.put(name, uri);
        } catch (URISyntaxException ac) {
            CoffeeMain.log(Level.ERROR, "Something went horribly wrong when defining an addon resource", ac);
        }
    }

    @Override
    public URL findResource(String name) {
        CoffeeMain.log(Level.DEBUG, "Finding texture " + name);
        if (resourceMap.containsKey(name)) {
            try {
                return resourceMap.get(name).toURL();
            } catch (MalformedURLException e) {
                CoffeeMain.log(Level.ERROR, "Something went horribly wrong when loading a resource", e);
            }
        }
        return super.findResource(name);
    }
}
