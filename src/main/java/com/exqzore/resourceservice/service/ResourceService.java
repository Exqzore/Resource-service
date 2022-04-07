package com.exqzore.resourceservice.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.util.IOUtils;
import com.exqzore.resourceservice.exception.InternalServerException;
import com.exqzore.resourceservice.exception.InvalidBodyOrValidationException;
import com.exqzore.resourceservice.exception.ResourceNotFoundException;
import com.exqzore.resourceservice.model.DeletedEntitiesInfo;
import com.exqzore.resourceservice.model.EntityBase;
import com.exqzore.resourceservice.model.domain.ResourceInfo;
import com.exqzore.resourceservice.repository.ResourceDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.multipart.MultipartFile;

import javax.transaction.Transactional;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ResourceService {

  private static final String INVALID_BODY_VALIDATION_ERROR =
      "Validation error or request body is an invalid MP3";
  private static final String INTERNAL_SERVER_ERROR = "Internal server error occurred";
  private static final String NOT_FOUND_ERROR = "Resource doesn't exist with given id";
  private final AmazonS3 s3Client;
  private final ResourceDao resourceDao;

  @Value("${cloud.aws.bucket.name}")
  private String bucketName;

  private final KafkaTemplate<String, byte[]> kafkaTemplate;

  @Autowired
  public ResourceService(
      AmazonS3 s3Client, ResourceDao resourceDao, KafkaTemplate<String, byte[]> kafkaTemplate) {
    this.s3Client = s3Client;
    this.resourceDao = resourceDao;
    this.kafkaTemplate = kafkaTemplate;
  }

  public EntityBase uploadFile(MultipartFile file) {
    if (file.getOriginalFilename() == null) {
      throw new InvalidBodyOrValidationException(INVALID_BODY_VALIDATION_ERROR);
    }

    String[] parts = file.getOriginalFilename().split("\\.");
    if (parts.length != 2) {
      throw new InvalidBodyOrValidationException(INVALID_BODY_VALIDATION_ERROR);
    }
    if (!"MP3".equals(parts[1].toUpperCase(Locale.ROOT))) {
      throw new InvalidBodyOrValidationException(INVALID_BODY_VALIDATION_ERROR);
    }

    ResourceInfo resourceInfo = new ResourceInfo();
    resourceInfo.setBucketName(bucketName);
    resourceDao.save(resourceInfo);

    if (resourceInfo.getId() != null) {
      String fileName =
          System.currentTimeMillis()
              + "_"
              + resourceInfo.getId()
              + "_"
              + file.getOriginalFilename();

      resourceInfo.setFileName(fileName);

      resourceDao.save(resourceInfo);

      File fileObj = convertMultiPartFileToFile(file);
      s3Client.putObject(new PutObjectRequest(bucketName, fileName, fileObj));
      fileObj.delete();

      kafkaTemplate.send("topic", Long.toString(resourceInfo.getId()).getBytes());
    } else {
      throw new InternalServerException(INTERNAL_SERVER_ERROR);
    }

    return new EntityBase(resourceInfo.getId());
  }

  public byte[] downloadFile(Long id) {
    ResourceInfo resourceInfo;
    Optional<ResourceInfo> resourceInfoOptional = resourceDao.findById(id);
    if (resourceInfoOptional.isPresent()) {
      resourceInfo = resourceInfoOptional.get();
    } else {
      throw new ResourceNotFoundException(NOT_FOUND_ERROR);
    }

    S3Object s3Object =
        s3Client.getObject(resourceInfo.getBucketName(), resourceInfo.getFileName());
    S3ObjectInputStream inputStream = s3Object.getObjectContent();
    try {
      return IOUtils.toByteArray(inputStream);
    } catch (IOException e) {
      throw new InternalServerException(INTERNAL_SERVER_ERROR);
    }
  }

  private File convertMultiPartFileToFile(MultipartFile file) {
    File convertedFile = new File(Objects.requireNonNull(file.getOriginalFilename()));
    try (FileOutputStream fos = new FileOutputStream(convertedFile)) {
      fos.write(file.getBytes());
    } catch (IOException exception) {
      throw new InternalServerException(INTERNAL_SERVER_ERROR);
    }
    return convertedFile;
  }

  public String getFileNameById(Long id) {
    Optional<ResourceInfo> resourceInfo = resourceDao.findById(id);
    if (resourceInfo.isPresent()) {
      return resourceInfo.get().getFileName();
    }
    throw new ResourceNotFoundException(NOT_FOUND_ERROR);
  }

  @Transactional
  public DeletedEntitiesInfo deleteFiles(String idsString) {
    if (idsString == null || idsString.length() > 200) {
      throw new InvalidBodyOrValidationException(INVALID_BODY_VALIDATION_ERROR);
    }
    try {
      List<Long> idValues = getIdValuesFromStrings(idsString.split(","));
      List<ResourceInfo> deletedResources = resourceDao.deleteAllByIdIn(idValues);
      deletedResources.forEach(
          resourceInfo ->
              s3Client.deleteObject(resourceInfo.getBucketName(), resourceInfo.getFileName()));

      return new DeletedEntitiesInfo(
          deletedResources.stream().map(ResourceInfo::getId).collect(Collectors.toList()));

    } catch (Exception exception) {
      throw new InternalServerException(INTERNAL_SERVER_ERROR);
    }
  }

  private List<Long> getIdValuesFromStrings(String[] ids) {
    return Arrays.stream(ids).map(Long::parseLong).collect(Collectors.toList());
  }
}
