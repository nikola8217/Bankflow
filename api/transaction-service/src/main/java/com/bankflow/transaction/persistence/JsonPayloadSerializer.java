package com.bankflow.transaction.persistence;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

@Component
@RequiredArgsConstructor
public class JsonPayloadSerializer {

    private final JsonMapper jsonMapper;

    public String serialize(Object obj) {
        return jsonMapper.writeValueAsString(obj);
    }

    public <T> T deserialize(String json, Class<T> clazz) {
        return jsonMapper.readValue(json, clazz);
    }
}