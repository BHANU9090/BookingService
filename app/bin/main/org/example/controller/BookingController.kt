package org.example.controller

import org.example.model.Booking
import org.example.service.BookingService
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import java.time.LocalDate
import java.time.LocalDateTime
import jakarta.validation.Valid
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull
import org.slf4j.LoggerFactory


@RestController
@RequestMapping("/bookings")
@Validated
open class BookingController(private val bookingService: BookingService) {
    companion object {
        private val logger = LoggerFactory.getLogger(BookingController::class.java)
    }

    @PostMapping
    fun createBooking(@Valid @RequestBody booking: Booking): ResponseEntity<Booking> {
        logger.info("Received booking request: startTime = ${booking.startTime}, server now = ${java.time.LocalDateTime.now()}")
        val created = bookingService.createBooking(booking)
        return ResponseEntity.status(HttpStatus.CREATED).body(created)
    }
    @GetMapping
    fun listBookings(
        @RequestParam(required = false) clientId: Long?,
        @RequestParam(required = false) professionalId: Long?,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) date: LocalDate?,
        @RequestParam(defaultValue = "0") @Min(0) page: Int,
        @RequestParam(defaultValue = "10") @Min(1) size: Int
    ): ResponseEntity<Map<String, Any>> {
        val filtered = bookingService.listBookings(clientId, professionalId, date)

        val fromIndex = page * size
        val toIndex = minOf(fromIndex + size, filtered.size)

        val paginated = if (fromIndex >= filtered.size) emptyList() else filtered.subList(fromIndex, toIndex)

        val response = mapOf(
            "page" to page,
            "size" to size,
            "totalElements" to filtered.size,
            "totalPages" to (filtered.size + size - 1) / size,
            "bookings" to paginated
        )

        return ResponseEntity.ok(response)
    }


    @DeleteMapping("/{id}")
    fun deleteBooking(@PathVariable @NotNull id: String): ResponseEntity<Unit> {
        bookingService.deleteBooking(id)
        return ResponseEntity.noContent().build()
    }
    @GetMapping("/availability")
    fun getAvailability(
        @RequestParam @NotNull professionalId: Long,
        @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) date: LocalDate
    ): ResponseEntity<List<Map<String, LocalDateTime>>> {
        val slots = bookingService.getAvailability(professionalId, date)
        val response = slots.map { mapOf("startTime" to it.first, "endTime" to it.second) }
        return ResponseEntity.ok(response)
    }
}
