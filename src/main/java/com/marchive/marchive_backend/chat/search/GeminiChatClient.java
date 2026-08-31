package com.marchive.marchive_backend.chat.search;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class GeminiChatClient {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${gemini.api-key}")
    private String apiKey;

    // 답변 생성용 모델. 임베딩 모델과는 다른 모델을 씀
    private static final String MODEL_NAME = "gemini-3.6-flash";
    private static final String GENERATE_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/" + MODEL_NAME + ":generateContent";

    public GeminiChatClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String generateAnswer(String userQuestion, String contextInfo) {
        String prompt = buildPrompt(userQuestion, contextInfo);

        String requestBody = """
                {
                  "contents": [{ "parts": [{ "text": "%s" }] }]
                }
                """.formatted(escapeForJson(prompt));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-goog-api-key", apiKey);

        String responseBody = restTemplate.postForObject(
                GENERATE_URL,
                new HttpEntity<>(requestBody, headers),
                String.class
        );

        try {
            JsonNode response = objectMapper.readTree(responseBody);
            return response.path("candidates").get(0)
                    .path("content").path("parts").get(0)
                    .path("text").asText();
        } catch (Exception e) {
            throw new IllegalStateException("Gemini 답변 생성 응답 파싱에 실패했습니다: " + responseBody, e);
        }
    }

    private String buildPrompt(String userQuestion, String contextInfo) {
        return """
                사용자가 '%s'라고 검색했습니다.
                아래는 검색된 관련 게시물 정보입니다:
                
                %s
                
                이 정보를 바탕으로 사용자에게 자연스럽게 답변해주세요.
                게시물을 그대로 옮기지 말고, 검색 결과를 요약해서 설명해주세요.
                """.formatted(userQuestion, contextInfo);
    }

    private String escapeForJson(String text) {
        return text.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n");
    }
}
