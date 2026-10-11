package dev.viewtools.ui;

import dev.viewtools.ViewToolsClient;
import dev.viewtools.config.ViewToolsConfig;
import java.util.Locale;
import java.util.function.Supplier;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class SettingsScreen extends Screen {
    private final ViewToolsClient mod;
    private int panelLeft;
    private int panelWidth;
    private int top;

    public SettingsScreen(ViewToolsClient mod) {
        super(Component.literal("ViewTools Settings"));
        this.mod = mod;
    }

    @Override
    protected void init() {
        ViewToolsConfig config = mod.config();
        panelWidth = Math.min(360, width - 24);
        panelLeft = (width - panelWidth) / 2;
        top = Math.max(36, (height - 200) / 2);

        button(0, 0, () -> "Freecam: " + onOff(mod.freecam().isActive()),
                () -> mod.freecam().toggle(minecraft));
        button(1, 0, () -> "ESP: " + onOff(mod.espEnabled()),
                () -> mod.setEspEnabled(!mod.espEnabled()));
        button(0, 1, () -> String.format(Locale.ROOT, "Speed: %.1f blocks/s", config.freecamSpeed),
                () -> config.freecamSpeed = next(config.freecamSpeed, 4, 8, 12, 20, 32, 48));
        button(1, 1, () -> String.format(Locale.ROOT, "ESP range: %.0fm", config.espRange),
                () -> config.espRange = next(config.espRange, 32, 64, 96, 128, 192, 256));
        button(0, 2, () -> "Players: " + onOff(config.players),
                () -> config.players = !config.players);
        button(1, 2, () -> "Hostile mobs: " + onOff(config.hostileMobs),
                () -> config.hostileMobs = !config.hostileMobs);
        button(0, 3, () -> "Other living mobs: " + onOff(config.passiveMobs),
                () -> config.passiveMobs = !config.passiveMobs);
        button(1, 3, () -> "Dropped items: " + onOff(config.droppedItems),
                () -> config.droppedItems = !config.droppedItems);
        button(0, 4, () -> "Names + distance: " + onOff(config.showNames),
                () -> config.showNames = !config.showNames);
        button(1, 4, () -> "Through walls: " + onOff(config.throughWalls),
                () -> config.throughWalls = !config.throughWalls);
        button(0, 5, () -> "Filled boxes: " + onOff(config.filledBoxes),
                () -> config.filledBoxes = !config.filledBoxes);
        button(1, 5, () -> "Status HUD: " + onOff(config.showHud),
                () -> config.showHud = !config.showHud);
        addRenderableWidget(Button.builder(Component.literal("Done"), button -> onClose())
                .bounds(panelLeft, top + 150, panelWidth, 20).build());
    }

    private void button(int column, int row, Supplier<String> label, Runnable action) {
        int columnWidth = (panelWidth - 8) / 2;
        addRenderableWidget(Button.builder(Component.literal(label.get()), button -> {
            action.run();
            mod.config().save();
            button.setMessage(Component.literal(label.get()));
        }).bounds(panelLeft + column * (columnWidth + 8), top + row * 24, columnWidth, 20).build());
    }

    private static String onOff(boolean value) {
        return value ? "ON" : "OFF";
    }

    private static double next(double value, double... choices) {
        for (double choice : choices) {
            if (choice > value + 0.001) {
                return choice;
            }
        }
        return choices[0];
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        extractTransparentBackground(graphics);
        super.extractRenderState(graphics, mouseX, mouseY, delta);
        graphics.centeredText(font, title, width / 2, top - 22, 0xFF76DCFF);
        graphics.centeredText(font, "Rebind controls in Options > Controls > Key Binds",
                width / 2, top + 180, 0xFFB7C1CD);
    }

    @Override
    public void onClose() {
        mod.config().save();
        minecraft.gui.setScreen(null);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
