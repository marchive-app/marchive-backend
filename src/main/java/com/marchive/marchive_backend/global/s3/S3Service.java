package com.marchive.marchive_backend.global.s3;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

@Service
public class S3Service {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final RestTemplate restTemplate;

    @Value("${aws.s3.bucket}")
    private String bucket;

    private static final Map<String, String> KNOWN_EXTENSIONS = Map.of(
            "image/jpeg", "jpg",
            "image/png", "png",
            "image/webp", "webp",
            "video/mp4", "mp4",
            "video/quicktime", "mov"
    );

    public S3Service(S3Client s3Client, S3Presigner s3Presigner, RestTemplate restTemplate) {
        this.s3Client = s3Client;
        this.s3Presigner = s3Presigner;
        this.restTemplate = restTemplate;
    }

    // 인스타 CDN URL의 이미지를 다운로드해서 S3에 업로드하고, key를 반환
    public String uploadFromUrl(String sourceUrl) {
        ResponseEntity<byte[]> response = restTemplate.exchange(sourceUrl, HttpMethod.GET, null, byte[].class);

        byte[] fileBytes = response.getBody();
        if (fileBytes == null || fileBytes.length == 0) {
            throw new IllegalStateException("미디어 다운로드에 실패했습니다: " + sourceUrl);
        }

        String contentType = resolveContentType(response);
        String extension = resolveExtension(contentType);
        String key = "posts/" + UUID.randomUUID() + "." + extension;

        s3Client.putObject(
                PutObjectRequest.builder()
                        .bucket(bucket)
                        .key(key)
                        .contentType(contentType)
                        .build(),
                RequestBody.fromBytes(fileBytes)
        );

        return key;
    }

    private String resolveContentType(ResponseEntity<byte[]> response) {
        MediaType mediaType = response.getHeaders().getContentType();
        return mediaType != null ? mediaType.toString() : "application/octet-stream";
    }

    // 알려진 형식이면 정확한 확장자, 아니면 대분류(image/video)로 기본값 처리
    private String resolveExtension(String contentType) {
        if (KNOWN_EXTENSIONS.containsKey(contentType)) {
            return KNOWN_EXTENSIONS.get(contentType);
        }
        if (contentType.startsWith("video")) {
            return "mp4";
        }
        return "jpg";
    }

    // key로부터 짧게 유효한 조회용 presigned URL 생성
    public String generatePresignedUrl(String key) {
        if (key == null) {
            return null;
        }

        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(15))   // 15분간만 유효
                .getObjectRequest(getObjectRequest)
                .build();

        return s3Presigner.presignGetObject(presignRequest).url().toString();
    }
}
