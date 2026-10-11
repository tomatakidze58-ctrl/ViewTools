package dev.viewtools.mixin;

import dev.viewtools.ViewToolsClient;
import net.minecraft.client.player.ClientInput;
import net.minecraft.client.player.KeyboardInput;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.phys.Vec2;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardInput.class)
abstract class KeyboardInputMixin extends ClientInput {
    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void viewtools$neutralInput(CallbackInfo ci) {
        ViewToolsClient mod = ViewToolsClient.get();
        if (mod != null && mod.freecam().isActive()) {
            // Camera movement reads the key mappings directly; the real player gets empty input.
            keyPresses = Input.EMPTY;
            moveVector = Vec2.ZERO;
            ci.cancel();
        }
    }
}
