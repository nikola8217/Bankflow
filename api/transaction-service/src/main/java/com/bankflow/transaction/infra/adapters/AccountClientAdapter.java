package com.bankflow.transaction.infra.adapters;

import com.bankflow.shared.exceptions.ServiceUnavailableException;
import com.bankflow.transaction.business.ports.IAccountClient;
import com.bankflow.transaction.core.exceptions.AccountNotFoundException;
import com.bankflow.transaction.core.valueObjects.AccountSnapshot;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import java.util.UUID;

@Component
public class AccountClientAdapter implements IAccountClient {

    private final RestClient restClient;
    private final String internalApiKey;

    public AccountClientAdapter(RestClient accountRestClient,
                                @Value("${internal.api-key}") String internalApiKey) {
        this.restClient = accountRestClient;
        this.internalApiKey = internalApiKey;
    }

    @Override
    public AccountSnapshot getAccount(UUID accountId) {
        try {
            return restClient.get()
                    .uri("/internal/accounts/{id}", accountId)
                    .header("X-Internal-Api-Key", internalApiKey)
                    .retrieve()
                    .body(AccountSnapshot.class);
        } catch (HttpClientErrorException.NotFound e) {
            throw new AccountNotFoundException(accountId);
        } catch (ResourceAccessException | HttpServerErrorException e) {
            throw new ServiceUnavailableException("Account service");
        }
    }
}