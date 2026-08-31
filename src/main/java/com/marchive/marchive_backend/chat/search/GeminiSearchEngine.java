package com.marchive.marchive_backend.chat.search;

import com.marchive.marchive_backend.bookmark.domain.Post;
import com.marchive.marchive_backend.bookmark.repository.PostRepository;
import com.marchive.marchive_backend.qdrant.service.QdrantService;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Primary
@Component
public class GeminiSearchEngine implements SearchEngine {

    private static final int MAX_RESULTS = 3;

    private final GeminiEmbeddingClient embeddingClient;
    private final QdrantService qdrantService;   // 기존에 만든 Qdrant 검색 서비스
    private final GeminiChatClient chatClient;
    private final PostRepository postRepository;

    public GeminiSearchEngine(
            GeminiEmbeddingClient embeddingClient,
            QdrantService qdrantService,
            GeminiChatClient chatClient,
            PostRepository postRepository
    ) {
        this.embeddingClient = embeddingClient;
        this.qdrantService = qdrantService;
        this.chatClient = chatClient;
        this.postRepository = postRepository;
    }

    @Override
    public SearchResult search(String searchText, Long igAccountId) {
        try {
            List<Float> queryVector = embeddingClient.embedText(searchText);
            // igAccountId로 필터링해서 검색 (QdrantService 실제 메서드로 교체 예정)
            List<Long> postIds = qdrantService.searchSimilarPosts(igAccountId, queryVector);

            if (postIds.isEmpty()) {
                return SearchResult.success("관련된 게시물을 찾지 못했어요.", List.of());
            }

            // 상위 3개만 사용
            List<Long> topPostIds = postIds.stream().limit(MAX_RESULTS).toList();
            List<Post> similarPosts = postRepository.findAllById(topPostIds);

            String contextInfo = buildContextInfo(similarPosts);
            String answer = chatClient.generateAnswer(searchText, contextInfo);

            return SearchResult.success(answer, similarPosts);
        } catch (Exception e) {
            e.printStackTrace();
            return SearchResult.failure();
        }
    }

    private String buildContextInfo(List<Post> posts) {
        return posts.stream()
                .map(p -> "- 작성자: " + p.getAuthorHandle() + ", 캡션: " + p.getCaption())
                .collect(Collectors.joining("\n"));
    }
}
