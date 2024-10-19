package de.janoschbl.cozy.utils;

import de.janoschbl.cozy.Main;
import org.geysermc.floodgate.api.FloodgateApi;
import org.geysermc.floodgate.api.player.FloodgatePlayer;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;

public class UUIDFetcher {
    private static final ConcurrentMap<String, CacheEntry<UUID>> nameToUUID = new ConcurrentHashMap<>();
    private static final ConcurrentMap<UUID, CacheEntry<String>> uuidToName = new ConcurrentHashMap<>();
    private static final long TTL = 5 * 60 * 1000;
    private static final HttpClient client = HttpClient.newBuilder().build();

    public static UUID getUUID(String playerName) {
        if (playerName == null || playerName.isEmpty()) return null;
        String lowerName = playerName.toLowerCase();
        CacheEntry<UUID> entry = nameToUUID.get(lowerName);
        if (entry != null && entry.isExpired()) return entry.value;
        if (Main.getInstance().isFloodgateInstalled()) {
            FloodgateApi api = FloodgateApi.getInstance();
            if (api != null) {
                String prefix = api.getPlayerPrefix();
                if (lowerName.startsWith(prefix)) {
                    String gamertag = lowerName.substring(prefix.length());
                    try {
                        UUID uuid = api.getUuidFor(gamertag).get();
                        if (uuid != null && !lowerName.isEmpty()) {
                            nameToUUID.put(lowerName, new CacheEntry<>(uuid));
                            uuidToName.put(uuid, new CacheEntry<>(lowerName));
                            return uuid;
                        }
                    } catch (InterruptedException | ExecutionException e) {
                        Main.getInstance().getLogger().warning(e.toString());
                        return null;
                    }
                }
            }
        }
        UUID uuid = fetchUUID(playerName);
        if (uuid != null) nameToUUID.put(lowerName, new CacheEntry<>(uuid));
        return uuid;
    }

    public static String getNameFromUUID(UUID uuid) {
        if (uuid == null) return null;
        CacheEntry<String> entry = uuidToName.get(uuid);
        if (entry != null && entry.isExpired()) return entry.value;
        if (Main.getInstance().isFloodgateInstalled()) {
            FloodgateApi api = FloodgateApi.getInstance();
            if (api != null && api.isFloodgateId(uuid)) {
                FloodgatePlayer fp = api.getPlayer(uuid);
                if (fp != null) {
                    String name = fp.getCorrectUsername();
                    if (name != null && !name.isEmpty()) {
                        uuidToName.put(uuid, new CacheEntry<>(name));
                        nameToUUID.put(name.toLowerCase(), new CacheEntry<>(uuid));
                        return name;
                    }
                }
            }
        }
        String name = fetchName(uuid);
        if (name != null) {
            uuidToName.put(uuid, new CacheEntry<>(name));
            nameToUUID.put(name.toLowerCase(), new CacheEntry<>(uuid));
        }
        return name;
    }

    private static UUID fetchUUID(String playerName) {
        try {
            String url = "https://api.mojang.com/users/profiles/minecraft/" + playerName;
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(java.time.Duration.ofSeconds(5))
                    .GET()
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) return null;
            JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
            String id = json.get("id").getAsString();
            if (id == null || id.isEmpty()) return null;
            String uuidStr = id.replaceFirst("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})", "$1-$2-$3-$4-$5");
            return UUID.fromString(uuidStr);
        } catch (Exception e) {
            Main.getInstance().getLogger().warning("Error fetching UUID for " + playerName + ": " + e.getMessage());
            return null;
        }
    }

    private static String fetchName(UUID uuid) {
        try {
            String uuidStr = uuid.toString().replace("-", "");
            String url = "https://sessionserver.mojang.com/session/minecraft/profile/" + uuidStr;
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(java.time.Duration.ofSeconds(5))
                    .GET()
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) return null;
            JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
            if (json.has("name")) return json.get("name").getAsString();
            return null;
        } catch (Exception e) {
            Main.getInstance().getLogger().warning("Error fetching name for UUID " + uuid + ": " + e.getMessage());
            return null;
        }
    }

    private static class CacheEntry<T> {
        final T value;
        final long timestamp;
        CacheEntry(T value) { this.value = value; this.timestamp = System.currentTimeMillis(); }
        boolean isExpired() { return System.currentTimeMillis() - timestamp <= TTL; }
    }
}
