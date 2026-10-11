package dev.viewtools.mixin;

import dev.viewtools.ViewToolsClient;
import dev.viewtools.camera.FreecamController;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Minecraft.class)
abstract class MinecraftInteractionMixin {
    private static boolean viewtools$freecamActive() {
        ViewToolsClient mod = ViewToolsClient.get();
        return mod != null && mod.freecam().isActive();
    }

    @Inject(method = "handleKeybinds", at = @At("HEAD"))
    private void viewtools$discardClicks(CallbackInfo ci) {
        if (viewtools$freecamActive()) {
            FreecamController.discardInteractionClicks(((Minecraft) (Object) this).options);
        }
    }

    @Inject(method = "startAttack", at = @At("HEAD"), cancellable = true)
    private void viewtools$blockAttack(CallbackInfoReturnable<Boolean> cir) {
        if (viewtools$freecamActive()) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "continueAttack", at = @At("HEAD"), cancellable = true)
    private void viewtools$blockBreaking(boolean held, CallbackInfo ci) {
        if (viewtools$freecamActive()) {
            ci.cancel();
        }
    }

    @Inject(method = "startUseItem", at = @At("HEAD"), cancellable = true)
    private void viewtools$blockUse(CallbackInfo ci) {
        if (viewtools$freecamActive()) {
            ci.cancel();
        }
    }
}
