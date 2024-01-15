package com.example.demo.util

import kotlin.random.Random


class RandomUtil {
    fun generateRandomString(length: Int): String {
        val characters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        val random = Random

        return (1..length)
            .map { characters.random(random) }
            .joinToString("")
    }

    fun generateRandomNumber(length: Int): String {
        val characters = "0123456789"
        val random = Random

        return (1..length)
            .map { characters.random(random) }
            .joinToString("")
    }
}