package com.example

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sse.*
import io.ktor.sse.*
import io.ktor.utils.io.*
import kotlinx.coroutines.delay
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.util.*


fun Application.configureRouting() {
    install(SSE)
    install(ContentNegotiation) {
        json()
    }

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

                    val json = Json.parseToJsonElement(line).jsonObject

                    val done = json["done"]?.jsonPrimitive?.boolean ?: false
                    if (done) break

                    val token = json["response"]?.jsonPrimitive?.content

                    if (!token.isNullOrBlank()) {
                        send(ServerSentEvent(data = token))
                    }
                }
            } catch (e: Exception) {
                send(ServerSentEvent(data = "Error: ${e.message}"))
            }
        }

        get("/poll") {
            val currentCount = PollState.increment()

            val response = if (currentCount < 5) {
                PollResponse(
                    message = "This is a reply",
                    listing = generateListings(currentCount),
                    status = "PENDING"
                )
            } else {
                val completedResponse = PollResponse(
                    message = "Reply Completed",
                    listing = generateListings(currentCount),
                    status = "COMPLETED"
                )
                PollState.reset()
                completedResponse
            }
            call.respond(response)
        }
    }
}


fun generateListings(count: Int): List<Listing> {
    val titles = listOf(
        "Premium Grocery Pack",
        "Fresh Farm Vegetables",
        "Daily Essentials Combo",
        "Mega Discount Sale",
        "Weekend Special Offer",
        "Organic Fruits Basket",
        "Limited Time Deal",
        "Trending Now",
        "Hot Picks for You",
        "Exclusive Member Offer"
    )

    return List(count) {
        Listing(
            id = UUID.randomUUID().toString(),
            title = titles.random()
        )
    }
}
