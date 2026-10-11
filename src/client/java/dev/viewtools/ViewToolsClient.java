package dev.viewtools;

import com.mojang.blaze3d.platform.InputConstants;
import dev.viewtools.camera.FreecamController;
import dev.viewtools.config.ViewToolsConfig;
import dev.viewtools.render.EntityOverlay;
import dev.viewtools.ui.SettingsScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelExtractionEvents;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** All registration lives in the client entrypoint; no server mod is needed. */
public final class ViewToolsClient implements ClientModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("ViewTools");
    private static ViewToolsClient instance;

    private ViewToolsConfig config;
    private FreecamController freecam;
    private EntityOverlay overlay;
    private boolean espEnabled;
    private KeyMapping freecamKey;
    private KeyMapping espKey;
    private KeyMapping settingsKey;

    public static ViewToolsClient get() {
        return instance;
    }

    @Override
    public void onInitializeClient() {
        instance = this;
        config = ViewToolsConfig.load();
        freecam = new FreecamController(this);
        overlay = new EntityOverlay(this);

        KeyMapping.Category category = KeyMapping.Category.register(id("controls"));
        freecamKey = registerKey("freecam", GLFW.GLFW_KEY_F6, category);
        espKey = registerKey("esp", GLFW.GLFW_KEY_F7, category);
        settingsKey = registerKey("settings", GLFW.GLFW_KEY_F8, category);

        ClientTickEvents.START_CLIENT_TICK.register(this::tick);
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> reset(client));
        LevelExtractionEvents.END_EXTRACTION.register(overlay::extract);

        HudElementRegistry.addLast(id("status"), (graphics, delta) -> {
            Minecraft client = Minecraft.getInstance();
            if (!config.showHud || client.player == null || client.gui.hud.isHidden()
                    || (!freecam.isActive() && !espEnabled)) {
                return;
            }
            String status = "ViewTools" + (freecam.isActive() ? " | Freecam" : "")
                    + (espEnabled ? " | ESP (" + overlay.lastEntityCount() + ")" : "");
            graphics.text(client.font, status, 6, 6, 0xFF76DCFF);
        });
    }

    private KeyMapping registerKey(String name, int key, KeyMapping.Category category) {
        return KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.viewtools." + name, InputConstants.Type.KEYSYM, key, category));
    }

    private void tick(Minecraft client) {
        // Drain clicks even in menus so an old click cannot toggle a feature after closing one.
        boolean toggleFreecam = drain(freecamKey);
        boolean toggleEsp = drain(espKey);
        boolean openSettings = drain(settingsKey);
        if (client.player == null || client.level == null) {
            reset(client);
            return;
        }
        if (client.gui.screen() == null) {
            if (toggleFreecam) {
                freecam.toggle(client);
            }
            if (toggleEsp) {
                setEspEnabled(!espEnabled);
            }
            if (openSettings) {
                client.gui.setScreen(new SettingsScreen(this));
            }
        }
        freecam.tick(client);
    }

    private static boolean drain(KeyMapping mapping) {
        boolean clicked = false;
        while (mapping.consumeClick()) {
            clicked = true;
        }
        return clicked;
    }

    public void reset(Minecraft client) {
        if (freecam != null) {
            freecam.disable(client, null);
        }
        espEnabled = false;
        if (overlay != null) {
            overlay.clear();
        }
    }

    public void setEspEnabled(boolean enabled) {
        Minecraft client = Minecraft.getInstance();
        espEnabled = enabled && client.player != null && client.level != null;
        if (!espEnabled) {
            overlay.clear();
        }
        notify(client, "ESP " + (espEnabled ? "ON" : "OFF"));
    }

    public static void notify(Minecraft client, String message) {
        if (client.player != null) {
            client.gui.hud.setOverlayMessage(Component.literal(message), false);
        }
    }

    public ViewToolsConfig config() {
        return config;
    }

    public FreecamController freecam() {
        return freecam;
    }

    public boolean espEnabled() {
        return espEnabled;
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath("viewtools", path);
    }
}
