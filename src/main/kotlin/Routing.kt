package com.example

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sse.*
import io.ktor.sse.*
import io.ktor.utils.io.*
import kotlinx.coroutines.delay

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

            for (word in words) {
                send(
                    ServerSentEvent(data = "$word ")
                )
                delay(100)
            }

            send(ServerSentEvent("[DONE]"))
        }

        sse("/chat") {
            try {
                val client = HttpClient(CIO)

                val prompt = call.request.queryParameters["prompt"]
                if (prompt.isNullOrBlank()) {
                    send(ServerSentEvent(data = "Prompt is required"))
                    return@sse
                }

                val response = client.preparePost("http://localhost:11434/api/generate") {
                    contentType(ContentType.Application.Json)
                    setBody(
                        """
                {
                  "model": "llama3",
                  "prompt": "$prompt",
                  "stream": true
                }
                """
                    )
                }.execute()

                val channel = response.bodyAsChannel()

                while (!channel.isClosedForRead) {
                    val line = channel.readUTF8Line() ?: continue
                    if (line.isBlank()) continue

                    send(ServerSentEvent(data = line))
                }

            } catch (e: Exception) {
                send(ServerSentEvent(data = "Error: ${e.message}"))
            }
        }
    }
}
