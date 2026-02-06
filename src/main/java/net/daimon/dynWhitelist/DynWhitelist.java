package net.daimon.dynWhitelist;

import com.mojang.authlib.GameProfile;
import net.daimon.dynWhitelist.config.Config;
import net.daimon.dynWhitelist.mixin.ServerLoginNetworkHandlerAccessorMixin;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerLoginConnectionEvents;
import net.minecraft.text.Text;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class DynWhitelist implements ModInitializer {
    public static final String MOD_ID = "dynWhitelist";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        Config.load();

        ServerLoginConnectionEvents.QUERY_START.register((handler, server, sender, synchronizer) -> {
            GameProfile profile =
                    ((ServerLoginNetworkHandlerAccessorMixin) handler).getProfile();

            UUID uuid = profile.getId();
            System.out.println(uuid.toString());

            synchronizer.waitFor(CompletableFuture.runAsync(() -> {

                int status = checkApi(uuid);
                System.out.println(status);

                if (status == 403) {
                    handler.disconnect(Text.literal("Доступ запрещён"));
                } else if (status != 200) {
                    handler.disconnect(Text.literal("Ошибка проверки доступа"));
                }

            }));

        });

    }

    private static int checkApi(UUID uuid) {
        try {
            HttpClient client;
            client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(Config.getConnectTimeout())).build();

            String json = "{ \"uuid\": \"" + uuid + "\" }";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(Config.getApiUrl()))
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofSeconds(Config.getReadTimeout()))
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();
            HttpResponse<Void> response =
                    client.send(request, HttpResponse.BodyHandlers.discarding());
            client.close();
            return response.statusCode();
        } catch (
                Exception e) {
            LOGGER.error("Unable to connect to DynWhitelist API\n", e);
            return 500; // любая ошибка = отказ
        }
    }


}