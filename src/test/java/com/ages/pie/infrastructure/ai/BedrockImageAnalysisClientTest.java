package com.ages.pie.infrastructure.ai;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class BedrockImageAnalysisClientTest {
    private MockRestServiceServer server;
    private BedrockImageAnalysisClient client;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();
        client = new BedrockImageAnalysisClient("test-key", "us-east-2", "google.gemma-3-27b-it", builder.build());
    }

    @Test
    void sendsOneImageAndFixedPromptWithNinetySixOutputTokens() {
        server.expect(requestTo("https://bedrock-runtime.us-east-2.amazonaws.com/model/google.gemma-3-27b-it/converse"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("Authorization", "Bearer test-key"))
                .andExpect(jsonPath("$.inferenceConfig.maxTokens").value(96))
                .andExpect(jsonPath("$.inferenceConfig.temperature").value(0))
                .andExpect(jsonPath("$.messages.length()").value(1))
                .andExpect(jsonPath("$.messages[0].content[0].image.source.bytes").value("AQID"))
                .andExpect(jsonPath("$.messages[0].content[1].text").value(containsString("category: vestido,blazer")))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andRespond(withSuccess(response("{\"category\":\"vestido\",\"style\":\"casual\",\"color\":\"verde\"}", "end_turn"), MediaType.APPLICATION_JSON));
        var result = client.analyze(new byte[] {1, 2, 3}, "png");
        assertThat(result.category()).isEqualTo("vestido");
        assertThat(result.style()).isEqualTo("casual");
        assertThat(result.color()).isEqualTo("verde");
        server.verify();
    }

    @Test
    void parsesJsonInsideModelMarkdownFence() {
        server.expect(requestTo(org.hamcrest.Matchers.any(String.class))).andRespond(withSuccess(
                response("```json\n{\"category\":\"camisa\",\"style\":\"classico\",\"color\":\"branco\"}\n```", "end_turn"), MediaType.APPLICATION_JSON));
        assertThat(client.analyze(new byte[] {1}, "jpeg").category()).isEqualTo("camisa");
        server.verify();
    }

    @Test
    void doesNotRetryProviderFailuresOrExposeProviderBody() {
        server.expect(requestTo(org.hamcrest.Matchers.any(String.class))).andRespond(
                withStatus(HttpStatus.TOO_MANY_REQUESTS).body("provider-internal-details"));
        assertThatThrownBy(() -> client.analyze(new byte[] {1}, "jpeg"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageNotContaining("provider-internal-details")
                .satisfies(error -> assertThat(((ResponseStatusException) error).getStatusCode()).isEqualTo(HttpStatus.BAD_GATEWAY));
        server.verify();
    }

    @Test
    void rejectsMalformedOrTruncatedOutput() {
        server.expect(requestTo(org.hamcrest.Matchers.any(String.class))).andRespond(
                withSuccess(response("not json", "end_turn"), MediaType.APPLICATION_JSON));
        server.expect(requestTo(org.hamcrest.Matchers.any(String.class))).andRespond(
                withSuccess(response("{}", "max_tokens"), MediaType.APPLICATION_JSON));
        assertThatThrownBy(() -> client.analyze(new byte[] {1}, "png")).isInstanceOf(ResponseStatusException.class);
        assertThatThrownBy(() -> client.analyze(new byte[] {1}, "png")).isInstanceOf(ResponseStatusException.class);
        server.verify();
    }

    @Test
    void missingCredentialDoesNotCallProvider() {
        client = new BedrockImageAnalysisClient("", "us-east-2", "google.gemma-3-27b-it", RestClient.create());
        assertThatThrownBy(() -> client.analyze(new byte[] {1}, "png"))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> assertThat(((ResponseStatusException) error).getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE));
        server.verify();
    }

    private String response(String text, String stopReason) {
        return tools.jackson.databind.json.JsonMapper.builder().build().writeValueAsString(java.util.Map.of(
                "output", java.util.Map.of("message", java.util.Map.of("content", java.util.List.of(java.util.Map.of("text", text)))),
                "stopReason", stopReason, "usage", java.util.Map.of("inputTokens", 1200, "outputTokens", 30)));
    }
}
