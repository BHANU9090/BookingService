package org.example.model

import com.fasterxml.jackson.annotation.JsonFormat
import java.time.LocalDateTime
import jakarta.validation.constraints.Future
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive

data class Booking(
    val id: String = "",
    @field:NotNull
    @field:Positive
    val clientId: Long,
    @field:NotNull
    @field:Positive
    val professionalId: Long,
    @field:NotNull
    @field:Future
    @field:JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    val startTime: LocalDateTime,
    @field:NotNull
    @field:Future
    @field:JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    val endTime: LocalDateTime
) {
    init {
        require(startTime.isBefore(endTime)) { "Start time must be before end time" }
        require(startTime.isAfter(LocalDateTime.now())) { "Start time must be in the future" }
    }
}
