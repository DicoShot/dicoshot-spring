package io.disender.boot4;

import io.disender.core.DisenderClient;
import io.disender.core.exception.DisenderException;
import io.disender.core.message.DiscordMessage;
import io.disender.core.message.DiscordMessageJsonWriter;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

public class RestClientDisenderClient implements DisenderClient {

    private final RestClient restClient;
    private final String webhookUrl;

    public RestClientDisenderClient(RestClient restClient, String webhookUrl) {
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
            throw new DisenderException(
                    "Discord webhook returned " + e.getStatusCode() + ": " + e.getResponseBodyAsString(), e);
        } catch (RuntimeException e) {
            throw new DisenderException("Failed to send Discord webhook", e);
        }
    }
}
