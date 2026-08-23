package com.marchive.marchive_backend.global.s3;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.net.URL;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

@ExtendWith(MockitoExtension.class)
class S3ServiceTest {

    @Mock
    private S3Presigner s3Presigner;

    @InjectMocks
    private S3Service s3Service;

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