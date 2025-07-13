package org.example.exceptions

class BookingNotFoundException(message: String) : RuntimeException(message)
class BookingConflictException(message: String) : RuntimeException(message)
class InvalidBookingTimeException(message: String) : RuntimeException(message)
class ProfessionalNotFoundException(message: String) : RuntimeException(message) 