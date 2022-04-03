package com.exqzore.resourceservice.config;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ResourceConfig {

  @Value("${cloud.aws.credentials.access-key}")
  private String accessKey;

  @Value("${cloud.aws.credentials.secret-key}")
  private String secretKey;

  @Value("${cloud.aws.bucket.name}")
  private String bucketName;

  //  @Value("${cloud.aws.endpoint}")
  //  private String endpoint;
  //
  //  @Value("${cloud.aws.region}")
  //  private String region;

  @Bean
  public AmazonS3 amazonS3() {
    AWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);
    AmazonS3 amazonS3 =
        AmazonS3ClientBuilder.standard()
            .withCredentials(new AWSStaticCredentialsProvider(credentials))
            .withRegion(Regions.EU_CENTRAL_1)
            .build();
    if (!amazonS3.doesBucketExistV2(bucketName)) {
      amazonS3.createBucket(bucketName);
    }
    return amazonS3;
  }

  //  @Bean
  //  public AmazonS3 amazonS3() {
  //    AwsClientBuilder.EndpointConfiguration endpointConfiguration =
  //        new AwsClientBuilder.EndpointConfiguration(endpoint, region);
  //    AmazonS3 amazonS3 =
  //        AmazonS3ClientBuilder.standard()
  //            .withEndpointConfiguration(endpointConfiguration)
  //            .withPathStyleAccessEnabled(true)
  //            .build();
  //    if (!amazonS3.doesBucketExistV2(bucketName)) {
  //      amazonS3.createBucket(bucketName);
  //    }
  //    return amazonS3;
  //  }
}
