package com.exqzore.resourceservice.dao;

import com.exqzore.resourceservice.model.domain.ResourceInfo;
import com.exqzore.resourceservice.repository.ResourceDao;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

@ActiveProfiles("test")
@Tag("integration-test")
@DataJpaTest
public class ResourceDaoTest {
  @Autowired private TestEntityManager entityManager;

  @Value("${amazon.s3.bucket-name}")
  private String bucketName;

  @Autowired private ResourceDao resourceDao;

  private ResourceInfo resourceEntityWithId;
  private ResourceInfo resourceEntityWithoutId;

  @BeforeEach
  void setUp() {
    resourceEntityWithId =
        new ResourceInfo().setId(1L).setBucketName(bucketName).setFileName("test1.mp3");
    resourceEntityWithoutId = new ResourceInfo().setBucketName(bucketName).setFileName("test1.mp3");
  }

  @Test
  void findById() {
    // given
    entityManager.persist(resourceEntityWithoutId);
    entityManager.flush();

    // when
    final Optional<ResourceInfo> expected = Optional.of(resourceEntityWithoutId);
    final Optional<ResourceInfo> actual = resourceDao.findById(resourceEntityWithoutId.getId());

    // then
    Assertions.assertEquals(expected, actual);
  }

  @Test
  void save() {
    // when
    final ResourceInfo actual = resourceDao.save(resourceEntityWithoutId);
    final ResourceInfo expected =
        entityManager.find(ResourceInfo.class, resourceEntityWithoutId.getId());

    // then
    Assertions.assertEquals(expected, actual);
  }

  @Test
  void delete() {
    // given
    entityManager.persist(resourceEntityWithoutId);
    entityManager.flush();

    // when
    Executable executable = () -> resourceDao.delete(resourceEntityWithoutId);

    // then
    Assertions.assertDoesNotThrow(executable);
  }
}
