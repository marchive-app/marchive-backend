package com.marchive.marchive_backend.chat.search;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class GeminiEmbeddingClient {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${gemini.api-key}")
    private String apiKey;

    private static final String MODEL_NAME = "gemini-embedding-2";
    private static final String EMBED_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/" + MODEL_NAME + ":embedContent";

    public GeminiEmbeddingClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public List<Float> embedText(String text) {
        String requestBody = """
                {
                  "model": "models/%s",
                  "content": { "parts": [{ "text": "%s" }] },
                  "output_dimensionality": 1536
                }
                """.formatted(MODEL_NAME, text.replace("\"", "\\\""));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-goog-api-key", apiKey);

        String responseBody = restTemplate.postForObject(
                EMBED_URL,
                new HttpEntity<>(requestBody, headers),
                String.class
        );

        try {
            JsonNode response = objectMapper.readTree(responseBody);
            List<Float> vector = new ArrayList<>();
            JsonNode values = response.path("embedding").path("values");
            for (JsonNode v : values) {
                vector.add((float) v.asDouble());
            }
            return vector;
        } catch (Exception e) {
            throw new IllegalStateException("Gemini 임베딩 응답 파싱에 실패했습니다.: " + responseBody, e);
        }
    }
}
