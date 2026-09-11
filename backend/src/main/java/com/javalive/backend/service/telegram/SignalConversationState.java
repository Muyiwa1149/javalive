package com.javalive.backend.service.telegram;

/** Per-chat conversation state — Java replacement for BotMan's cache-backed {@code Conversation} object. */
class SignalConversationState {

    enum Step {
        AWAITING_CHOICE, POST_DIRECTION, POST_PAIR, POST_PRICE, POST_TP1, POST_TP2, POST_SL,
        POST_PUBLISH_CONFIRM, POST_CONTINUE, UPDATE_REF, UPDATE_RESULT, UPDATE_CONFIRM
    }

    Step step = Step.AWAITING_CHOICE;
    String tradeDirection;
    String pair;
    String price;
    String tp1;
    String tp2;
    String sl;
    String ref;
    String result;
}
