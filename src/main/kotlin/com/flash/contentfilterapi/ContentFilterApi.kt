package com.flash.contentfilterapi

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cache.annotation.EnableCaching

@SpringBootApplication
@EnableCaching
class ContentFilterApi

fun main(args: Array<String>) {
    runApplication<ContentFilterApi>(*args)
}