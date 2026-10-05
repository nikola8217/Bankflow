package com.bankflow.account.web;

import com.bankflow.shared.handlers.ErrorHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ErrorHandlerTest {

    @RestController
    static class TestController {
        @GetMapping("/items/{id}")
        public String get(@PathVariable UUID id) { return "ok"; }

        @PostMapping("/items")
        public String create(@RequestBody Map<String, Object> body) { return "ok"; }

        @GetMapping("/boom")
        public String boom() { throw new IllegalStateException("secret internal detail"); }
    }

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new TestController())
                .setControllerAdvice(new ErrorHandler())
                .build();
    }

    @Test
    void invalidUuidInPathIs400() throws Exception {
        mockMvc.perform(get("/items/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Invalid value for 'id'"));
    }

    @Test
    void unknownRouteIs404() throws Exception {
        mockMvc.perform(get("/does-not-exist"))
                .andExpect(status().isNotFound());
    }

    @Test
    void wrongHttpMethodIs405() throws Exception {
        mockMvc.perform(delete("/items/" + UUID.randomUUID()))
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    void wrongContentTypeIs415() throws Exception {
        mockMvc.perform(post("/items").contentType(MediaType.TEXT_PLAIN).content("hello"))
                .andExpect(status().isUnsupportedMediaType());
    }

    @Test
    void brokenJsonIs400() throws Exception {
        mockMvc.perform(post("/items").contentType(MediaType.APPLICATION_JSON).content("{broken"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void realBugIs500WithoutLeakingDetails() throws Exception {
        mockMvc.perform(get("/boom"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Something went wrong"))
                .andExpect(content().string(not(containsString("secret"))));
    }
}