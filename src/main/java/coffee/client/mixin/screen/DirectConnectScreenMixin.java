/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors.
 * Some rights reserved, refer to LICENSE file.
 */

package coffee.client.mixin.screen;

import coffee.client.feature.gui.FastTickable;
import coffee.client.helper.CodecMapper;
import coffee.client.helper.DirectConnectScreenVariables;
import coffee.client.helper.font.FontRenderers;
import coffee.client.helper.font.adapter.impl.RendererFontAdapter;
import coffee.client.helper.font.renderer.ColoredTextSegment;
import coffee.client.helper.render.AlphaOverride;
import coffee.client.helper.render.ClipStack;
import coffee.client.helper.render.MSAAFramebuffer;
import coffee.client.helper.render.Rectangle;
import coffee.client.helper.render.Renderer;
import coffee.client.helper.render.Texture;
import coffee.client.helper.util.Transitions;
import coffee.client.helper.util.Utils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.blaze3d.systems.RenderSystem;
import me.x150.netmc.C2SPacket;
import me.x150.netmc.PacketInputStream;
import me.x150.netmc.PacketOutputStream;
import me.x150.netmc.S2CPacket;
import me.x150.renderer.util.RendererUtils;
import net.minecraft.SharedConstants;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.DirectConnectScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.server.ServerMetadata;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.LowercaseEnumTypeAdapterFactory;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import static coffee.client.helper.DirectConnectScreenVariables.*;

@Debug(export = true)
@Mixin(DirectConnectScreen.class)
public abstract class DirectConnectScreenMixin extends Screen implements FastTickable {
    private static final Gson GSON = new GsonBuilder().registerTypeAdapter(ServerMetadata.Version.class, CodecMapper.createSerializer(ServerMetadata.Version.CODEC))
        .registerTypeAdapter(ServerMetadata.Players.class, CodecMapper.createDeserializer(ServerMetadata.Players.CODEC))
        .registerTypeAdapter(ServerMetadata.class, CodecMapper.createDeserializer(ServerMetadata.CODEC))
        .registerTypeHierarchyAdapter(Text.class, new Text.Serializer())
        .registerTypeHierarchyAdapter(Style.class, new Style.Serializer())
        .registerTypeAdapterFactory(new LowercaseEnumTypeAdapterFactory())
        .create();
    private static final Texture SERVER_ICON = new Texture("dynamic/directConnectServerIcon");
    private static final Identifier UNKNOWN_SERVER_ICON = new Identifier("textures/misc/unknown_server.png");
    private static final AtomicBoolean alreadyRefreshing = new AtomicBoolean(false);
    private static final Color BG_COL = new Color(20, 20, 20);
    @Shadow
    private TextFieldWidget addressField;

    private DirectConnectScreenMixin() {
        super(null);
    }

    @Shadow
    protected abstract void onAddressFieldChanged();

    @ModifyArg(method = "init", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/widget/TextFieldWidget;<init>(Lnet/minecraft/client/font/TextRenderer;IIIILnet/minecraft/text/Text;)V"),
               index = 2)
    int coffee_modifyY(int y) {
        return this.height / 4 + 96 + 12 - 30;
    }

    @Inject(method = "init", at = @At("RETURN"))
    void coffee_postReturn(CallbackInfo ci) {
        this.addressField.setChangedListener(s -> {
            onAddressFieldChanged();
            if (s.equals(lastIp)) {
                return;
            }
            lastIp = s;
            lastChanged = System.currentTimeMillis();
            updated = false;
        });
    }

    @ModifyArg(method = "render", at = @At(value = "INVOKE",
                                           target = "Lnet/minecraft/client/gui/DrawContext;drawTextWithShadow(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/text/Text;III)I"),
               index = 4)
    int coffee_modifyYOfText(int par4) {
        return this.height / 4 + 96 + 12 - 30 - 16;
    }

    @Inject(method = "tick", at = @At("RETURN"))
    void coffee_postTick(CallbackInfo ci) {
        if (System.currentTimeMillis() - lastChanged > 500 && !updated) { // 500 ms delay between stopping typing and update
            try {
                update();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void update() {
        if (alreadyRefreshing.get()) {
            return;
        }
        String currentIp = this.addressField.getText();
        if (!ServerAddress.isValid(currentIp)) {
            return;
        }
        ServerAddress parse = ServerAddress.parse(currentIp);
        String address = parse.getAddress();
        int port = parse.getPort();
        alreadyRefreshing.set(true);
        updated = true;
        new Thread(() -> {
            try (Socket socket = openSock(address, port)) {
                PacketOutputStream pos = new PacketOutputStream(socket.getOutputStream(), false);
                PacketInputStream pis = new PacketInputStream(socket.getInputStream(), true, pos::setCompressionEnabled);
                int protocolVersion = SharedConstants.getProtocolVersion();
                pos.write(new C2SPacket(0x00, packetWriter -> packetWriter // Handshake
                    .writeVarInt(protocolVersion) // Version
                    .writeString(address) // Connecting address
                    .writeShort(port) // Connecting port
                    .writeVarInt(1))); // Next state: Status
                pos.write(new C2SPacket(0x00)); // Status
                S2CPacket read = pis.read();
                pis.close();
                pos.close();
                String s = read.getPacketReader().readString();
                DirectConnectScreenVariables.latestResponse = JsonHelper.deserialize(GSON, s, ServerMetadata.class);
                Optional<ServerMetadata.Favicon> favicon1 = latestResponse.favicon();
                if (favicon1.isEmpty()) {
                    serverTextureKnown = false;
                } else {
                    try {
                        byte[] bytes = favicon1.get().iconBytes();
                        RendererUtils.registerBufferedImageTexture(SERVER_ICON, ImageIO.read(new ByteArrayInputStream(bytes)));
                        serverTextureKnown = true;
                    } catch (Exception ignored) {
                        serverTextureKnown = false;
                    }
                }
                show = true;
            } catch (Exception e) {
                show = false;
            } finally {
                alreadyRefreshing.set(false);
            }
        }, "Direct connect updater").start();
    }

    Socket openSock(String a, int b) throws IOException {
        Socket s = new Socket();
        s.setSoLinger(false, 0);
        s.setSoTimeout(3000);
        s.connect(new InetSocketAddress(a, b));
        return s;
    }

    @Inject(method = "render", at = @At("RETURN"))
    void coffee_postRender(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        MSAAFramebuffer.use(() -> coffee_innerRender(context.getMatrices()));
    }

    @Override
    public void onFastTick() {
        double delta = 0.02;
        if (!show) {
            delta *= -1;
        }
        animationProgress += delta;
        animationProgress = MathHelper.clamp(animationProgress, 0, 1);
    }

    void coffee_innerRender(MatrixStack ms) {
        double v = Transitions.easeOutExpo(animationProgress);
        double height = 40;
        double minWidth = 40;
        double innerPadding = 4;
        double maxWidth = 300;
        double width = MathHelper.lerp(v, minWidth, maxWidth);
        double originX = this.width / 2d - width / 2d;
        double originY = 50;
        Renderer.R2D.renderRoundedQuad(ms, BG_COL, originX, originY, originX + width, originY + height, 5, 10);
        Renderer.R2D.renderLoadingSpinner(ms, 1f, originX + minWidth / 2d, originY + height / 2d, 10, 1, 10);

        RenderSystem.setShaderTexture(0, serverTextureKnown ? SERVER_ICON : UNKNOWN_SERVER_ICON);

        Renderer.R2D.runWithinBlendMask(() -> Renderer.R2D.renderRoundedQuadInternal(
            ms.peek().getPositionMatrix(),
            0f,
            0f,
            0f,
            (float) v,
            originX + innerPadding,
            originY + innerPadding,
            originX + minWidth - innerPadding,
            originY + height - innerPadding,
            5,
            10
        ), () -> {
            double w = minWidth - innerPadding * 2;
            Renderer.R2D.renderTexture(ms, originX + innerPadding, originY + innerPadding, w, w, 0, 0, w, w, w, w);
        });
        ServerMetadata latestResponse = DirectConnectScreenVariables.latestResponse;
        if (latestResponse != null) {
            ClipStack.globalInstance.addWindow(ms, new Rectangle(originX, originY, originX + width, originY + height));
            double statsX = originX + minWidth;
            List<OrderedText> orderedTexts = this.client.textRenderer.wrapLines(latestResponse.description(), (int) (maxWidth - innerPadding - minWidth));
            RendererFontAdapter fa = (RendererFontAdapter) FontRenderers.getRenderer();
            AlphaOverride.pushAlphaMul((float) v);
            for (int i = 0; i < Math.min(orderedTexts.size(), 2); i++) {
                OrderedText orderedText = orderedTexts.get(i);
                ColoredTextSegment content = Utils.getContent(orderedText, true, false);
                fa.drawString(ms, content, (float) statsX, (float) (originY + innerPadding + fa.getFontHeight() * i));
            }
            AlphaOverride.popAlphaMul();

            String format = String.format("%s / %s", latestResponse.players().orElseThrow().online(), latestResponse.players().orElseThrow().max());
            fa.drawString(ms, format, originX + maxWidth - innerPadding - fa.getStringWidth(format), originY + innerPadding, 0xBBBBBB);
            ClipStack.globalInstance.popWindow();
        }
    }
}
