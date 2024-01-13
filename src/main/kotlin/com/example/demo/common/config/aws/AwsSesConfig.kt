package com.example.demo.common.config.aws

import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClientBuilder
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration


@Configuration
class AwsSesConfig {
    @Value("\${aws.ses.access_key}")
    lateinit var accessKey: String

    @Value("\${aws.ses.secret_key}")
    lateinit var secretKey: String

    @Bean
    fun amazonSimpleEmailService(): AmazonSimpleEmailService {
        val basicAWSCredentials = BasicAWSCredentials(accessKey, secretKey)
        val awsStaticCredentialsProvider = AWSStaticCredentialsProvider(
            basicAWSCredentials
        )
        return AmazonSimpleEmailServiceClientBuilder.standard()
            .withCredentials(awsStaticCredentialsProvider)
            .withRegion("ap-northeast-2")
            .build()
    }
}
