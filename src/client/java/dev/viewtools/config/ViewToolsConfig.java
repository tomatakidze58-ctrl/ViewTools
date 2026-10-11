package dev.viewtools.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.viewtools.ViewToolsClient;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import net.fabricmc.loader.api.FabricLoader;

public final class ViewToolsConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("viewtools.json");

    public double freecamSpeed = 12.0; // Blocks per second; the sprint key multiplies this.
    public double sprintMultiplier = 3.0;
    public double freecamMaxDistance = 256.0;
    public double espRange = 128.0;
    public int maxEspEntities = 256;
    public boolean players = true;
    public boolean hostileMobs = true;
    public boolean passiveMobs = false;
    public boolean droppedItems = false;
    public boolean throughWalls = true;
    public boolean filledBoxes = false;
    public boolean showNames = true;
    public boolean showHud = true;
    public String playerColor = "#65D6FF";
    public String hostileColor = "#FF6262";
    public String passiveColor = "#79E78D";
    public String itemColor = "#FFD56A";

    public static ViewToolsConfig load() {
        if (!Files.exists(FILE)) {
            ViewToolsConfig defaults = new ViewToolsConfig();
            defaults.save();
            return defaults;
        }
        try (Reader reader = Files.newBufferedReader(FILE, StandardCharsets.UTF_8)) {
            ViewToolsConfig loaded = GSON.fromJson(reader, ViewToolsConfig.class);
            if (loaded == null) {
                throw new IOException("Empty configuration");
            }
            loaded.validate();
            return loaded;
        } catch (IOException | RuntimeException exception) {
            // Leave an unreadable file alone; the settings screen can explicitly replace it.
            ViewToolsClient.LOGGER.warn("Cannot read viewtools.json; using defaults", exception);
            return new ViewToolsConfig();
        }
    }

    public void validate() {
        freecamSpeed = finiteClamp(freecamSpeed, 0.5, 100.0, 12.0);
        sprintMultiplier = finiteClamp(sprintMultiplier, 1.0, 10.0, 3.0);
        freecamMaxDistance = finiteClamp(freecamMaxDistance, 16.0, 512.0, 256.0);
        espRange = finiteClamp(espRange, 8.0, 256.0, 128.0);
        maxEspEntities = Math.max(1, Math.min(1024, maxEspEntities));
        playerColor = validColor(playerColor, "#65D6FF");
        hostileColor = validColor(hostileColor, "#FF6262");
        passiveColor = validColor(passiveColor, "#79E78D");
        itemColor = validColor(itemColor, "#FFD56A");
    }

    private static double finiteClamp(double value, double min, double max, double fallback) {
        return Double.isFinite(value) ? Math.max(min, Math.min(max, value)) : fallback;
    }

    private static String validColor(String value, String fallback) {
        return value != null && value.matches("#[0-9a-fA-F]{6}") ? value : fallback;
    }

    public static int argb(String color) {
        return 0xFF000000 | Integer.parseInt(color.substring(1), 16);
    }

    public void save() {
        validate();
        Path temporary = FILE.resolveSibling("viewtools.json.tmp");
        try {
            Files.createDirectories(FILE.getParent());
            Files.writeString(temporary, GSON.toJson(this) + "\n", StandardCharsets.UTF_8);
            try {
                Files.move(temporary, FILE, StandardCopyOption.REPLACE_EXISTING,
                        StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException exception) {
                Files.move(temporary, FILE, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException exception) {
            ViewToolsClient.LOGGER.warn("Cannot save viewtools.json", exception);
        }
    }
}
