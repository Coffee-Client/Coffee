/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors. Some rights reserved.
 */

package coffee.client.helper;

import coffee.client.CoffeeMain;
import org.apache.logging.log4j.Level;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AddonClassLoader extends URLClassLoader {
    final Map<String, URL> resourceMap = new ConcurrentHashMap<>();

    public AddonClassLoader(ClassLoader parent) {
        super(new URL[0], parent);
    }

    public Class<?> defineAndGetClass(byte[] classBytes) {
        return super.defineClass(null, classBytes, 0, classBytes.length);
    }

    public void defineResource(String name, URL location) {
        CoffeeMain.log(Level.DEBUG, "Registering texture " + name + " to URL " + location.toString());
        resourceMap.put(name, location);
    }

    @Override
    public URL findResource(String name) {
        CoffeeMain.log(Level.DEBUG, "Finding texture " + name);
        if (resourceMap.containsKey(name)) {
            return resourceMap.get(name);
        }
        return super.findResource(name);
    }
}
