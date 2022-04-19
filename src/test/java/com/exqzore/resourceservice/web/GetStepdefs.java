package com.exqzore.resourceservice.web;

import com.amazonaws.services.s3.AmazonS3;
import com.exqzore.resourceservice.model.domain.ResourceInfo;
import com.exqzore.resourceservice.repository.ResourceDao;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.cucumber.junit.Cucumber;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RunWith(Cucumber.class)
@SpringBootTest(classes = CucumberConfig.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Tag("component-test")
public class GetStepdefs {
    @Autowired
    private ResourceDao resourceDao;

    @Autowired
    private AmazonS3 amazonS3;

    @Autowired
    private TestRestTemplate template;

    @Value("${amazon.s3.bucket-name}")
    private String bucketName;

    @LocalServerPort
    private int port;

    private final Map<ResourceInfo, byte[]> savedEntities = new HashMap<>(2);
    private byte[] actual;

    @Given("^The following resources to get$")
    public void theFollowingResources(List<ResourceInfo> resources) {
        resourceDao.saveAllAndFlush(resources).forEach((entity) -> {
            byte[] tmpByteArray = new byte[]{1, 2, 3};
            savedEntities.put(entity, tmpByteArray);
            amazonS3.putObject(bucketName, entity.getFileName(), new String(tmpByteArray));
        });
        Assertions.assertEquals(2, savedEntities.size());
    }

    @When("Users request data from Resource Service")
    public void usersRequestDataFromResourceService() {
        ResourceInfo entity = (ResourceInfo) savedEntities.keySet().toArray()[0];
        actual = template.getForObject("http://localhost:" + port + "/resources/" + entity.getId(), byte[].class);
    }

    @Then("Resource Service should return requested data")
    public void resourceServiceShouldReturnRequestedData() throws IOException {
        final byte[] expected = savedEntities.get(savedEntities.keySet().toArray()[0]);
        Assertions.assertArrayEquals(expected, actual);
    }
}
