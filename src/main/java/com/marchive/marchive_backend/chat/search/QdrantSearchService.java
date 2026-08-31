package com.marchive.marchive_backend.chat.search;

import com.marchive.marchive_backend.bookmark.domain.Post;
import com.marchive.marchive_backend.bookmark.repository.PostRepository;
import io.qdrant.client.QdrantClient;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;

public class QdrantSearchService {

    private final QdrantClient qdrantClient;
    private final PostRepository postRepository;

    @Value("${qdrant.collection}")
    private String collectionName;

    public QdrantSearchService(QdrantClient qdrantClient, PostRepository postRepository) {
        this.qdrantClient = qdrantClient;
        this.postRepository = postRepository;
    }

    // igAccountId로 필터링해서, 유사도 상위 N개 게시물 검색
    public List<Post> searchSimilarPosts(List<Float> queryVector, Long igAccountId, int limit) {
        // Qdrant 검색 로직 (payload에 저장된 igAccountId로 필터링)
        // 검색 결과의 postId들을 모아서 MySQL에서 Post 엔티티로 조회

        List<Long> postIds = performQdrantSearch(queryVector, igAccountId, limit);
        return postRepository.findAllById(postIds);
    }

    private List<Long> performQdrantSearch(List<Float> vector, Long igAccountId, int limit) {
        // 실제 Qdrant 검색 API 호출
        // (QdrantService에 이전에 만든 search 로직 재사용/이동)
        return List.of(); // TODO: 실제 구현
    }
}
