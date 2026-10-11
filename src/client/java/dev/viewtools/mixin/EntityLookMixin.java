package dev.viewtools.mixin;

import dev.viewtools.ViewToolsClient;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
abstract class EntityLookMixin {
    @Inject(method = "turn(DD)V", at = @At("HEAD"), cancellable = true)
    private void viewtools$turnCamera(double dx, double dy, CallbackInfo ci) {
        ViewToolsClient mod = ViewToolsClient.get();
        if (mod != null && mod.freecam().isActive()
                && (Object) this == Minecraft.getInstance().player) {
            mod.freecam().turn(dx, dy);
            ci.cancel();
        }
    }
}
