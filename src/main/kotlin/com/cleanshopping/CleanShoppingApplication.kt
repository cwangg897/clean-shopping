package com.cleanshopping

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class CleanShoppingApplication

fun main(args: Array<String>) {
    runApplication<CleanShoppingApplication>(*args)
}
