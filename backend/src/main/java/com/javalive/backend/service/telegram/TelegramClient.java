package com.javalive.backend.service.telegram;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;
import java.util.Map;

/**
 * Thin wrapper over the Telegram Bot HTTP API, replacing source's BotMan {@code TelegramDriver}.
 * Uses long-polling ({@code getUpdates}) rather than a webhook, since a webhook needs a public HTTPS
 * URL this local/self-hosted deployment doesn't have.
 */
class TelegramClient {

    private static final Logger log = LoggerFactory.getLogger(TelegramClient.class);
    private static final ParameterizedTypeReference<Map<String, Object>> MAP_TYPE = new ParameterizedTypeReference<>() {};
    private final RestClient restClient = RestClient.create();

    @SuppressWarnings("unchecked")
    List<Map<String, Object>> getUpdates(String token, long offset) {
        try {
            Map<String, Object> response = restClient.get()
                    .uri("https://api.telegram.org/bot{token}/getUpdates?offset={offset}&timeout=0",
                            token, offset)
                    .retrieve().body(MAP_TYPE);
            if (response == null || !Boolean.TRUE.equals(response.get("ok"))) {
                return List.of();
            }
            Object result = response.get("result");
            return result instanceof List<?> list ? (List<Map<String, Object>>) (List<?>) list : List.of();
        } catch (RestClientException e) {
            throw e;
        }
    }

    void sendMessage(String token, long chatId, String text, List<List<InlineButton>> buttons) {
        try {
            Map<String, Object> body = new java.util.HashMap<>();
            body.put("chat_id", chatId);
            body.put("text", text);
            if (buttons != null && !buttons.isEmpty()) {
                List<List<Map<String, String>>> rows = buttons.stream()
                        .map(row -> row.stream()
                                .map(b -> Map.of("text", b.label(), "callback_data", b.value()))
                                .toList())
                        .toList();
                body.put("reply_markup", Map.of("inline_keyboard", rows));
            }
            restClient.post().uri("https://api.telegram.org/bot{token}/sendMessage", token)
                    .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve().toBodilessEntity();
        } catch (RestClientException e) {
            log.warn("Failed to send Telegram message to chat {}: {}", chatId, e.getMessage());
        }
    }

    record InlineButton(String label, String value) {}
}
