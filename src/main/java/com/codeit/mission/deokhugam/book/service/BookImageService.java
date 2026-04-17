package com.codeit.mission.deokhugam.book.service;

import com.codeit.mission.deokhugam.book.exception.S3UploadFailureException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetUrlRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.time.Duration;
import java.io.IOException;
import java.util.UUID;

import static com.codeit.mission.deokhugam.error.ErrorCode.S3_UPLOAD_FAILED;

@Service
@RequiredArgsConstructor
public class BookImageService {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    //임시 설정값, 추후 인프라 세팅하면서 변경될 수 있음
    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    public String upload(MultipartFile file) {
        try {
            String fileName = createFileName(file.getOriginalFilename());

            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(fileName)
                    .contentType(file.getContentType())
                    .build();

            s3Client.putObject(
                    request,
                    RequestBody.fromInputStream(
                            file.getInputStream(),
                            file.getSize()
                    )
            );

            return getUrl(fileName);

        } catch (IOException e) {
            throw new S3UploadFailureException(S3_UPLOAD_FAILED);
        }
    }

    public String generatePresignedUrl(String fileName) {

        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucket)
                .key(fileName)
                .build();

        PresignedGetObjectRequest presignedRequest =
                s3Presigner.presignGetObject(builder -> builder
                        .signatureDuration(Duration.ofMinutes(10)) // 유효시간
                        .getObjectRequest(getObjectRequest)
                );

        return presignedRequest.url().toString();
    }

    private String createFileName(String originalName) {
        return UUID.randomUUID() + "_" + originalName;
    }

    private String getUrl(String fileName) {
        return s3Client.utilities()
                .getUrl(GetUrlRequest.builder().bucket(bucket).key(fileName).build())
                .toExternalForm();
    }
}
