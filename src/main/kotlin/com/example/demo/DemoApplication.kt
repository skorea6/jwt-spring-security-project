package com.example.demo

import jakarta.annotation.PostConstruct
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cache.annotation.EnableCaching
import java.util.*

@EnableCaching
@SpringBootApplication
class DemoApplication{
	@PostConstruct
	fun started() {
		TimeZone.setDefault(TimeZone.getTimeZone("Asia/Seoul"))
	}
}

fun main(args: Array<String>) {
	runApplication<DemoApplication>(*args)
}
