/*
 * Copyright (c) 2022 Coffee client, 0x150 and contributors. See copyright file in project root.
 */

package cf.coffee.client.helper;

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
        System.out.println("register " + name + " with " + location.toString());
        resourceMap.put(name, location);
    }

    @Override
    public URL findResource(String name) {
        System.out.println(resourceMap);
        System.out.println("find resource " + name);
        if (resourceMap.containsKey(name)) return resourceMap.get(name);
        return super.findResource(name);
    }
}
