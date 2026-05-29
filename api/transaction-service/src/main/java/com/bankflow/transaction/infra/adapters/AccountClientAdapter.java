package com.bankflow.transaction.infra.adapters;

import com.bankflow.shared.exceptions.ServiceUnavailableException;
import com.bankflow.transaction.business.ports.IAccountClient;
import com.bankflow.transaction.core.valueObjects.AccountSnapshot;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

@Component
public class AccountClientAdapter implements IAccountClient {

    private final RestTemplate restTemplate;

    @Value("${account.service.url}")
    private String accountServiceUrl;

    public AccountClientAdapter(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public AccountSnapshot getAccount(UUID accountId, String token) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", token);
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            return restTemplate.exchange(
                    accountServiceUrl + "/api/accounts/" + accountId,
                    HttpMethod.GET,
                    entity,
                    AccountSnapshot.class
            ).getBody();
        } catch (Exception e) {
            throw new ServiceUnavailableException("Account service");
        }
    }
}