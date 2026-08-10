package com.marchive.marchive_backend.qdrant.controller;

import com.marchive.marchive_backend.qdrant.service.QdrantService;
import java.util.List;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/qdrant")
public class QdrantTestController {

    private final QdrantService qdrantService;

    public QdrantTestController(QdrantService qdrantService) {
        this.qdrantService = qdrantService;
    }

    // 테스트용 벡터 삽입
    @PostMapping("/test-insert")
    public String testInsert(@RequestBody InsertTestRequest request) {
        qdrantService.insertPostVector(
                request.postId(),
                request.igAccountId(),
                request.vector()
        );
        return "삽입 완료";
    }

    // 테스트용 유사도 검색
    @PostMapping("/test-search")
    public List<Long> testSearch(@RequestBody SearchTestRequest request) {
        return qdrantService.searchSimilarPosts(
                request.igAccountId(),
                request.vector()
        );
    }

    public record InsertTestRequest(Long postId, Long igAccountId, List<Float> vector) {
    }

    public record SearchTestRequest(Long igAccountId, List<Float> vector) {
    }
}