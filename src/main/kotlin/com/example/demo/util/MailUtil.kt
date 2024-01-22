package com.example.demo.util

import com.amazonaws.services.simpleemail.model.*
import com.example.demo.common.config.aws.AwsSesConfig
import org.springframework.stereotype.Component


data class SenderDto(
    val from: String = "ABZ 관리자 <no-reply@abz.kr>",
    val to: ArrayList<String>,
    val subject: String,
    val content: String
) {
    fun addTo(email: String) {
        to.add(email)
    }

    fun toSendRequestDto(): SendEmailRequest {
        val destination: Destination = Destination()
            .withToAddresses(to)
        val message: Message = Message()
            .withSubject(createContent(subject))
            .withBody(
                Body()
                    .withHtml(createContent(content))
            )
        return SendEmailRequest()
            .withSource(from)
            .withDestination(destination)
            .withMessage(message)
    }

    private fun createContent(text: String): Content {
        return Content()
            .withCharset("UTF-8")
            .withData(text)
    }
}


@Component
class MailUtil(
    private val awsSesConfig: AwsSesConfig
) {
    fun send(senderDto: SenderDto) {
        try {
            val client = awsSesConfig.amazonSimpleEmailService()
            client.sendEmail(senderDto.toSendRequestDto())
        } catch (ex: Exception) {
//            throw IllegalArgumentException(ex.message)
            throw IllegalArgumentException("이메일 전송 서비스가 원활하지 않습니다.")
        }
    }
}
