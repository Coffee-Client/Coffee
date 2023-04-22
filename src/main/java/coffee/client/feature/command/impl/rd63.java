package coffee.client.feature.command.impl;

import coffee.Coffee;
import coffee.client.feature.command.Command;
import coffee.client.feature.command.syntax.ChunkBuilder;
import coffee.client.feature.command.syntax.Syntax;
import coffee.client.feature.module.Module;
import coffee.client.feature.module.ModuleManager;
import coffee.client.utils.ChatUtils;
import io.netty.buffer.Unpooled;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.PacketDirection;
import net.minecraft.network.PacketType;
import net.minecraft.network.protocol.game.ServerboundCustomPayloadPacket;
import net.minecraft.resources.ResourceLocation;

import java.io.DataOutputStream;
import java.io.IOException;

public class rd63 extends Command {

    public RDPacketCommand() {
        super("rd63", new Syntax(
                new ChunkBuilder().append("toggle")
        ));
    }

    @Override
    public void handle(String[] args) {
        if (args[0].equalsIgnoreCase("toggle")) {
            Module module = ModuleManager.getModule("RD63");
            if (module.isEnabled()) {
                module.disable();
                ChatUtils.sendMessage("RD63 has been disabled.");
            } else {
                module.enable();
                ChatUtils.sendMessage("RD63 has been enabled.");
            }
        }
    }
}

class SlowDownPacket implements Packet<ServerboundCustomPayloadPacket> {

    private boolean isEnabled = false;

    public SlowDownPacket() {
    }

    @Override
    public void write(PacketBuffer buf) throws IOException  {
        PacketBuffer packetbuffer = new PacketBuffer(Unpooled.buffer());
        packetbuffer.writeEnum(PacketDirection.SERVERBOUND);
        packetbuffer.writeEnum(PacketType.JIGSAW);
        packetbuffer.writeByteArray(generateC03PacketData());
        packetbuffer.writeBoolean(false);
        if (isEnabled) {
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        buf.writeBytes(packetbuffer);
    }

    @Override
    public void handle(ServerboundCustomPayloadPacket packet) {
      
    }

    private byte[] generateC03PacketData() {
        // LAZY SHIT, HOLY FUCK
        return new byte[0];
    }
}
