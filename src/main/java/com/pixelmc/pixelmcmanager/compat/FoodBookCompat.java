package com.pixelmc.pixelmcmanager.compat;

import com.google.gson.JsonObject;
import java.util.UUID;
import java.util.function.Predicate;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

/** Optional capability boundary. Fabric food sources need an audited API and committed-change events. */
public final class FoodBookCompat {
    private boolean installed;
    private String version = "";

    public FoodBookCompat(Predicate<UUID> admitted) {}

    public void start(MinecraftServer server) {
        clear();
        installed = FabricLoader.getInstance().isModLoaded("solcarrot");
        version = FabricLoader.getInstance().getModContainer("solcarrot")
                .map(mod -> mod.getMetadata().getVersion().getFriendlyString()).orElse("");
    }

    public void clear() { installed = false; version = ""; }
    public void login(ServerPlayer player) {}
    public void logout(UUID player) {}
    public void append(ServerPlayer player, JsonObject stats) {}

    public JsonObject capability() {
        JsonObject result = new JsonObject();
        result.addProperty("id", "solcarrot.effectiveFoods");
        result.addProperty("version", 1);
        result.addProperty("modVersion", version);
        result.addProperty("status", installed ? "unavailable" : "unsupported");
        return result;
    }
}
