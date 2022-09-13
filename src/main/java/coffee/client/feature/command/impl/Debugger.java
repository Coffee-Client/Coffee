/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors. Some rights reserved.
 */

package coffee.client.feature.command.impl;

import coffee.client.feature.command.Command;
import coffee.client.feature.command.exception.CommandException;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.Packet;

public class Debugger extends Command {
    public Debugger() {
        super("Debugger", "Don't use this if you dont know what it does", "dbg");
    }

    @Override
    public void onExecute(String[] args) throws CommandException {
        validateArgumentsLength(args, 1, "action required");
        switch (args[0].toLowerCase()) {
            case "addpacketclass" -> {
                validateArgumentsLength(args, 2, "packet class required");
                String pClass = args[1];
                try {
                    Class<?> aClass = Class.forName(pClass);
                    if (!Packet.class.isAssignableFrom(aClass)) {
                        error("class " + aClass.getSimpleName() + " doesn't extend packet");
                        return;
                    }
                    @SuppressWarnings("unchecked") // i know it extends packet shitter
                    Class<? extends Packet<?>> pResolved = (Class<? extends Packet<?>>) aClass;
                    coffee.client.feature.module.impl.misc.Debugger.whitelistedPacketClasses.add(pResolved);
                    success("whitelisted packet class " + pResolved.getSimpleName());
                } catch (Exception e) {
                    e.printStackTrace();
                    throw new CommandException("failed to resolve class " + pClass);
                }
            }
            case "removepacketclass" -> {
                validateArgumentsLength(args, 2, "packet class required");
                String pClass = args[1];
                try {
                    Class<?> aClass = Class.forName(pClass);
                    if (!Packet.class.isAssignableFrom(aClass)) {
                        error("class " + aClass.getSimpleName() + " doesn't extend packet");
                        return;
                    }
                    @SuppressWarnings("unchecked") // i know it extends packet shitter
                    Class<? extends Packet<?>> pResolved = (Class<? extends Packet<?>>) aClass;
                    if (coffee.client.feature.module.impl.misc.Debugger.whitelistedPacketClasses.remove(pResolved)) {
                        success("removed packet class " + pResolved.getSimpleName());
                    } else {
                        error("class " + pResolved.getSimpleName() + " not in whitelist");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    throw new CommandException("failed to resolve class " + pClass);
                }
            }
            case "listnetpacketclasses" -> {
                for (Class<? extends Packet<?>> whitelistedPacketClass : coffee.client.feature.module.impl.misc.Debugger.whitelistedPacketClasses) {
                    message(FabricLoader.getInstance().getMappingResolver().unmapClassName("named", whitelistedPacketClass.getName()));
                }
            }
        }
    }
}
