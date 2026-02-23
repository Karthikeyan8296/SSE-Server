package com.example
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Listing(
    val id: String,
    val title: String
)

@Serializable
data class PollResponse(
    val message: String,
    val listing: List<Listing>? = null,
    val status: String
)

object PollState {
    private var callCount = 0

    fun increment(): Int {
        callCount++
        return callCount
    }

    fun reset(){
        callCount = 0
    }
}