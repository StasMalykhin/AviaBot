package com.github.StasMalykhin.aviabot.service;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

/**
 * Принимает токен для доступа к Aviasales API и создает HTTP заголовки для всех запросов бота.
 *
 * @author Stanislav Malykhin
 */
@Service
@ConfigurationProperties(prefix = "travelpayouts")
@Getter
@Setter
public class APIConnectionService {
    private String accessToken;

    public HttpEntity<HttpHeaders> createRequestWithHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("X-Access-Token", accessToken);
        return new HttpEntity<>(headers);
    }
}
