package dev.viewtools.render;

import dev.viewtools.ViewToolsClient;
import dev.viewtools.config.ViewToolsConfig;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelExtractionContext;
import net.minecraft.client.Minecraft;
import net.minecraft.gizmos.GizmoProperties;
import net.minecraft.gizmos.GizmoStyle;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.gizmos.TextGizmo;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/** Uses vanilla 26.2 gizmos so depth handling works with the game's rendering backend. */
public final class EntityOverlay {
    private final ViewToolsClient mod;
    private int lastEntityCount;

    public EntityOverlay(ViewToolsClient mod) {
        this.mod = mod;
    }

    public void extract(LevelExtractionContext context) {
        clear();
        Minecraft client = Minecraft.getInstance();
        if (!mod.espEnabled() || client.player == null || context.level() == null) {
            return;
        }
        ViewToolsConfig config = mod.config();
        Vec3 cameraPosition = context.camera().position();
        double rangeSquared = config.espRange * config.espRange;
        List<Entity> entities = new ArrayList<>();
        for (Entity entity : context.level().entitiesForRendering()) {
            if (entity == client.player || entity.isRemoved() || !entity.isAlive()) {
                continue;
            }
            if (colorFor(entity, config) != 0 && entity.distanceToSqr(cameraPosition) <= rangeSquared) {
                entities.add(entity);
            }
        }
        // Prefer the nearest entities when the configured cap is reached.
        entities.sort(Comparator.comparingDouble(entity -> entity.distanceToSqr(cameraPosition)));

        try (Gizmos.TemporaryCollection ignored = client.levelRenderer.collectPerFrameRenderThreadGizmos()) {
            for (Entity entity : entities) {
                if (lastEntityCount >= config.maxEspEntities) {
                    break;
                }
                float partialTick = context.level().tickRateManager().isEntityFrozen(entity)
                        ? 1.0F : context.deltaTracker().getGameTimeDeltaPartialTick(true);
                Vec3 renderPosition = entity.getPosition(partialTick);
                AABB box = entity.getBoundingBox().move(renderPosition.subtract(entity.position()))
                        .inflate(0.035);
                int color = colorFor(entity, config);
                GizmoStyle style = config.filledBoxes
                        ? GizmoStyle.strokeAndFill(color, 1.5F, (color & 0x00FFFFFF) | 0x24000000)
                        : GizmoStyle.stroke(color, 1.5F);
                applyDepth(Gizmos.cuboid(box, style), config);

                if (config.showNames) {
                    double distance = Math.sqrt(entity.distanceToSqr(cameraPosition));
                    String label = entity.getName().getString()
                            + String.format(Locale.ROOT, " [%.0fm]", distance);
                    Vec3 labelPosition = new Vec3((box.minX + box.maxX) / 2.0,
                            box.maxY + 0.25, (box.minZ + box.maxZ) / 2.0);
                    applyDepth(Gizmos.billboardText(label, labelPosition,
                            TextGizmo.Style.forColorAndCentered(color).withScale(0.7F)), config);
                }
                lastEntityCount++;
            }
        }
    }

    private static void applyDepth(GizmoProperties properties, ViewToolsConfig config) {
        if (config.throughWalls) {
            properties.setAlwaysOnTop();
        }
    }

    private static int colorFor(Entity entity, ViewToolsConfig config) {
        if (entity instanceof Player) {
            return config.players ? ViewToolsConfig.argb(config.playerColor) : 0;
        }
        if (entity instanceof Enemy) {
            return config.hostileMobs ? ViewToolsConfig.argb(config.hostileColor) : 0;
        }
        if (entity instanceof LivingEntity) {
            return config.passiveMobs ? ViewToolsConfig.argb(config.passiveColor) : 0;
        }
        if (entity instanceof ItemEntity) {
            return config.droppedItems ? ViewToolsConfig.argb(config.itemColor) : 0;
        }
        return 0;
    }

    public int lastEntityCount() {
        return lastEntityCount;
    }

    public void clear() {
        lastEntityCount = 0;
    }
}
