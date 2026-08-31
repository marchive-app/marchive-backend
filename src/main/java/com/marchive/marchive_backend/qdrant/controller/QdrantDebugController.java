package com.marchive.marchive_backend.qdrant.controller;

import com.marchive.marchive_backend.chat.search.GeminiEmbeddingClient;
import com.marchive.marchive_backend.qdrant.service.QdrantService;
import com.marchive.marchive_backend.qdrant.service.QdrantService.ScoredResult;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/qdrant")
public class QdrantDebugController {

    private final GeminiEmbeddingClient embeddingClient;
    private final QdrantService qdrantService;

    public QdrantDebugController(GeminiEmbeddingClient embeddingClient, QdrantService qdrantService) {
        this.embeddingClient = embeddingClient;
        this.qdrantService = qdrantService;
    }

    @GetMapping("/debug-search")
    public List<ScoredResult> debugSearch(
            @RequestParam String query,
            @RequestParam Long igAccountId) {
        List<Float> vector = embeddingClient.embedText(query);
        return qdrantService.searchSimilarPosts(igAccountId, vector) != null
                ? qdrantService.searchSimilarPostsWithScore(igAccountId, vector)
                : List.of();
    }
}
