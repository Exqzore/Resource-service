package com.exqzore.resourceservice.web;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.PutObjectRequest;

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

import java.io.File;
import java.util.Arrays;
import java.util.List;

@Tag("component-test")
public class DeleteStepdefs {
  @Autowired private ResourceDao resourceDao;

  @Autowired private AmazonS3 amazonS3;

  @Autowired private TestRestTemplate template;

  @Value("${amazon.s3.bucket-name}")
  private String bucketName;

  @LocalServerPort private int port;

  private List<ResourceInfo> savedInstances;

  @Given("^The following resources to delete$")
  public void theFollowingResources(List<ResourceInfo> resources) {
    savedInstances = resourceDao.saveAllAndFlush(resources);
    savedInstances.forEach(
        entity ->
            amazonS3.putObject(
                    bucketName, entity.getFileName(), new String(new byte[]{})));
    Assertions.assertEquals(2, savedInstances.size());
  }

  @When("Users wants to delete specified data in Resource Service")
  public void usersWantsToDeleteSpecifiedDataInResourceService() {
    long realId = savedInstances.get(0).getId();
    long fakeId =
        findPositiveNumberNotIn(savedInstances.get(0).getId(), savedInstances.get(1).getId());
    template.delete("http://localhost:" + port + "/resources?id=" + realId + "," + fakeId);
  }

  @Then("Resource Service should delete it")
  public void resourceServiceShouldDeleteIt() {
    List<ResourceInfo> all = resourceDao.findAll();
    Assertions.assertThrows(
        AmazonServiceException.class,
        () -> amazonS3.getObject(bucketName, savedInstances.get(0).getFileName()));
    Assertions.assertNotNull(amazonS3.getObject(bucketName, savedInstances.get(1).getFileName()));
    Assertions.assertEquals(1, all.size());
    Assertions.assertEquals(savedInstances.get(1), all.get(0));
  }

  private long findPositiveNumberNotIn(long... numbers) {
    for (long l = 1; l < Long.MAX_VALUE; l++) {
      long finalL = l;
      if (Arrays.stream(numbers).noneMatch(number -> finalL == number)) {
        return finalL;
      }
    }
    throw new RuntimeException("No positive number found");
  }
}
