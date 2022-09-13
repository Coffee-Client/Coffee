/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors. All rights reserved.
 */

package coffee.client.feature.command.impl;

import coffee.client.CoffeeMain;
import coffee.client.feature.command.Command;
import coffee.client.feature.command.argument.PlayerFromNameArgumentParser;
import coffee.client.feature.command.coloring.ArgumentType;
import coffee.client.feature.command.coloring.PossibleArgument;
import coffee.client.feature.command.coloring.StaticArgumentServer;
import coffee.client.feature.command.examples.ExampleServer;
import coffee.client.feature.command.exception.CommandException;
import com.mojang.authlib.GameProfile;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.NetworkState;
import net.minecraft.network.listener.ClientLoginPacketListener;
import net.minecraft.network.packet.c2s.handshake.HandshakeC2SPacket;
import net.minecraft.network.packet.c2s.login.LoginHelloC2SPacket;
import net.minecraft.network.packet.s2c.login.LoginCompressionS2CPacket;
import net.minecraft.network.packet.s2c.login.LoginDisconnectS2CPacket;
import net.minecraft.network.packet.s2c.login.LoginHelloS2CPacket;
import net.minecraft.network.packet.s2c.login.LoginQueryRequestS2CPacket;
import net.minecraft.network.packet.s2c.login.LoginSuccessS2CPacket;
import net.minecraft.text.Text;

import java.net.InetSocketAddress;
import java.util.Objects;
import java.util.Optional;

public class SocketKick extends Command {
    public SocketKick() {
        super("SocketKick", "Kicks people out of the server using sockets!!", "socketkick", "skick", "sockkick");
    }

    @Override
    public PossibleArgument getSuggestionsWithType(int index, String[] args) {
        return StaticArgumentServer.serveFromStatic(index,
            new PossibleArgument(ArgumentType.PLAYER,
                () -> Objects.requireNonNull(CoffeeMain.client.world)
                    .getPlayers()
                    .stream()
                    .map(abstractClientPlayerEntity -> abstractClientPlayerEntity.getGameProfile().getName())
                    .toList()
                    .toArray(String[]::new)));
    }

    @Override
    public ExamplesEntry getExampleArguments() {
        return ExampleServer.getPlayerNames();
    }

    @Override
    public void onExecute(String[] args) throws CommandException {
        InetSocketAddress sa = (InetSocketAddress) CoffeeMain.client.getNetworkHandler().getConnection().getAddress();
        ClientConnection conn = ClientConnection.connect(sa, CoffeeMain.client.options.shouldUseNativeTransport());
        conn.setPacketListener(new ClientLoginPacketListener() {
            @Override
            public void onHello(LoginHelloS2CPacket packet) {
                conn.disconnect(Text.of("your mother"));
            }

            @Override
            public void onSuccess(LoginSuccessS2CPacket packet) {

            }

            @Override
            public void onDisconnect(LoginDisconnectS2CPacket packet) {

            }

            @Override
            public void onCompression(LoginCompressionS2CPacket packet) {

            }

            @Override
            public void onQueryRequest(LoginQueryRequestS2CPacket packet) {

            }

            @Override
            public void onDisconnected(Text reason) {

            }

            @Override
            public ClientConnection getConnection() {
                return null;
            }
        });
        conn.send(new HandshakeC2SPacket(sa.getHostName(), sa.getPort(), NetworkState.LOGIN));
        PlayerFromNameArgumentParser parser = new PlayerFromNameArgumentParser(true);
        PlayerEntity pe = parser.parse(args[0]);
        GameProfile fuck = pe.getGameProfile();
        conn.send(new LoginHelloC2SPacket(fuck.getName(), Optional.empty(), Optional.empty()));
    }
}
