package com.marchive.marchive_backend.global.s3;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.net.URL;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

@ExtendWith(MockitoExtension.class)
class S3ServiceTest {

    @Mock
    private S3Client s3Client;
    @Mock
    private S3Presigner s3Presigner;
    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private S3Service s3Service;

    @Test
    void 이미지_다운로드_후_S3에_업로드하고_key를_반환한다() {
        ReflectionTestUtils.setField(s3Service, "bucket", "test-bucket");

        byte[] fakeImageBytes = "fake-image-data".getBytes();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_JPEG);
        ResponseEntity<byte[]> fakeResponse =
                new ResponseEntity<>(fakeImageBytes, headers, HttpStatus.OK);

        when(restTemplate.exchange(eq("https://cdn.example.com/img.jpg"), eq(HttpMethod.GET), eq(null),
                eq(byte[].class)))
                .thenReturn(fakeResponse);
        when(s3Client.putObject(any(PutObjectRequest.class), any(software.amazon.awssdk.core.sync.RequestBody.class)))
                .thenReturn(PutObjectResponse.builder().build());

        String key = s3Service.uploadFromUrl("https://cdn.example.com/img.jpg");

        assertThat(key).startsWith("posts/");
        assertThat(key).endsWith(".jpg");   // Content-Type이 jpeg였으니 확장자도 jpg
        verify(s3Client).putObject(any(PutObjectRequest.class),
                any(software.amazon.awssdk.core.sync.RequestBody.class));
    }

    @Test
    void png_이미지면_png_확장자로_저장된다() {
        ReflectionTestUtils.setField(s3Service, "bucket", "test-bucket");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_PNG);
        ResponseEntity<byte[]> fakeResponse =
                new ResponseEntity<>("data".getBytes(), headers, HttpStatus.OK);

        when(restTemplate.exchange(any(String.class), eq(HttpMethod.GET), eq(null), eq(byte[].class)))
                .thenReturn(fakeResponse);
        when(s3Client.putObject(any(PutObjectRequest.class), any(software.amazon.awssdk.core.sync.RequestBody.class)))
                .thenReturn(PutObjectResponse.builder().build());

        String key = s3Service.uploadFromUrl("https://cdn.example.com/img.png");

        assertThat(key).endsWith(".png");
    }

    @Test
    void contentType이_없으면_jpg로_저장된다() {
        ReflectionTestUtils.setField(s3Service, "bucket", "test-bucket");

        HttpHeaders headers = new HttpHeaders(); // Content-Type 없음
        ResponseEntity<byte[]> fakeResponse =
                new ResponseEntity<>("data".getBytes(), headers, HttpStatus.OK);

        when(restTemplate.exchange(any(String.class), eq(HttpMethod.GET), eq(null), eq(byte[].class)))
                .thenReturn(fakeResponse);
        when(s3Client.putObject(any(PutObjectRequest.class), any(software.amazon.awssdk.core.sync.RequestBody.class)))
                .thenReturn(PutObjectResponse.builder().build());

        String key = s3Service.uploadFromUrl("https://cdn.example.com/unknown");

        assertThat(key).endsWith(".jpg");
    }

    @Test
    void 다운로드한_데이터가_비어있으면_예외가_발생한다() {
        ReflectionTestUtils.setField(s3Service, "bucket", "test-bucket");

        ResponseEntity<byte[]> emptyResponse =
                new ResponseEntity<>(new byte[0], new HttpHeaders(), HttpStatus.OK);

        when(restTemplate.exchange(any(String.class), eq(HttpMethod.GET), eq(null), eq(byte[].class)))
                .thenReturn(emptyResponse);

        assertThatThrownBy(() -> s3Service.uploadFromUrl("https://cdn.example.com/broken"))
                .isInstanceOf(IllegalStateException.class);

        verify(s3Client, never()).putObject(any(PutObjectRequest.class),
                any(software.amazon.awssdk.core.sync.RequestBody.class));
    }

    @Test
    void key가_null이면_presigned_URL도_null을_반환한다() {
        String result = s3Service.generatePresignedUrl(null);
        assertThat(result).isNull();
        verifyNoInteractions(s3Presigner);
    }

    @Test
    void key가_있으면_presigned_URL을_생성한다() throws Exception {
        ReflectionTestUtils.setField(s3Service, "bucket", "test-bucket");

        PresignedGetObjectRequest presignedRequest = mock(PresignedGetObjectRequest.class);
        when(presignedRequest.url()).thenReturn(
                new URL("https://test-bucket.s3.amazonaws.com/posts/abc.jpg?X-Amz-Signature=..."));
        when(s3Presigner.presignGetObject(
                any(software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest.class)))
                .thenReturn(presignedRequest);

        String url = s3Service.generatePresignedUrl("posts/abc.jpg");

        assertThat(url).contains("X-Amz-Signature");
    }
}