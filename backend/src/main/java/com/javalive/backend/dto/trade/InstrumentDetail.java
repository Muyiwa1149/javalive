package com.javalive.backend.dto.trade;

import java.util.List;

public record InstrumentDetail(InstrumentSummary instrument, List<UserTradeSummary> openTrades, List<UserTradeSummary> closedTrades) {
}
