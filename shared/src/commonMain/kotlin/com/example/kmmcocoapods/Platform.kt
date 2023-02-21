package com.example.kmmcocoapods

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform