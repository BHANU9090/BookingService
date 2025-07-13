package org.example.service

import org.example.exceptions.BookingConflictException
import org.example.exceptions.BookingNotFoundException
import org.example.exceptions.InvalidBookingTimeException
import org.example.repository.FirestoreBookingRepository
import org.example.model.Booking
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

@Service
open class BookingService(private val bookingRepository: FirestoreBookingRepository) {

    private val lock = ReentrantLock()

    fun createBooking(booking: Booking): Booking {
        lock.withLock {
            validateBookingTime(booking)
            
            val overlapping = bookingRepository.findByProfessionalIdAndTimeRange(
                booking.professionalId,
                booking.startTime,
                booking.endTime
            )
            if (overlapping.isNotEmpty()) {
                throw BookingConflictException("Booking overlaps with existing booking")
            }
            
            val created = bookingRepository.save(booking)
            evictAvailabilityCache(booking.professionalId, booking.startTime.toLocalDate())
            return created
        }
    }

    fun listBookings(
        clientId: Long?,
        professionalId: Long?,
        date: LocalDate?,
        page: Int?=null,
        size: Int?=null
    ): List<Booking> {
        if (page != null && page < 0) {
            throw IllegalArgumentException("Page number must be non-negative")
        }
        if (size != null && size < 1) {
            throw IllegalArgumentException("Page size must be positive")
        }

        var bookings = bookingRepository.findAll()

        clientId?.let {
            bookings = bookings.filter { it.clientId == clientId }
        }
        professionalId?.let {
            bookings = bookings.filter { it.professionalId == professionalId }
        }
        date?.let {
            val startOfDay = it.atStartOfDay()
            val endOfDay = it.atTime(LocalTime.MAX)
            bookings = bookings.filter { it.startTime.isAfter(startOfDay) && it.endTime.isBefore(endOfDay) }
        }

        if (page != null && size != null) {
            val fromIndex = page * size
            val toIndex = kotlin.math.min(fromIndex + size, bookings.size)
            if (fromIndex >= bookings.size) {
                return emptyList()
            }
            bookings = bookings.subList(fromIndex, toIndex)
        }

        return bookings
    }

    fun deleteBooking(id: String): Boolean {
        return if (bookingRepository.existsById(id)) {
            val booking = bookingRepository.findById(id)
            bookingRepository.deleteById(id)
            // Evict availability cache for this professional and date
            booking?.let { evictAvailabilityCache(it.professionalId, it.startTime.toLocalDate()) }
            true
        } else {
            throw BookingNotFoundException("Booking with id $id not found")
        }
    }

    @Cacheable(value = ["availability"], key = "#professionalId + '_' + #date")
    fun getAvailability(professionalId: Long, date: LocalDate): List<Pair<LocalDateTime, LocalDateTime>> {
        val startOfDay = date.atStartOfDay()
        val endOfDay = date.atTime(LocalTime.MAX)

        val bookings = bookingRepository.findByProfessionalId(professionalId)
            .filter { it.startTime.toLocalDate() == date }

        val freeSlots = mutableListOf<Pair<LocalDateTime, LocalDateTime>>()

        var currentStart = startOfDay

        for (booking in bookings.sortedBy { it.startTime }) {
            if (currentStart.isBefore(booking.startTime)) {
                freeSlots.add(Pair(currentStart, booking.startTime))
            }
            currentStart = if (currentStart.isAfter(booking.endTime)) currentStart else booking.endTime
        }

        if (currentStart.isBefore(endOfDay)) {
            freeSlots.add(Pair(currentStart, endOfDay))
        }

        return freeSlots
    }

    private fun validateBookingTime(booking: Booking) {
        if (booking.startTime.isAfter(booking.endTime)) {
            throw InvalidBookingTimeException("Start time must be before end time")
        }
        if (booking.startTime.isBefore(LocalDateTime.now())) {
            throw InvalidBookingTimeException("Start time must be in the future")
        }
        
        // Check if booking is within business hours (9 AM to 6 PM)
        val startHour = booking.startTime.hour
        val endHour = booking.endTime.hour
        if (startHour < 9 || endHour > 18) {
            throw InvalidBookingTimeException("Bookings must be within business hours (9 AM - 6 PM)")
        }
    }

    @CacheEvict(value = ["availability"], key = "#professionalId + '_' + #date")
    private fun evictAvailabilityCache(professionalId: Long, date: LocalDate) {
        // This method is used to evict cache when bookings are created or deleted
    }
}
