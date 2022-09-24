/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors.
 * Some rights reserved, refer to LICENSE file.
 */

package coffee.client.feature.gui.screen;

import coffee.client.CoffeeMain;
import coffee.client.feature.gui.FastTickable;
import coffee.client.feature.gui.notifications.hudNotif.HudNotification;
import coffee.client.feature.gui.screen.base.ClientScreen;
import coffee.client.feature.gui.widget.RoundButton;
import coffee.client.feature.gui.widget.RoundTextFieldWidget;
import coffee.client.helper.font.FontRenderers;
import coffee.client.helper.font.adapter.FontAdapter;
import coffee.client.helper.render.ClipStack;
import coffee.client.helper.render.MSAAFramebuffer;
import coffee.client.helper.render.PlayerHeadResolver;
import coffee.client.helper.render.Rectangle;
import coffee.client.helper.render.Renderer;
import coffee.client.helper.render.Texture;
import coffee.client.helper.util.Transitions;
import coffee.client.login.mojang.MinecraftAuthenticator;
import coffee.client.login.mojang.MinecraftToken;
import coffee.client.login.mojang.profile.MinecraftProfile;
import coffee.client.mixin.IMinecraftClientMixin;
import coffee.client.mixin.ISessionMixin;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.authlib.minecraft.UserApiService;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import com.mojang.blaze3d.systems.RenderSystem;
import lombok.Setter;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.DefaultSkinHelper;
import net.minecraft.client.util.ProfileKeys;
import net.minecraft.client.util.Session;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.Level;
import org.lwjgl.opengl.GL40C;

import java.awt.Color;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class AltManagerScreen extends ClientScreen implements FastTickable {
    public static final Map<UUID, Texture> texCache = new HashMap<>();
    static final File ALTS_FILE = new File(CoffeeMain.BASE, "alts.sip");
    static final String TOP_NOTE = """
        // DO NOT SHARE THIS FILE
        // This file contains sensitive information about your accounts
        // Unless you REALLY KNOW WHAT YOU ARE DOING, DO NOT SEND THIS TO ANYONE
        """;
    static final Color bg = new Color(20, 20, 20);
    static final Color pillColor = new Color(25, 25, 25, 100);
    static final Color backgroundOverlay = new Color(0, 0, 0, 130);
    static final Color overlayBackground = new Color(25, 25, 25);
    private static AltManagerScreen instance = null;
    final List<AltContainer> alts = new ArrayList<>();
    final double leftWidth = 200;
    final FontAdapter titleSmall = FontRenderers.getCustomSize(30);
    final FontAdapter title = FontRenderers.getCustomSize(40);
    final AtomicBoolean isLoggingIn = new AtomicBoolean(false);
    final boolean currentAccountTextureLoaded = true;
    AltContainer selectedAlt;
    RoundButton add, exit, remove, tags, login, session, censorMail;
    RoundTextFieldWidget search;
    boolean censorEmail = true;
    double scroll = 0;
    double scrollSmooth = 0;
    Texture currentAccountTexture = new Texture("dynamic/currentaccount");

    @Setter
    Screen parent;

    private AltManagerScreen() {
        super(MSAAFramebuffer.MAX_SAMPLES);
        loadAlts();
        updateCurrentAccount();
    }

    public static AltManagerScreen instance(Screen parent) {
        if (instance == null) {
            instance = new AltManagerScreen();
        }
        instance.setParent(parent);
        return instance;
    }

    public List<AltContainer> getAlts() {
        return alts.stream()
            .filter(altContainer -> altContainer.storage.cachedName.toLowerCase().startsWith(search.get().toLowerCase()) || Arrays.stream(altContainer.storage.tags.split(
                ",")).map(String::trim).filter(s -> !s.isEmpty()).anyMatch(s -> s.toLowerCase().startsWith(search.get().toLowerCase())))
            .collect(Collectors.toList());
    }

    void tryParseAltsFile(String txt) {
        for (String s : txt.split("\n")) {
            String trm = s.trim();
            String[] spl = trm.split(":");
            String mail = spl[0];
            String pass = spl[1];
            AddScreenOverlay.AccountType acType = AddScreenOverlay.AccountType.MOJANG;
            if (spl.length > 2) {
                acType = AddScreenOverlay.AccountType.valueOf(spl[2].toUpperCase());
            }
            AltStorage as = new AltStorage("Unknown", mail, pass, UUID.randomUUID(), acType, "");
            AltContainer ac = new AltContainer(-1, -1, 0, as);
            ac.renderX = -1;
            ac.renderY = -1;
            alts.add(ac);
        }
    }

    @Override
    public void filesDragged(List<Path> paths) {
        for (Path path : paths) {
            File f = path.toFile();
            if (!f.exists() || !f.canRead() || !f.isFile()) {
                continue;
            }
            try {
                String content = Files.readString(path);
                tryParseAltsFile(content);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        super.filesDragged(paths);
    }

    void saveAlts() {
        CoffeeMain.log(Level.INFO, "Saving alts");
        JsonArray root = new JsonArray();
        for (AltContainer alt1 : alts) {
            AltStorage alt = alt1.storage;
            JsonObject current = new JsonObject();
            current.addProperty("email", alt.email);
            current.addProperty("password", alt.password);
            current.addProperty("type", alt.type.name());
            current.addProperty("cachedUsername", alt.cachedName);
            current.addProperty("cachedUUID", alt.cachedUuid != null ? alt.cachedUuid.toString() : null);
            current.addProperty("valid", alt.valid);
            // remove every tag that is empty or consists of only spaces
            List<String> parsedTags = Arrays.stream(alt.tags.split(",")).map(String::trim).filter(s -> !s.isEmpty()).toList();
            current.addProperty("tags", parsedTags.isEmpty() ? "" : String.join(",", parsedTags));
            root.add(current);
        }
        try {
            FileUtils.write(ALTS_FILE, TOP_NOTE + "\n" + root, StandardCharsets.UTF_8);
        } catch (Exception ignored) {
            CoffeeMain.log(Level.ERROR, "Failed to write alts file");
        }
    }

    @Override
    public void close() {
        client.setScreen(parent);
        saveAlts();
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    void loadAlts() {
        CoffeeMain.log(Level.INFO, "Loading alts");

        if (!ALTS_FILE.isFile()) {
            ALTS_FILE.delete();
        }
        if (!ALTS_FILE.exists()) {
            CoffeeMain.log(Level.INFO, "Skipping alt loading because file doesn't exist");
            return;
        }
        try {
            String contents = FileUtils.readFileToString(ALTS_FILE, StandardCharsets.UTF_8);
            JsonArray ja = JsonParser.parseString(contents).getAsJsonArray();
            for (JsonElement jsonElement : ja) {
                JsonObject jo = jsonElement.getAsJsonObject();
                try {
                    AltStorage container = new AltStorage(jo.get("cachedUsername").getAsString(),
                        jo.get("email").getAsString(),
                        jo.get("password").getAsString(),
                        UUID.fromString(jo.get("cachedUUID").getAsString()),
                        AddScreenOverlay.AccountType.valueOf(jo.get("type").getAsString()),
                        jo.get("tags") == null ? "" : jo.get("tags").getAsString());
                    container.valid = !jo.has("valid") || jo.get("valid").getAsBoolean();
                    AltContainer ac = new AltContainer(0, 0, 0, container);
                    ac.renderY = ac.renderX = -1;
                    alts.add(ac);
                } catch (Exception ignored) {

                }

            }
        } catch (Exception ignored) {
            CoffeeMain.log(Level.ERROR, "Failed to read alts file - corrupted?");
        }
    }

    double getPadding() {
        return 7;
    }

    double getHeaderHeight() {
        return 10 + getPadding() + title.getMarginHeight();
    }

    public void setSelectedAlt(AltContainer selectedAlt) {
        this.selectedAlt = selectedAlt;

    }

    void toggleCensor() {
        censorEmail = !censorEmail;
        censorMail.setText(censorEmail ? "Show email" : "Hide email");
    }

    @Override
    protected void init() {
        search = new RoundTextFieldWidget(width - 200 - 5 - 100 - 5 - 60 - 5 - 20 - getPadding(), 10 + title.getMarginHeight() / 2d - 20 / 2d, 200, 20, "Search");
        addDrawableChild(search);
        censorMail = new RoundButton(RoundButton.STANDARD,
            width - 100 - 5 - 60 - 5 - 20 - getPadding(),
            10 + title.getMarginHeight() / 2d - 20 / 2d,
            100,
            20,
            "Show email",
            this::toggleCensor);
        add = new RoundButton(RoundButton.SUCCESS, width - 60 - 5 - 20 - getPadding(), 10 + title.getMarginHeight() / 2d - 20 / 2d, 60, 20, "Add", () -> {
            if (!isLoggingIn.get()) {
                client.setScreen(new AddScreenOverlay(this));
            }
        });
        exit = new RoundButton(RoundButton.DANGER, width - 20 - getPadding(), 10 + title.getMarginHeight() / 2d - 20 / 2d, 20, 20, "X", this::close);

        double padding = 5;
        double widRHeight = 64 + padding * 2;
        double toX = width - getPadding();
        double fromY = getHeaderHeight();
        double toY = fromY + widRHeight;
        double fromX = width - (leftWidth + getPadding());
        double texDim = widRHeight - padding * 2;
        double buttonWidth = (toX - (fromX + texDim + padding * 2)) / 3d - padding / 4d;
        login = new RoundButton(RoundButton.SUCCESS, fromX + texDim + padding * 2, toY - 20 - padding, buttonWidth - padding, 20, "Login", () -> {
            if (!this.isLoggingIn.get()) {
                this.login();
            }
        });
        remove = new RoundButton(RoundButton.DANGER,
            fromX + texDim + padding * 2 + buttonWidth + padding / 2d,
            toY - 20 - padding,
            buttonWidth - padding,
            20,
            "Remove",
            () -> {
                if (!this.isLoggingIn.get()) {
                    this.remove();
                }
            });
        tags = new RoundButton(RoundButton.STANDARD,
            fromX + texDim + padding * 2 + buttonWidth + padding / 2d + buttonWidth + padding / 2d,
            toY - 20 - padding,
            buttonWidth - padding,
            20,
            "Tags",
            this::editTags);

        toY = height - getPadding();
        buttonWidth = toX - fromX - padding * 3 - texDim;
        session = new RoundButton(RoundButton.STANDARD, fromX + texDim + padding * 2, toY - 20 - padding, buttonWidth, 20, "Session", () -> {
            if (!this.isLoggingIn.get()) {
                Objects.requireNonNull(client).setScreen(new SessionEditor(this, CoffeeMain.client.getSession())); // this is not a session stealer
            }
        });

        addDrawableChild(censorMail);
        addDrawableChild(add);
        addDrawableChild(exit);
        addDrawableChild(login);
        addDrawableChild(remove);
        addDrawableChild(tags);
        addDrawableChild(session);
    }

    void editTags() {
        client.setScreen(new TagEditor(this));
    }

    void updateCurrentAccount() {
        UUID uid = CoffeeMain.client.getSession().getProfile().getId();

        this.currentAccountTexture = PlayerHeadResolver.resolve(uid);
    }

    void login() {
        if (this.selectedAlt == null) {
            return;
        }
        isLoggingIn.set(true);
        new Thread(() -> {
            this.selectedAlt.login();
            isLoggingIn.set(false);
            if (!this.selectedAlt.storage.valid) {
                HudNotification.create("Failed to log in", 5000, HudNotification.Type.ERROR);
                return;
            }
            Session newSession = new Session(selectedAlt.storage.cachedName,
                selectedAlt.storage.cachedUuid.toString(),
                selectedAlt.storage.accessToken,
                Optional.empty(),
                Optional.empty(),
                Session.AccountType.MOJANG);
            IMinecraftClientMixin accessor = ((IMinecraftClientMixin) CoffeeMain.client);
            accessor.setSession(newSession);
            // Fuck you, mojang
            try {
                YggdrasilAuthenticationService authenticationService = accessor.getAuthenticationService();
                UserApiService userApiService = authenticationService.createUserApiService(newSession.getAccessToken());
                accessor.setUserApiService(userApiService);
                ProfileKeys pk = new ProfileKeys(userApiService, newSession.getProfile().getId(), CoffeeMain.client.runDirectory.toPath());
                accessor.setProfileKeys(pk);

                HudNotification.create("Logged into account " + newSession.getUsername(), 5000, HudNotification.Type.INFO);
            } catch (Exception e) {
                HudNotification.create("Logged in, but failed to get profile keys", 5000, HudNotification.Type.ERROR);
                e.printStackTrace();
            }

            updateCurrentAccount();

        }).start();
    }

    void remove() {
        if (this.selectedAlt == null) {
            return;
        }
        alts.remove(this.selectedAlt);
        this.selectedAlt = null;
    }

    @Override
    public void onFastTick() {
        for (AltContainer alt : getAlts()) {
            alt.tickAnim();
        }
        scrollSmooth = Transitions.transition(scrollSmooth, scroll, 7, 0);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        scroll -= amount * 10;
        double max = 0;
        for (AltContainer alt : getAlts()) {
            max = Math.max(max, alt.y + alt.getHeight());
        }
        max -= height;
        max += getPadding();
        max = Math.max(0, max);
        scroll = MathHelper.clamp(scroll, 0, max);
        return super.mouseScrolled(mouseX, mouseY, amount);
    }

    @Override
    public void renderInternal(MatrixStack stack, int mouseX, int mouseY, float delta) {
        Renderer.R2D.renderQuad(stack, bg, 0, 0, width, height);
        title.drawString(stack, "Coffee", 10, 10, 0xFFFFFF, false);
        titleSmall.drawString(stack,
            "Alt manager",
            10 + title.getStringWidth("Coffee") + 5,
            10 + title.getMarginHeight() - titleSmall.getMarginHeight() - 1,
            0xFFFFFF,
            false);

        ClipStack.globalInstance.addWindow(stack,
            new Rectangle(getPadding() - 5, getHeaderHeight(), getPadding() + (width - (getPadding() + leftWidth + getPadding() * 2)) + 5, height));
        stack.push();
        stack.translate(0, -scrollSmooth, 0);
        double mys = mouseY + scrollSmooth;
        double x = getPadding();
        double y = getHeaderHeight();
        double wid = width - (getPadding() + leftWidth + getPadding() * 2);
        List<AltContainer> altList = getAlts();
        if (altList.isEmpty()) {
            title.drawCenteredString(stack, "No alts", wid / 2d, height / 2d, 0xAAAAAA);
            titleSmall.drawCenteredString(stack, "Add some with the \"Add\" button", wid / 2d, height / 2d + title.getMarginHeight(), 0xAAAAAAAA);
        }
        for (AltContainer alt : altList) {
            alt.x = x;
            alt.y = y;
            alt.width = wid;
            if (alt.renderX == -1) {
                alt.renderX = -alt.width;
            }
            if (alt.renderY == -1) {
                alt.renderY = alt.y;
            }
            alt.render(stack, mouseX, mys);
            y += alt.getHeight() + getPadding();
        }
        stack.pop();
        ClipStack.globalInstance.popWindow();

        double padding = 5;
        double widRHeight = 64 + padding * 2;

        double fromX = width - (leftWidth + getPadding());
        double toX = width - getPadding();
        double fromY = getHeaderHeight();
        double toY = fromY + widRHeight;

        Renderer.R2D.renderRoundedQuad(stack, pillColor, fromX, fromY, toX, toY, 5, 20);
        boolean vis = selectedAlt != null;
        remove.setVisible(vis);
        login.setVisible(vis);
        tags.setVisible(vis);
        if (vis) {

            double texDim = widRHeight - padding * 2;

            RenderSystem.enableBlend();
            RenderSystem.colorMask(false, false, false, true);
            RenderSystem.clearColor(0.0F, 0.0F, 0.0F, 0.0F);
            RenderSystem.clear(GL40C.GL_COLOR_BUFFER_BIT, false);
            RenderSystem.colorMask(true, true, true, true);
            RenderSystem.setShader(GameRenderer::getPositionColorShader);
            Renderer.R2D.renderRoundedQuadInternal(stack.peek().getPositionMatrix(),
                0,
                0,
                0,
                1,
                fromX + padding,
                fromY + padding,
                fromX + padding + texDim,
                fromY + padding + texDim,
                5,
                20);

            RenderSystem.blendFunc(GL40C.GL_DST_ALPHA, GL40C.GL_ONE_MINUS_DST_ALPHA);
            RenderSystem.setShaderTexture(0, selectedAlt.tex);
            Renderer.R2D.renderTexture(stack, fromX + padding, fromY + padding, texDim, texDim, 0, 0, 64, 64, 64, 64);
            RenderSystem.defaultBlendFunc();

            String mail;
            if (this.selectedAlt.storage.type != AddScreenOverlay.AccountType.CRACKED) {
                mail = this.selectedAlt.storage.email;
                String[] mailPart = mail.split("@");
                String domain = mailPart[mailPart.length - 1];
                String mailN = String.join("@", Arrays.copyOfRange(mailPart, 0, mailPart.length - 1));
                if (censorEmail) {
                    mailN = "*".repeat(mailN.length());
                }
                mail = mailN + "@" + domain;
            } else {
                mail = "No email bound";
            }
            AltContainer.PropEntry[] props = new AltContainer.PropEntry[] {
                new AltContainer.PropEntry(this.selectedAlt.storage.type == AddScreenOverlay.AccountType.CRACKED ? this.selectedAlt.storage.email : this.selectedAlt.storage.cachedName,
                    FontRenderers.getCustomSize(22),
                    this.selectedAlt.storage.valid ? 0xFFFFFF : 0xFF3333), new AltContainer.PropEntry(mail, FontRenderers.getRenderer(), 0xAAAAAA),
                new AltContainer.PropEntry("Type: " + this.selectedAlt.storage.type.s, FontRenderers.getRenderer(), 0xAAAAAA) };

            float propsOffset = (float) (fromY + padding);
            for (AltContainer.PropEntry prop : props) {
                prop.cfr.drawString(stack, prop.name, (float) (fromX + padding + texDim + padding), propsOffset, prop.color, false);
                propsOffset += prop.cfr.getMarginHeight();
            }
        } else {
            titleSmall.drawCenteredString(stack, "No alt selected", fromX + (toX - fromX) / 2d, fromY + (toY - fromY) / 2d - titleSmall.getFontHeight() / 2d, 0xAAAAAA);
        }

        toY = height - getPadding();
        fromY = toY - widRHeight;
        Renderer.R2D.renderRoundedQuad(stack, pillColor, fromX, fromY, toX, toY, 5, 20);
        double texDim = widRHeight - padding * 2;

        RenderSystem.enableBlend();
        RenderSystem.colorMask(false, false, false, true);
        RenderSystem.clearColor(0.0F, 0.0F, 0.0F, 0.0F);
        RenderSystem.clear(GL40C.GL_COLOR_BUFFER_BIT, false);
        RenderSystem.colorMask(true, true, true, true);
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        Renderer.R2D.renderRoundedQuadInternal(stack.peek().getPositionMatrix(),
            0,
            0,
            0,
            1,
            fromX + padding,
            fromY + padding,
            fromX + padding + texDim,
            fromY + padding + texDim,
            5,
            20);

        RenderSystem.blendFunc(GL40C.GL_DST_ALPHA, GL40C.GL_ONE_MINUS_DST_ALPHA);
        RenderSystem.setShaderTexture(0, currentAccountTextureLoaded ? currentAccountTexture : DefaultSkinHelper.getTexture());
        if (currentAccountTextureLoaded) {
            Renderer.R2D.renderTexture(stack, fromX + padding, fromY + padding, texDim, texDim, 0, 0, 64, 64, 64, 64);
        } else {
            Renderer.R2D.renderTexture(stack, fromX + padding, fromY + padding, texDim, texDim, 8, 8, 8, 8, 64, 64);
        }
        RenderSystem.defaultBlendFunc();
        String uuid = CoffeeMain.client.getSession().getUuid();
        double uuidWid = FontRenderers.getRenderer().getStringWidth(uuid);
        double maxWid = leftWidth - texDim - padding * 3;
        if (uuidWid > maxWid) {
            double threeDotWidth = FontRenderers.getRenderer().getStringWidth("...");
            uuid = FontRenderers.getRenderer().trimStringToWidth(uuid, maxWid - 1 - threeDotWidth);
            uuid += "...";
        }
        AltContainer.PropEntry[] props = new AltContainer.PropEntry[] {
            new AltContainer.PropEntry(CoffeeMain.client.getSession().getUsername(), FontRenderers.getCustomSize(22), 0xFFFFFF),
            new AltContainer.PropEntry(uuid, FontRenderers.getRenderer(), 0xAAAAAA) };
        float propsOffset = (float) (fromY + padding);
        for (AltContainer.PropEntry prop : props) {
            prop.cfr.drawString(stack, prop.name, (float) (fromX + padding + texDim + padding), propsOffset, prop.color, false);
            propsOffset += prop.cfr.getMarginHeight();
        }

        super.renderInternal(stack, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        Rectangle rBounds = new Rectangle(getPadding(), getHeaderHeight(), getPadding() + (width - (getPadding() + leftWidth + getPadding() * 2)), height);

        boolean a = super.mouseClicked(mouseX, mouseY, button);
        if (a || isLoggingIn.get()) {
            return true;
        }
        if (mouseX >= rBounds.getX() && mouseX <= rBounds.getX1() && mouseY >= rBounds.getY() && mouseY <= rBounds.getY1()) {
            for (AltContainer alt : getAlts()) {
                alt.clicked(mouseX, mouseY + scrollSmooth);
            }
        }
        return false;
    }

    static class AltStorage {
        final String email;
        final String password;
        final AddScreenOverlay.AccountType type;
        String tags;
        String cachedName;
        String accessToken;
        UUID cachedUuid;
        boolean valid = true;
        boolean didSuccessfulLogin = false;

        public AltStorage(String n, String e, String p, UUID u, AddScreenOverlay.AccountType type, String tags) {
            this.cachedName = n;
            this.email = e;
            this.password = p;
            this.cachedUuid = u;
            this.type = type;
            this.tags = tags;
        }
    }

    static class SessionEditor extends ClientScreen {
        static final double widgetWid = 300;
        static double widgetHei = 0;
        final Session session;
        final ClientScreen parent;
        final double padding = 5;
        final FontAdapter title = FontRenderers.getCustomSize(40);
        RoundTextFieldWidget access, name, uuid;
        RoundButton save;

        public SessionEditor(ClientScreen parent, Session s) {
            super(MSAAFramebuffer.MAX_SAMPLES);
            this.session = s;
            this.parent = parent;
        }

        @Override
        protected void init() {
            RoundButton exit = new RoundButton(RoundButton.STANDARD, width - 20 - 5, 5, 20, 20, "X", () -> Objects.requireNonNull(client).setScreen(parent));
            addDrawableChild(exit);
            double y = height / 2d - widgetHei / 2d + padding + title.getMarginHeight() + FontRenderers.getRenderer().getMarginHeight() + padding;
            RoundTextFieldWidget accessToken = new RoundTextFieldWidget(width / 2d - (widgetWid - padding * 2) / 2d, y, widgetWid - padding * 2, 20, "Access token");
            accessToken.setText(session.getAccessToken());
            y += accessToken.getHeight() + padding;
            RoundTextFieldWidget username = new RoundTextFieldWidget(width / 2d - (widgetWid - padding * 2) / 2d, y, widgetWid - padding * 2, 20, "Username");
            username.setText(session.getUsername());
            y += username.getHeight() + padding;
            RoundTextFieldWidget uuid = new RoundTextFieldWidget(width / 2d - (widgetWid - padding * 2) / 2d, y, widgetWid - padding * 2, 20, "UUID");
            uuid.setText(session.getUuid());
            y += uuid.getHeight() + padding;
            RoundButton save = new RoundButton(RoundButton.STANDARD, width / 2d - (widgetWid - padding * 2) / 2d, y, widgetWid - padding * 2, 20, "Save", () -> {
                ISessionMixin sa = (ISessionMixin) session;
                sa.setUsername(username.get());
                sa.setAccessToken(accessToken.get());
                sa.setUuid(uuid.get());
                Objects.requireNonNull(client).setScreen(parent);
            });
            y += 20 + padding;
            this.save = save;
            access = accessToken;
            name = username;
            this.uuid = uuid;
            addDrawableChild(save);
            addDrawableChild(access);
            addDrawableChild(name);
            addDrawableChild(uuid);
            widgetHei = y - (height / 2d - widgetHei / 2d);
            super.init();
        }

        @Override
        public void renderInternal(MatrixStack stack, int mouseX, int mouseY, float delta) {
            if (parent != null) {
                parent.renderInternal(stack, mouseX, mouseY, delta);
            }

            double y = height / 2d - widgetHei / 2d + padding + title.getMarginHeight() + FontRenderers.getRenderer().getMarginHeight() + padding;
            access.setY(y);
            y += access.getHeight() + padding;
            name.setY(y);
            y += name.getHeight() + padding;
            uuid.setY(y);
            y += uuid.getHeight() + padding;
            save.setY(y);
            y += 20 + padding;
            widgetHei = y - (height / 2d - widgetHei / 2d);


            save.setEnabled(!name.get().isEmpty() && !uuid.get().isEmpty()); // enable when both name and uuid are set
            Renderer.R2D.renderQuad(stack, backgroundOverlay, 0, 0, width, height);


            double centerX = width / 2d;
            double centerY = height / 2d;
            Renderer.R2D.renderRoundedQuad(stack,
                overlayBackground,
                centerX - widgetWid / 2d,
                centerY - widgetHei / 2d,
                centerX + widgetWid / 2d,
                centerY + widgetHei / 2d,
                5,
                20);
            stack.push();

            double originX = width / 2d - widgetWid / 2d;
            double originY = height / 2d - widgetHei / 2d;
            title.drawString(stack, "Edit session", (float) (originX + padding), (float) (originY + padding), 0xFFFFFF, false);
            FontRenderers.getRenderer()
                .drawString(stack, "Edit your user session here", (float) (originX + padding), (float) (originY + padding + title.getMarginHeight()), 0xAAAAAA, false);
            stack.pop();
            super.renderInternal(stack, mouseX, mouseY, delta);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            for (Element child : children()) {
                child.mouseClicked(-1, -1, button);
            }
            return super.mouseClicked(mouseX, mouseY, button);
        }
    }

    class TagEditor extends ClientScreen implements FastTickable {
        final List<RoundButton> tags = new ArrayList<>();
        final double widgetWidth = 300;
        final Screen parent;
        RoundTextFieldWidget tagName;
        RoundButton add;
        double widgetHeight = 0;
        double widgetStartX, widgetStartY;

        public TagEditor(Screen parent) {
            super(MSAAFramebuffer.MAX_SAMPLES);
            this.parent = parent;
        }

        @Override
        public void onFastTick() {
            for (RoundButton tag : tags) {
                tag.onFastTick();
            }
            add.onFastTick();
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            for (RoundButton tag : new ArrayList<>(tags)) {
                tag.mouseClicked(mouseX, mouseY, button);
            }
            tagName.mouseClicked(mouseX, mouseY, button);
            add.mouseClicked(mouseX, mouseY, button);
            return super.mouseClicked(mouseX, mouseY, button);
        }

        @Override
        public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
            tagName.keyPressed(keyCode, scanCode, modifiers);
            return super.keyPressed(keyCode, scanCode, modifiers);
        }

        @Override
        public boolean charTyped(char chr, int modifiers) {
            tagName.charTyped(chr, modifiers);
            return super.charTyped(chr, modifiers);
        }

        @Override
        protected void init() {
            RoundButton exit = new RoundButton(RoundButton.DANGER, width - 20 - 5, 5, 20, 20, "X", this::close);
            addDrawableChild(exit);
            this.tags.clear();
            String tags = selectedAlt.storage.tags;
            double xOffset = 5;
            double yOffset = 0;
            double widgetsHeight = 20;
            double padding = 5;
            List<String> parsedTags = new ArrayList<>(Arrays.stream(tags.split(",")).map(String::trim).filter(s -> !s.isEmpty()).toList());
            for (String s : parsedTags) {
                if (s.isEmpty()) {
                    continue;
                }
                float width = FontRenderers.getRenderer().getStringWidth(s) + 2 + 4;
                if (xOffset + width > (widgetWidth - 5)) {
                    xOffset = 5;
                    yOffset += FontRenderers.getRenderer().getMarginHeight() + 4 + 2;
                }
                RoundButton inst = new RoundButton(RoundButton.STANDARD, xOffset, yOffset, width, FontRenderers.getRenderer().getMarginHeight() + 4, s, () -> {
                    parsedTags.remove(s);
                    selectedAlt.storage.tags = String.join(",", parsedTags);
                    init();
                });
                this.tags.add(inst);
                xOffset += width + 2;
            }
            double yBase = parsedTags.isEmpty() ? 0 : yOffset + FontRenderers.getRenderer().getMarginHeight() + 4 + padding;
            tagName = new RoundTextFieldWidget(5, yBase, widgetWidth - 60 - padding * 3, widgetsHeight, "Tag name");
            add = new RoundButton(RoundButton.SUCCESS, tagName.getX() + tagName.getWidth() + padding, yBase, 60, widgetsHeight, "Add", () -> {
                if (tagName.get().isEmpty()) {
                    return;
                }
                parsedTags.add(tagName.get());
                tagName.set("");
                selectedAlt.storage.tags = String.join(",", parsedTags);
                init();
            });
            widgetHeight = add.getY() + add.getHeight() + padding * 2;

            widgetStartX = width / 2d - widgetWidth / 2d;
            widgetStartY = height / 2d - widgetHeight / 2d;
            double widgetStartY = this.widgetStartY + padding;

            for (RoundButton tag : this.tags) {
                tag.setX(tag.getX() + widgetStartX);
                tag.setY(tag.getY() + widgetStartY);
            }
            tagName.setX(tagName.getX() + widgetStartX);
            tagName.setY(tagName.getY() + widgetStartY);
            add.setX(add.getX() + widgetStartX);
            add.setY(add.getY() + widgetStartY);
        }

        @Override
        public void close() {
            client.setScreen(parent);
        }

        @Override
        public void renderInternal(MatrixStack stack, int mouseX, int mouseY, float delta) {
            if (parent != null) {
                parent.render(stack, mouseX, mouseY, delta);
            }
            Renderer.R2D.renderQuad(stack, backgroundOverlay, 0, 0, width, height);
            Renderer.R2D.renderRoundedQuad(stack, overlayBackground, widgetStartX, widgetStartY, widgetStartX + widgetWidth, widgetStartY + widgetHeight, 5, 20);
            for (RoundButton tag : tags) {
                tag.render(stack, mouseX, mouseY, delta);
            }
            tagName.render(stack, mouseX, mouseY, delta);
            add.render(stack, mouseX, mouseY, delta);
            super.renderInternal(stack, mouseX, mouseY, delta);
        }
    }

    class AddScreenOverlay extends ClientScreen implements FastTickable {
        static final double widgetWid = 200;
        static int accountTypeI = 0;
        static double widgetHei = 0;
        final List<RoundButton> buttons = new ArrayList<>();
        final ClientScreen parent;
        final double padding = 5;
        final FontAdapter title = FontRenderers.getCustomSize(40);
        RoundTextFieldWidget email;
        RoundTextFieldWidget passwd;
        RoundButton type;
        RoundButton add;

        public AddScreenOverlay(ClientScreen parent) {
            super(MSAAFramebuffer.MAX_SAMPLES);
            this.parent = parent;
        }

        @Override
        protected void init() {
            RoundButton exit = new RoundButton(RoundButton.STANDARD, width - 20 - 5, 5, 20, 20, "X", () -> Objects.requireNonNull(client).setScreen(parent));
            buttons.add(exit);
            email = new RoundTextFieldWidget(width / 2d - (widgetWid - padding * 2) / 2d,
                height / 2d - widgetHei / 2d + padding,
                widgetWid - padding * 2,
                20,
                "E-Mail or username");
            passwd = new RoundTextFieldWidget(width / 2d - (widgetWid - padding * 2) / 2d,
                height / 2d - widgetHei / 2d + padding * 2 + 20,
                widgetWid - padding * 2,
                20,
                "Password");
            type = new RoundButton(RoundButton.STANDARD, 0, 0, widgetWid / 2d - padding * 1.5, 20, "Type: " + AccountType.values()[accountTypeI].s, this::cycle);
            add = new RoundButton(RoundButton.SUCCESS, 0, 0, widgetWid / 2d - padding * 1.5, 20, "Add", this::add);
        }

        void add() {
            AltStorage as = new AltStorage("Unknown", email.getText(), passwd.getText(), UUID.randomUUID(), AccountType.values()[accountTypeI], "");
            AltContainer ac = new AltContainer(-1, -1, 0, as);
            ac.renderX = -1;
            ac.renderY = -1;
            alts.add(ac);
            Objects.requireNonNull(client).setScreen(parent);
        }

        boolean isAddApplicable() {
            if (AccountType.values()[accountTypeI] == AccountType.CRACKED && !email.getText().isEmpty()) {
                return true;
            } else {
                return !email.getText().isEmpty() && !passwd.getText().isEmpty();
            }
        }

        void cycle() {
            accountTypeI++;
            if (accountTypeI >= AccountType.values().length) {
                accountTypeI = 0;
            }
            type.setText("Type: " + AccountType.values()[accountTypeI].s);
        }

        @Override
        public void onFastTick() {
            for (RoundButton button : buttons) {
                button.onFastTick();
            }
            type.onFastTick();
            add.onFastTick();
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            for (RoundButton themedButton : buttons) {
                themedButton.mouseClicked(mouseX, mouseY, button);
            }
            email.mouseClicked(mouseX, mouseY, button);
            passwd.mouseClicked(mouseX, mouseY, button);
            type.mouseClicked(mouseX, mouseY, button);
            add.mouseClicked(mouseX, mouseY, button);
            return super.mouseClicked(mouseX, mouseY, button);
        }

        @Override
        public void renderInternal(MatrixStack stack, int mouseX, int mouseY, float delta) {
            if (parent != null) {
                parent.renderInternal(stack, mouseX, mouseY, delta);
            }
            Renderer.R2D.renderQuad(stack, backgroundOverlay, 0, 0, width, height);

            for (RoundButton button : buttons) {
                button.render(stack, mouseX, mouseY, delta);
            }
            double centerX = width / 2d;
            double centerY = height / 2d;
            Renderer.R2D.renderRoundedQuad(stack,
                overlayBackground,
                centerX - widgetWid / 2d,
                centerY - widgetHei / 2d,
                centerX + widgetWid / 2d,
                centerY + widgetHei / 2d,
                5,
                20);
            stack.push();

            double originX = width / 2d - widgetWid / 2d;
            double originY = height / 2d - widgetHei / 2d;
            title.drawString(stack, "Add account", (float) (originX + padding), (float) (originY + padding), 0xFFFFFF, false);
            FontRenderers.getRenderer()
                .drawString(stack, "Add another account here", (float) (originX + padding), (float) (originY + padding + title.getMarginHeight()), 0xAAAAAA, false);
            email.setX(originX + padding);
            email.setY(originY + padding + title.getMarginHeight() + FontRenderers.getRenderer().getMarginHeight() + padding);
            email.setWidth(widgetWid - padding * 2);
            email.render(stack, mouseX, mouseY, 0);
            passwd.setX(originX + padding);
            passwd.setY(originY + padding + title.getMarginHeight() + FontRenderers.getRenderer().getMarginHeight() + padding + email.getHeight() + padding);
            passwd.setWidth(widgetWid - padding * 2);
            passwd.render(stack, mouseX, mouseY, 0);
            type.setX(originX + padding);
            type.setY(originY + padding + title.getMarginHeight() + FontRenderers.getRenderer()
                .getMarginHeight() + padding + email.getHeight() + padding + passwd.getHeight() + padding);
            type.render(stack, mouseX, mouseY, delta);
            add.setX(originX + padding + type.getWidth() + padding);
            add.setY(originY + padding + title.getMarginHeight() + FontRenderers.getRenderer()
                .getMarginHeight() + padding + email.getHeight() + padding + passwd.getHeight() + padding);
            add.setEnabled(isAddApplicable());
            add.render(stack, mouseX, mouseY, delta);
            widgetHei = padding + title.getMarginHeight() + FontRenderers.getRenderer()
                .getMarginHeight() + padding + email.getHeight() + padding + passwd.getHeight() + padding + type.getHeight() + padding;
            stack.pop();
        }

        @Override
        public boolean charTyped(char chr, int modifiers) {
            email.charTyped(chr, modifiers);
            passwd.charTyped(chr, modifiers);
            return super.charTyped(chr, modifiers);
        }

        @Override
        public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
            email.keyPressed(keyCode, scanCode, modifiers);
            passwd.keyPressed(keyCode, scanCode, modifiers);
            return super.keyPressed(keyCode, scanCode, modifiers);
        }

        enum AccountType {
            MOJANG("Mojang"), MICROSOFT("Microsoft"), CRACKED("Cracked");

            final String s;

            AccountType(String s) {
                this.s = s;
            }
        }
    }

    public class AltContainer {
        final AltStorage storage;
        Texture tex;
        float animProgress = 0;
        boolean isHovered = false;
        double x, y, width, renderX, renderY;


        public AltContainer(double x, double y, double width, AltStorage inner) {
            this.storage = inner;
            this.tex = new Texture(DefaultSkinHelper.getTexture(inner.cachedUuid));
            this.x = x;
            this.y = y;
            this.width = width;
            UUID uuid = inner.cachedUuid;
            if (texCache.containsKey(uuid)) {
                this.tex = texCache.get(uuid);
            } else {
                downloadTexture();
            }
        }

        void downloadTexture() {
            this.tex = PlayerHeadResolver.resolve(this.storage.cachedUuid);
        }

        public double getHeight() {
            return 60d;
        }

        public void login() {
            if (storage.didSuccessfulLogin) {
                return;
            }
            try {
                MinecraftAuthenticator auth = new MinecraftAuthenticator();
                MinecraftToken token = switch (storage.type) {
                    case MOJANG -> auth.login(storage.email, storage.password);
                    case MICROSOFT -> auth.loginWithMicrosoft(storage.email, storage.password);
                    case CRACKED -> null;
                };
                if (token == null && storage.password.equals("")) {
                    storage.valid = true;
                    storage.cachedUuid = UUID.randomUUID();
                    storage.cachedName = storage.email;
                    storage.accessToken = "coffeelmao";
                    return;
                }
                if (token == null) {
                    throw new NullPointerException();
                }
                storage.accessToken = token.accessToken();
                MinecraftProfile profile = auth.getGameProfile(token);
                storage.cachedName = profile.username();
                storage.cachedUuid = profile.uuid();
                downloadTexture();
                storage.valid = true;
                storage.didSuccessfulLogin = true;
            } catch (Exception e) {
                e.printStackTrace();
                storage.valid = false;
            }
        }

        public void tickAnim() {
            double d = 0.04;
            if (!isHovered) {
                d *= -1;
            }
            animProgress += d;
            animProgress = MathHelper.clamp(animProgress, 0, 1);
            if (renderX != -1) {
                renderX = Transitions.transition(renderX, x, 7, 0.0001);
            }
            if (renderY != -1) {
                renderY = Transitions.transition(renderY, y, 7, 0.0001);
            }
        }

        boolean inBounds(double cx, double cy) {
            return cx >= renderX && cx < renderX + width && cy >= renderY && cy < renderY + getHeight();
        }

        double easeInOutQuint(double x) {
            return x < 0.5 ? 16 * x * x * x * x * x : 1 - Math.pow(-2 * x + 2, 5) / 2;
        }

        public void render(MatrixStack stack, double mx, double my) {
            isHovered = inBounds(mx, my);
            stack.push();
            double originX = -width / 2d;
            double originY = -getHeight() / 2d;
            stack.translate(renderX + width / 2d, renderY + getHeight() / 2d, 0);
            float animProgress = (float) easeInOutQuint(this.animProgress);
            stack.scale(MathHelper.lerp(animProgress, 1f, 0.99f), MathHelper.lerp(animProgress, 1f, 0.99f), 1f);
            Renderer.R2D.renderRoundedQuadWithShadow(stack, pillColor, originX, originY, originX + width, originY + getHeight(), 5, 20);
            double padding = 5;
            double texWidth = getHeight() - padding * 2;
            double texHeight = getHeight() - padding * 2;

            RenderSystem.enableBlend();
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.colorMask(false, false, false, true);
            RenderSystem.clearColor(0.0F, 0.0F, 0.0F, 0.0F);
            RenderSystem.clear(GL40C.GL_COLOR_BUFFER_BIT, false);
            RenderSystem.colorMask(true, true, true, true);
            RenderSystem.setShader(GameRenderer::getPositionColorShader);
            Renderer.R2D.renderRoundedQuadInternal(stack.peek().getPositionMatrix(),
                0,
                0,
                0,
                1,
                originX + padding,
                originY + padding,
                originX + padding + texWidth,
                originY + padding + texHeight,
                5,
                20);

            RenderSystem.blendFunc(GL40C.GL_DST_ALPHA, GL40C.GL_ONE_MINUS_DST_ALPHA);
            RenderSystem.setShaderTexture(0, tex);
            Renderer.R2D.renderTexture(stack, originX + padding, originY + padding, texWidth, texHeight, 0, 0, 64, 64, 64, 64);
            String mail;
            if (this.storage.type != AddScreenOverlay.AccountType.CRACKED) {
                mail = this.storage.email;
                String[] mailPart = mail.split("@");
                String domain = mailPart[mailPart.length - 1];
                String mailN = String.join("@", Arrays.copyOfRange(mailPart, 0, mailPart.length - 1));
                if (censorEmail) {
                    mailN = "*".repeat(mailN.length());
                }
                mail = mailN + "@" + domain;
            } else {
                mail = "No email bound";
            }
            PropEntry[] props = new PropEntry[] { new PropEntry(this.storage.type == AddScreenOverlay.AccountType.CRACKED ? this.storage.email : this.storage.cachedName,
                FontRenderers.getCustomSize(22),
                storage.valid ? 0xFFFFFF : 0xFF3333), new PropEntry("Email: " + mail, FontRenderers.getRenderer(), 0xAAAAAA)
                /*, new PropEntry("Type: " + this.storage.type.s, FontRenderers.getRenderer(), 0xAAAAAA)*/ };
            float propsOffset = (float) (getHeight() - (texHeight)) / 2f;
            for (PropEntry prop : props) {
                prop.cfr.drawString(stack, prop.name, (float) (originX + padding + texWidth + padding), (float) (originY + propsOffset), prop.color, false);
                propsOffset += prop.cfr.getFontHeight(prop.name);
            }
            if (isLoggingIn.get() && selectedAlt == this) {
                double fromTop = getHeight() / 2d;
                Renderer.R2D.renderLoadingSpinner(stack, 1f, originX + width - fromTop, originY + fromTop, 10, 1, 10);
            }
            double xOff = 0;
            for (String s : (storage.tags.isEmpty() ? "No tags" : storage.tags).split(",")) {
                String v = s.trim();
                if (v.isEmpty()) {
                    continue;
                }
                float w = FontRenderers.getRenderer().getStringWidth(v);
                float h = FontRenderers.getRenderer().getMarginHeight();
                float pad = 2;
                w += pad * 2;
                Renderer.R2D.renderRoundedQuad(stack,
                    new Color(30, 30, 30),
                    originX + padding + texWidth + padding + xOff,
                    originY + getHeight() - h - pad * 2 - padding,
                    originX + padding + texWidth + padding + xOff + w,
                    originY + getHeight() - padding,
                    5,
                    10);
                FontRenderers.getRenderer()
                    .drawString(stack,
                        v,
                        originX + padding + texWidth + padding + xOff + pad,
                        originY + getHeight() - pad - FontRenderers.getRenderer().getMarginHeight() - padding,
                        0xFFFFFF);
                xOff += w + 2;
            }

            stack.pop();
        }

        public void clicked(double mx, double my) {
            if (inBounds(mx, my)) {
                setSelectedAlt(this);
            }
        }

        public record PropEntry(String name, FontAdapter cfr, int color) {

        }
    }
}
