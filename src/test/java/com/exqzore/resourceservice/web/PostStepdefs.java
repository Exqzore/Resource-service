package com.exqzore.resourceservice.web;

import com.amazonaws.services.s3.AmazonS3;
import com.exqzore.resourceservice.model.EntityBase;
import com.exqzore.resourceservice.model.domain.ResourceInfo;
import com.exqzore.resourceservice.repository.ResourceDao;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Tag("component-test")
public class PostStepdefs {
  @Autowired private ResourceDao resourceDao;

  @Autowired private AmazonS3 amazonS3;

  @Autowired private TestRestTemplate template;

  @Value("${amazon.s3.bucket-name}")
  private String bucketName;

  @LocalServerPort private int port;

  private MultipartFile fileToUpload;
  private EntityBase actual;

  @Given("^File (.*) with (.*)$")
  public void fileWith(String fileName, String content) {
    fileToUpload = new MockMultipartFile(fileName, content.getBytes(StandardCharsets.UTF_8));
  }

  @When("Users upload file to Resource Service")
  public void usersUploadFileToResourceService() throws IOException {
    MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
    body.add(
        "file",
        new MultipartInputStreamFileResource(
            fileToUpload.getInputStream(), fileToUpload.getName()));
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.MULTIPART_FORM_DATA);
    HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(body, headers);
    ResponseEntity<EntityBase> idsResponseEntity =
        template.postForEntity(
            "http://localhost:" + port + "/resources", request, EntityBase.class);
    Assertions.assertTrue(idsResponseEntity.getStatusCode().is2xxSuccessful());
    actual = idsResponseEntity.getBody();
  }

  @Then("Resource Service should handle it and return success status")
  public void resourceServiceShouldHandleItAndReturnSuccessStatus() {
    List<ResourceInfo> all = resourceDao.findAll();
    Assertions.assertEquals(1, all.size());
    ResourceInfo entity = all.get(0);
    Assertions.assertNotNull(actual);
    Assertions.assertEquals(entity.getId(), actual.getId());
    Assertions.assertNotNull(amazonS3.getObject(bucketName, entity.getFileName()));
  }

  static class MultipartInputStreamFileResource extends InputStreamResource {

    private final String filename;

    MultipartInputStreamFileResource(InputStream inputStream, String filename) {
      super(inputStream);
      this.filename = filename;
    }

    @Override
    public String getFilename() {
      return this.filename;
    }

    @Override
    public long contentLength() throws IOException {
      return -1;
    }
  }
}
