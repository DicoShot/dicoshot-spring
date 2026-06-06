package io.dicoshot.boot4;

import io.dicoshot.core.DicoshotClient;
import io.dicoshot.core.exception.DicoshotException;
import io.dicoshot.core.message.DiscordMessage;
import io.dicoshot.core.message.DiscordMessageJsonWriter;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

public class RestClientDicoshotClient implements DicoshotClient {

    private final RestClient restClient;
    private final String webhookUrl;

    public RestClientDicoshotClient(RestClient restClient, String webhookUrl) {
        this.restClient = restClient;
        this.webhookUrl = webhookUrl;
    }

    @Override
    public void send(DiscordMessage message) {
        String body = DiscordMessageJsonWriter.toJson(message);
        try {
            restClient.post()
                    .uri(webhookUrl)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientResponseException e) {
            throw new DicoshotException(
                    "Discord webhook returned " + e.getStatusCode() + ": " + e.getResponseBodyAsString(), e);
        } catch (RuntimeException e) {
            throw new DicoshotException("Failed to send Discord webhook", e);
        }
    }
}
