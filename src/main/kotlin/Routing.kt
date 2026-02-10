package com.example

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sse.*
import io.ktor.sse.*
import kotlinx.coroutines.delay
import kotlin.io.path.Path

fun Application.configureRouting() {
    install(SSE)
    routing {
        get("/") {
            call.respondText("Hello World!")
        }
        sse("/message") {
            val message = "Hello im Karthik, and Im happy to share that I " +
                    "have did this project from scratch!"

            val words = message.split(" ")

            for(word in words){
                send(
                    ServerSentEvent(data = "$word ")
                )
                delay(100)
            }

            send(ServerSentEvent("[DONE]"))
        }
    }
}
