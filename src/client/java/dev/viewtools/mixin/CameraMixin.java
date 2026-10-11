package dev.viewtools.mixin;

import dev.viewtools.ViewToolsClient;
import dev.viewtools.camera.FreecamController;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
abstract class CameraMixin {
    @Shadow private boolean detached;
    @Shadow protected abstract void setRotation(float yaw, float pitch);
    @Shadow protected abstract void setPosition(Vec3 position);

    // Override before update() prepares the culling frustum, not after it.
    @Inject(method = "alignWithEntity(F)V", at = @At("HEAD"), cancellable = true)
    private void viewtools$align(float partialTick, CallbackInfo ci) {
        ViewToolsClient mod = ViewToolsClient.get();
        if (mod != null && mod.freecam().isActive()) {
            FreecamController freecam = mod.freecam();
            setRotation(freecam.yaw(), freecam.pitch());
            setPosition(freecam.interpolatedPosition(partialTick));
            detached = true;
            ci.cancel();
        }
    }

    @Inject(method = "extractRenderState", at = @At("RETURN"))
    private void viewtools$disableOcclusion(CameraRenderState state, float partialTick, CallbackInfo ci) {
        ViewToolsClient mod = ViewToolsClient.get();
        if (mod != null && mod.freecam().isActive()) {
            // Detached cameras can be inside a solid block; use the spectator-style culling rule.
            state.smartCull = false;
        }
    }
}
