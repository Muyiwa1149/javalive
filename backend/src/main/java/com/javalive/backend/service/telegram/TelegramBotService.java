package com.javalive.backend.service.telegram;

import com.javalive.backend.service.external.OnlineTraderApiClient;
import com.javalive.backend.service.settings.SettingsService;
import com.javalive.backend.web.exception.ApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Java port of the source app's Telegram bot (BotMan {@code TelegramDriver} + {@code SignalConversation}):
 * greeting handler ("Hi" starts a conversation) and the full post-signal / update-result flow, which
 * proxies to the external app.getonlinetrader.pro SaaS via {@link OnlineTraderApiClient} exactly like
 * source's {@code PingServer::fetctApi}.
 *
 * <p>Uses long-polling ({@code getUpdates}) on a {@code @Scheduled} loop rather than a webhook — no
 * public HTTPS URL is available in this deployment, same practical constraint as BotMan's driver would
 * have without one configured. Per-chat conversation state is an in-memory map (BotMan used a cache
 * driver for the same purpose) — acceptable for a single-instance deployment, same pattern this
 * codebase already uses for {@code SettingsService}/{@code IpBlacklistService}.
 *
 * <p><b>Known limitation, not fixable here</b>: the migrated {@code telegram_bot_api_token} returns
 * 401 Unauthorized from Telegram's API (revoked/rotated since the source DB dump) — same situation as
 * {@code merchant_key} for {@link OnlineTraderApiClient}. Wired correctly, cannot be exercised live;
 * configure a real bot token in Settings to light it up.
 */
@Service
public class TelegramBotService {

    private static final Logger log = LoggerFactory.getLogger(TelegramBotService.class);

    private final TelegramClient telegramClient = new TelegramClient();
    private final SettingsService settingsService;
    private final OnlineTraderApiClient onlineTraderApiClient;

    private final Map<Long, SignalConversationState> conversations = new ConcurrentHashMap<>();
    private final AtomicLong updateOffset = new AtomicLong(0);
    private final AtomicBoolean loggedFailure = new AtomicBoolean(false);

    public TelegramBotService(SettingsService settingsService, OnlineTraderApiClient onlineTraderApiClient) {
        this.settingsService = settingsService;
        this.onlineTraderApiClient = onlineTraderApiClient;
    }

    @Scheduled(initialDelay = 30_000, fixedDelay = 5_000)
    public void poll() {
        String token = settingsService.get().getTelegramBotApiToken();
        if (token == null || token.isBlank()) {
            return;
        }

        List<Map<String, Object>> updates;
        try {
            updates = telegramClient.getUpdates(token, updateOffset.get());
            loggedFailure.set(false);
        } catch (Exception e) {
            if (loggedFailure.compareAndSet(false, true)) {
                log.warn("Telegram getUpdates failed (will keep retrying silently): {}", e.getMessage());
            }
            return;
        }

        for (Map<String, Object> update : updates) {
            long updateId = ((Number) update.get("update_id")).longValue();
            updateOffset.set(updateId + 1);
            try {
                handleUpdate(token, update);
            } catch (Exception e) {
                log.error("Error handling Telegram update {}: {}", updateId, e.getMessage());
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void handleUpdate(String token, Map<String, Object> update) {
        Map<String, Object> message = (Map<String, Object>) update.get("message");
        Map<String, Object> callback = (Map<String, Object>) update.get("callback_query");

        if (callback != null) {
            Map<String, Object> chat = (Map<String, Object>) ((Map<String, Object>) callback.get("message")).get("chat");
            long chatId = ((Number) chat.get("id")).longValue();
            String data = String.valueOf(callback.get("data"));
            handleInput(token, chatId, data, true);
            return;
        }

        if (message == null || message.get("text") == null) {
            return;
        }
        Map<String, Object> chat = (Map<String, Object>) message.get("chat");
        long chatId = ((Number) chat.get("id")).longValue();
        String text = String.valueOf(message.get("text")).trim();

        if (!conversations.containsKey(chatId)) {
            if ("Hi".equalsIgnoreCase(text)) {
                conversations.put(chatId, new SignalConversationState());
                askWhatToDo(token, chatId);
            } else {
                telegramClient.sendMessage(token, chatId,
                        "Sorry, I did not understand this command. Enter Hi to start a conversation", null);
            }
            return;
        }

        if ("pause".equalsIgnoreCase(text)) {
            conversations.remove(chatId);
            telegramClient.sendMessage(token, chatId, "Conversation ended. Enter Hi to start again.", null);
            return;
        }

        handleInput(token, chatId, text, false);
    }

    private void handleInput(String token, long chatId, String value, boolean fromButton) {
        SignalConversationState state = conversations.get(chatId);
        if (state == null) {
            return;
        }

        switch (state.step) {
            case AWAITING_CHOICE -> {
                if ("post_signal".equals(value) || "Post Signal".equals(value)) {
                    state.step = SignalConversationState.Step.POST_DIRECTION;
                    telegramClient.sendMessage(token, chatId, "Alright lets go - Enter Trade direction: Buy/Sell", null);
                } else {
                    state.step = SignalConversationState.Step.UPDATE_REF;
                    telegramClient.sendMessage(token, chatId, "Alright lets go - Enter signal reference ID", null);
                }
            }
            case POST_DIRECTION -> {
                if (value.isBlank()) { telegramClient.sendMessage(token, chatId, "Please enter trade direction", null); return; }
                state.tradeDirection = value;
                state.step = SignalConversationState.Step.POST_PAIR;
                telegramClient.sendMessage(token, chatId, "Great - Now enter currency pair: eg EUR/USD", null);
            }
            case POST_PAIR -> {
                if (value.isBlank()) { telegramClient.sendMessage(token, chatId, "Please enter currency pair", null); return; }
                state.pair = value;
                state.step = SignalConversationState.Step.POST_PRICE;
                telegramClient.sendMessage(token, chatId, "You're getting close - Enter the Price", null);
            }
            case POST_PRICE -> {
                if (value.isBlank()) { telegramClient.sendMessage(token, chatId, "Please enter signal price", null); return; }
                state.price = value;
                state.step = SignalConversationState.Step.POST_TP1;
                telegramClient.sendMessage(token, chatId, "Enter the first Take Profit", null);
            }
            case POST_TP1 -> {
                if (value.isBlank()) { telegramClient.sendMessage(token, chatId, "Please enter the first take profit", null); return; }
                state.tp1 = value;
                state.step = SignalConversationState.Step.POST_TP2;
                telegramClient.sendMessage(token, chatId, "Enter the second Take Profit", null);
            }
            case POST_TP2 -> {
                if (value.isBlank()) { telegramClient.sendMessage(token, chatId, "Please enter the second take profit", null); return; }
                state.tp2 = value;
                state.step = SignalConversationState.Step.POST_SL;
                telegramClient.sendMessage(token, chatId, "One more thing - what is your stop loss?", null);
            }
            case POST_SL -> {
                if (value.isBlank()) { telegramClient.sendMessage(token, chatId, "Please enter your stop loss", null); return; }
                state.sl = value;
                state.step = SignalConversationState.Step.POST_PUBLISH_CONFIRM;
                telegramClient.sendMessage(token, chatId, "Great - that is all we need. Should I save and publish to channel?: Yes or No", null);
            }
            case POST_PUBLISH_CONFIRM -> {
                if ("yes".equalsIgnoreCase(value)) {
                    postAndPublishSignal(token, chatId, state);
                } else {
                    conversations.remove(chatId);
                    telegramClient.sendMessage(token, chatId, "Discarded. Enter Hi to start again.", null);
                }
            }
            case POST_CONTINUE -> {
                if ("another".equals(value) || "Post another signal".equals(value)) {
                    state.step = SignalConversationState.Step.POST_DIRECTION;
                    telegramClient.sendMessage(token, chatId, "Alright lets go - Enter Trade direction: Buy/Sell", null);
                } else {
                    conversations.remove(chatId);
                }
            }
            case UPDATE_REF -> {
                if (value.isBlank()) { telegramClient.sendMessage(token, chatId, "Please enter signal reference ID", null); return; }
                state.ref = value;
                lookupSignal(token, chatId, state);
            }
            case UPDATE_RESULT -> {
                if (value.isBlank()) { telegramClient.sendMessage(token, chatId, "Please enter signal result", null); return; }
                state.result = value;
                state.step = SignalConversationState.Step.UPDATE_CONFIRM;
                telegramClient.sendMessage(token, chatId, "Post Result to channel?: Yes or No", null);
            }
            case UPDATE_CONFIRM -> {
                if ("yes".equalsIgnoreCase(value) || "post_result".equals(value)) {
                    submitResult(token, chatId, state);
                } else {
                    telegramClient.sendMessage(token, chatId, "Result not posted. Enter pause to end conversation", null);
                }
            }
        }
    }

    private void askWhatToDo(String token, long chatId) {
        telegramClient.sendMessage(token, chatId, "Hello Welcome, what would you like to do: Post Signal or Update Result", null);
    }

    @SuppressWarnings("unchecked")
    private void lookupSignal(String token, long chatId, SignalConversationState state) {
        try {
            Map<String, Object> response = onlineTraderApiClient.get("/signal", Map.of("ref", state.ref));
            if (Boolean.TRUE.equals(response.get("error"))) {
                telegramClient.sendMessage(token, chatId, String.valueOf(response.get("message")), null);
                return;
            }
            Map<String, Object> data = (Map<String, Object>) response.get("data");
            Object signal = data != null ? data.get("signal") : null;
            if (signal == null) {
                telegramClient.sendMessage(token, chatId, "No Signal found with this reference ID, try again with another value.", null);
                state.step = SignalConversationState.Step.AWAITING_CHOICE;
                askWhatToDo(token, chatId);
            } else {
                telegramClient.sendMessage(token, chatId, String.valueOf(signal), null);
                state.step = SignalConversationState.Step.UPDATE_RESULT;
                telegramClient.sendMessage(token, chatId, "Enter the result for this signal", null);
            }
        } catch (ApiException e) {
            telegramClient.sendMessage(token, chatId, "The signal service is currently unavailable: " + e.getMessage(), null);
        }
    }

    @SuppressWarnings("unchecked")
    private void submitResult(String token, long chatId, SignalConversationState state) {
        try {
            Map<String, Object> response = onlineTraderApiClient.post("/update-result",
                    Map.of("ref", state.ref, "result", state.result));
            if (Boolean.TRUE.equals(response.get("error"))) {
                telegramClient.sendMessage(token, chatId, String.valueOf(response.get("message")), null);
                return;
            }
            Map<String, Object> data = (Map<String, Object>) response.get("data");
            if (data != null && data.get("chat_id") != null) {
                long targetChatId = ((Number) data.get("chat_id")).longValue();
                telegramClient.sendMessage(token, targetChatId, String.valueOf(data.get("message")), null);
            }
            conversations.remove(chatId);
            telegramClient.sendMessage(token, chatId, "Result posted successfully, Enter Hi to post/update another.", null);
        } catch (ApiException e) {
            telegramClient.sendMessage(token, chatId, "The signal service is currently unavailable: " + e.getMessage(), null);
        }
    }

    @SuppressWarnings("unchecked")
    private void postAndPublishSignal(String token, long chatId, SignalConversationState state) {
        try {
            Map<String, Object> response = onlineTraderApiClient.post("/post-signals", Map.of(
                    "direction", state.tradeDirection, "pair", state.pair, "price", state.price,
                    "tp1", state.tp1, "tp2", state.tp2, "sl1", state.sl));

            if (Boolean.TRUE.equals(response.get("error"))) {
                telegramClient.sendMessage(token, chatId, "Should I try again to save and publish to channel?: Yes or No", null);
                return;
            }

            Map<String, Object> data = (Map<String, Object>) response.get("data");
            Map<String, Object> signal = data != null ? (Map<String, Object>) data.get("signal") : null;
            Object signalId = signal != null ? signal.get("id") : null;

            if (signalId != null) {
                publishSignal(token, chatId, signalId);
            }

            state.step = SignalConversationState.Step.POST_CONTINUE;
            telegramClient.sendMessage(token, chatId, "Signal posted successfully, Enter pause to end or Enter Hi to post another.", null);
        } catch (ApiException e) {
            telegramClient.sendMessage(token, chatId, "The signal service is currently unavailable: " + e.getMessage(), null);
        }
    }

    @SuppressWarnings("unchecked")
    private void publishSignal(String token, long chatId, Object signalId) {
        try {
            Map<String, Object> response = onlineTraderApiClient.get("/publish-signals/" + signalId, Map.of());
            if (Boolean.TRUE.equals(response.get("error"))) {
                telegramClient.sendMessage(token, chatId, "Something went wrong publishing to channel, please try again", null);
                return;
            }
            Map<String, Object> data = (Map<String, Object>) response.get("data");
            if (data != null && data.get("chat_id") != null) {
                long targetChatId = ((Number) data.get("chat_id")).longValue();
                telegramClient.sendMessage(token, targetChatId, String.valueOf(data.get("message")), null);
            }
        } catch (ApiException e) {
            log.warn("Failed to publish signal {}: {}", signalId, e.getMessage());
        }
    }
}
