package de.libf.transportrng

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform