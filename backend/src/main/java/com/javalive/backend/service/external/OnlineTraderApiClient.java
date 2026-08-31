package com.javalive.backend.service.external;

import com.javalive.backend.service.settings.SettingsService;
import com.javalive.backend.web.exception.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Map;

/**
 * Shared proxy client for the external app.getonlinetrader.pro SaaS — mirrors the source app's
 * {@code PingServer::fetctApi} trait exactly (same base URL, same {@code token} header carrying
 * {@code settings.merchant_key}). Backs Membership/Courses, MT4-external and the external
 * Signal-Provider broadcast (the local, non-external MT4 subscription feature does NOT use this).
 *
 * <p><b>Known limitation</b>: the migrated live settings have {@code merchant_key = NULL} — this
 * integration was never actually configured in the source production app either. Per the explicit
 * decision recorded in docs/parity-checklist.md, this client and the features built on it are
 * wired correctly but have not been exercised against a real upstream response; configure a real
 * merchant key in Settings to light them up.
 */
@Service
public class OnlineTraderApiClient {

    private static final String BASE_URL = "https://app.getonlinetrader.pro/api/v1";

    private final RestClient restClient = RestClient.create();
    private final SettingsService settingsService;

    public OnlineTraderApiClient(SettingsService settingsService) {
        this.settingsService = settingsService;
    }

    public Map<String, Object> get(String path, Map<String, ?> queryParams) {
        return exchange(client -> client.get()
                .uri(uriBuilder -> {
                    var builder = uriBuilder.path(path);
                    queryParams.forEach(builder::queryParam);
                    return builder.build();
                })
                .header("token", merchantKey())
                .retrieve()
                .body(RESPONSE_TYPE));
    }

    public Map<String, Object> post(String path, Object body) {
        return exchange(client -> client.post().uri(path)
                .header("token", merchantKey())
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(RESPONSE_TYPE));
    }

    public Map<String, Object> delete(String path) {
        return exchange(client -> client.delete().uri(path)
                .header("token", merchantKey())
                .retrieve()
                .body(RESPONSE_TYPE));
    }

    private static final org.springframework.core.ParameterizedTypeReference<Map<String, Object>> RESPONSE_TYPE =
            new org.springframework.core.ParameterizedTypeReference<>() {};

    private Map<String, Object> exchange(java.util.function.Function<RestClient, Map<String, Object>> call) {
        try {
            return call.apply(restClient.mutate().baseUrl(BASE_URL).build());
        } catch (RestClientException e) {
            throw new ApiException(HttpStatus.BAD_GATEWAY, "The external trading services API is currently unavailable.");
        }
    }

    private String merchantKey() {
        String key = settingsService.get().getMerchantKey();
        if (key == null || key.isBlank()) {
            throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE,
                    "This feature requires an external API key to be configured in Settings.");
        }
        return key;
    }
}
