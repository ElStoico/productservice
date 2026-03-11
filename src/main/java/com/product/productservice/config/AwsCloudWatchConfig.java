package com.product.productservice.config;

import java.net.URI;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;

@Configuration
public class AwsCloudWatchConfig {

    @Bean
    CloudWatchLogsClient cloudWatchLogsClient(
            @Value("${observability.cloudwatch.logs.endpoint}") String endpoint,
            @Value("${observability.cloudwatch.logs.region}") String region,
            @Value("${observability.cloudwatch.logs.access-key}") String accessKey,
            @Value("${observability.cloudwatch.logs.secret-key}") String secretKey) {
        return CloudWatchLogsClient.builder()
                .endpointOverride(URI.create(endpoint))
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey)))
                .build();
    }
}