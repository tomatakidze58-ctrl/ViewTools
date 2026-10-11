package dev.viewtools.camera;

import dev.viewtools.ViewToolsClient;
import dev.viewtools.config.ViewToolsConfig;
import net.minecraft.client.Camera;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

/** Moves only a camera position. Never moves an entity or sends a camera position to a server. */
public final class FreecamController {
    private final ViewToolsClient mod;
    private boolean active;
    private ClientLevel level;
    private LocalPlayer player;
    private Vec3 position = Vec3.ZERO;
    private Vec3 previousPosition = Vec3.ZERO;
    private Vec3 lastPlayerPosition = Vec3.ZERO;
    private float yaw;
    private float pitch;

    public FreecamController(ViewToolsClient mod) {
        this.mod = mod;
    }

    public boolean isActive() {
        return active;
    }

    public void toggle(Minecraft client) {
        if (active) {
            disable(client, "Freecam OFF");
            return;
        }
        if (client.level == null || client.player == null || !client.player.isAlive()) {
            return;
        }
        if (client.player.isPassenger() || client.player.isSleeping() || client.player.isUsingItem()) {
            ViewToolsClient.notify(client, "Leave your vehicle/bed and stop using the item first");
            return;
        }
        if (client.getCameraEntity() != client.player) {
            ViewToolsClient.notify(client, "Freecam needs the normal player camera");
            return;
        }

        level = client.level;
        player = client.player;
        Camera camera = client.gameRenderer.mainCamera();
        position = camera.isInitialized() ? camera.position() : player.getEyePosition();
        previousPosition = position;
        lastPlayerPosition = player.position();
        yaw = camera.isInitialized() ? camera.yRot() : player.getYRot();
        pitch = camera.isInitialized() ? camera.xRot() : player.getXRot();
        active = true;
        discardInteractionClicks(client.options);
        if (client.gameMode != null) {
            client.gameMode.stopDestroyBlock();
        }
        ViewToolsClient.notify(client, "Freecam ON");
    }

    public void disable(Minecraft client, String message) {
        if (!active) {
            return;
        }
        active = false;
        level = null;
        player = null;
        discardInteractionClicks(client.options);
        if (message != null) {
            ViewToolsClient.notify(client, message);
        }
    }

    public void tick(Minecraft client) {
        if (!active) {
            return;
        }
        if (client.level != level || client.player != player || player == null
                || !player.isAlive() || player.isRemoved() || player.isPassenger()
                || player.isSleeping() || client.getCameraEntity() != player) {
            disable(client, "Freecam OFF");
            return;
        }
        if (player.hurtTime > 0 || player.position().distanceToSqr(lastPlayerPosition) > 16.0) {
            disable(client, "Freecam OFF: player damaged or moved");
            return;
        }
        lastPlayerPosition = player.position();
        previousPosition = position;
        discardInteractionClicks(client.options);
        if (client.isPaused() || client.gui.screen() != null || !client.isWindowActive()) {
            return;
        }

        Options keys = client.options;
        double forward = axis(keys.keyUp, keys.keyDown);
        double right = axis(keys.keyRight, keys.keyLeft);
        double up = axis(keys.keyJump, keys.keyShift);
        Vec3 direction = Vec3.directionFromRotation(pitch, yaw).scale(forward)
                .add(Vec3.directionFromRotation(0.0F, yaw + 90.0F).scale(right))
                .add(0.0, up, 0.0);
        if (direction.lengthSqr() < 1.0e-10) {
            return;
        }

        ViewToolsConfig config = mod.config();
        double speed = config.freecamSpeed / 20.0;
        if (keys.keySprint.isDown()) {
            speed *= config.sprintMultiplier;
        }
        Vec3 next = position.add(direction.normalize().scale(speed));
        Vec3 anchor = player.getEyePosition();
        Vec3 offset = next.subtract(anchor);
        if (offset.lengthSqr() > config.freecamMaxDistance * config.freecamMaxDistance) {
            next = anchor.add(offset.normalize().scale(config.freecamMaxDistance));
        }
        position = next;
    }

    private static double axis(KeyMapping positive, KeyMapping negative) {
        return (positive.isDown() ? 1.0 : 0.0) - (negative.isDown() ? 1.0 : 0.0);
    }

    /** MouseHandler has already applied sensitivity, smoothing and inverted mouse options. */
    public void turn(double dx, double dy) {
        yaw = Mth.wrapDegrees(yaw + (float) (dx * 0.15));
        pitch = Mth.clamp(pitch + (float) (dy * 0.15), -90.0F, 90.0F);
    }

    public Vec3 interpolatedPosition(float partialTick) {
        return previousPosition.lerp(position, Mth.clamp(partialTick, 0.0F, 1.0F));
    }

    public float yaw() {
        return yaw;
    }

    public float pitch() {
        return pitch;
    }

    /** Keep queued attack/use/drop/swap clicks from firing when freecam ends. */
    public static void discardInteractionClicks(Options options) {
        discard(options.keyAttack);
        discard(options.keyUse);
        discard(options.keyPickItem);
        discard(options.keyDrop);
        discard(options.keySwapOffhand);
    }

    private static void discard(KeyMapping mapping) {
        while (mapping.consumeClick()) {
            // Consume clicks only; movement key-down state remains available to the camera.
        }
    }
}
