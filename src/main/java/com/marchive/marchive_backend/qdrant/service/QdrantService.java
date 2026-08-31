package com.marchive.marchive_backend.qdrant.service;

import io.qdrant.client.QdrantClient;
import io.qdrant.client.grpc.Collections.Distance;
import io.qdrant.client.grpc.Collections.VectorParams;
import io.qdrant.client.grpc.JsonWithInt.Value;
import io.qdrant.client.grpc.Points.Condition;
import io.qdrant.client.grpc.Points.FieldCondition;
import io.qdrant.client.grpc.Points.Filter;
import io.qdrant.client.grpc.Points.Match;
import io.qdrant.client.grpc.Points.PointId;
import io.qdrant.client.grpc.Points.PointStruct;
import io.qdrant.client.grpc.Points.ScoredPoint;
import io.qdrant.client.grpc.Points.SearchPoints;
import io.qdrant.client.grpc.Points.Vector;
import io.qdrant.client.grpc.Points.Vectors;
import io.qdrant.client.grpc.Points.WithPayloadSelector;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import org.springframework.stereotype.Service;

@Service
public class QdrantService {

    private final QdrantClient qdrantClient;
    private final String COLLECTION_NAME = "instagram_posts";

    public QdrantService(QdrantClient qdrantClient) {
        this.qdrantClient = qdrantClient;
    }

    public void createCollection() {
        try {
            qdrantClient.createCollectionAsync(
                    COLLECTION_NAME,
                    VectorParams.newBuilder()
                            .setSize(1536)
                            .setDistance(Distance.Cosine)
                            .build()
            ).get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // 스레드 인터럽트 상태 복구
            throw new RuntimeException("Qdrant 컬렉션 생성 중 스레드가 중단되었습니다.", e);
        } catch (ExecutionException e) {
            // 이미 컬렉션이 존재하는 경우 발생하는 에러는 무시하거나 로깅 처리
            System.err.println("Qdrant 컬렉션 생성 실패 또는 이미 존재함: " + e.getMessage());
        }
    }

    public void insertPostVector(Long postId, Long igAccountId, List<Float> vectorValues) {
        // 1. 페이로드(메타데이터) 구성
        Map<String, Value> payload = Map.of(
                "post_id", Value.newBuilder().setIntegerValue(postId).build(),
                "ig_account_id", Value.newBuilder().setIntegerValue(igAccountId).build()
        );

        // 2. 포인트(하나의 데이터 행) 생성
        PointStruct point = PointStruct.newBuilder()
                .setId(PointId.newBuilder().setUuid(UUID.randomUUID().toString()).build())
                .setVectors(Vectors.newBuilder()
                        .setVector(Vector.newBuilder().addAllData(vectorValues).build())
                        .build())
                .putAllPayload(payload)
                .build();

        // 3. Qdrant에 저장
        try {
            qdrantClient.upsertAsync(COLLECTION_NAME, List.of(point)).get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("저장 중 스레드가 중단되었습니다.", e);
        } catch (java.util.concurrent.ExecutionException e) {
            throw new RuntimeException("Qdrant 데이터 저장 실패: " + e.getMessage(), e);
        }
    }

    public List<Long> searchSimilarPosts(Long igAccountId, List<Float> searchVector) {
        // 1. 검색 조건 필터: 해당 ig_account_id를 가진 데이터만 대상으로 함
        Filter filter = Filter.newBuilder()
                .addMust(Condition.newBuilder()
                        .setField(FieldCondition.newBuilder()
                                .setKey("ig_account_id")
                                .setMatch(Match.newBuilder().setInteger(igAccountId).build())
                                .build())
                        .build())
                .build();

        // 2. 검색 쿼리 구성
        SearchPoints searchPoints = SearchPoints.newBuilder()
                .setCollectionName(COLLECTION_NAME)
                .addAllVector(searchVector)
                .setFilter(filter)
                .setLimit(10) // 가장 가까운 10개 추출
                .setWithPayload(WithPayloadSelector.newBuilder().setEnable(true).build())
                .build();

        // 3. 검색 실행 및 MySQL post_id 추출
        try {
            List<ScoredPoint> results = qdrantClient.searchAsync(searchPoints).get();

            return results.stream()
                    .map(scoredPoint -> scoredPoint.getPayloadMap().get("post_id").getIntegerValue())
                    .toList();

        } catch (InterruptedException e) {
            // 스레드가 강제로 종료되었을 때
            Thread.currentThread().interrupt();
            throw new RuntimeException("Qdrant 검색 중 스레드가 중단되었습니다.", e);

        } catch (java.util.concurrent.ExecutionException e) {
            // DB 서버가 꺼져있거나 네트워크 통신에 실패했을 때
            throw new RuntimeException("Qdrant 유사도 검색 실행 실패: " + e.getMessage(), e);
        }
    }
}
