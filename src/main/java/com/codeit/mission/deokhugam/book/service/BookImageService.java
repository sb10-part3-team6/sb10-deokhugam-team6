package com.codeit.mission.deokhugam.book.service;

import com.codeit.mission.deokhugam.book.exception.S3UploadFailureException;
import com.codeit.mission.deokhugam.book.exception.S3UrlParseFailureException;
import java.io.IOException;
import java.net.URI;
import java.time.Duration;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetUrlRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

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
      throw new S3UploadFailureException();
    }
  }

  public void deleteFileByUrl(String fileUrl) {
    if (fileUrl == null || fileUrl.isBlank()) {
      return;
    }
    String key = extractKeyFromUrl(fileUrl);

    s3Client.deleteObject(
        DeleteObjectRequest.builder()
            .bucket(bucket)
            .key(key)
            .build()
    );
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

  private String extractKeyFromUrl(String fileUrl) {
    if (fileUrl == null || fileUrl.isBlank()) {
      return null;
    }
    try {
      URI uri = URI.create(fileUrl);

      String path = uri.getPath(); // "/images/test.jpg"
      if (path == null || path.length() <= 1) {
        throw new S3UrlParseFailureException();
      }

      return path.substring(1); // 앞 "/" 제거

    } catch (S3UrlParseFailureException e) {
      throw e;
    } catch (Exception e) {
      throw new S3UrlParseFailureException();
    }
  }
}
