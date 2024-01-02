package io.casswolfe.cloud;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import io.casswolfe.Configuration;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class S3Service {
    private final AmazonS3 s3;
    private final String s3Bucket;
    private final String s3Key;

    public S3Service() {
        this.s3Bucket = Configuration.getenv("app.s3-bucket");
        this.s3Key = Configuration.getenv("app.s3-key");
        this.s3 = "local".equalsIgnoreCase(Configuration.getenv("profile")) ?
                AmazonS3ClientBuilder.standard()
                        .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration("http://localhost:4566", "us-east-1"))
                        .enablePathStyleAccess()
                        .withCredentials(new DefaultAWSCredentialsProviderChain()).build() :
                AmazonS3ClientBuilder.standard()
                        .withRegion(Regions.US_EAST_1)
                        .withCredentials(new DefaultAWSCredentialsProviderChain()).build();

    }

    public List<String> getSales() {
        try {
            S3ObjectInputStream objectContent = s3.getObject(new GetObjectRequest(s3Bucket, s3Key)).getObjectContent();
            String content = new String(objectContent.readAllBytes());
            return Arrays.asList(content.split("\n"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
