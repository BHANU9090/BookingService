package org.example.repository

import com.google.cloud.Timestamp
import com.google.cloud.firestore.DocumentSnapshot
import com.google.cloud.firestore.Firestore
import org.example.model.Booking
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.concurrent.ExecutionException

@Repository
open class FirestoreBookingRepository {

    @Autowired
    private lateinit var firestore: Firestore

    private val collectionName = "bookings"

    fun save(booking: Booking): Booking {
        try {
            val docRef = firestore.collection(collectionName).document()
            val bookingWithId = booking.copy(id = docRef.id)

            val data = mapOf(
                "id" to bookingWithId.id,
                "clientId" to bookingWithId.clientId,
                "professionalId" to bookingWithId.professionalId,
                "startTime" to bookingWithId.startTime.toFirestoreTimestamp(),  // ✅ Proper Firestore timestamp
                "endTime" to bookingWithId.endTime.toFirestoreTimestamp()       // ✅ Proper Firestore timestamp
            )

            docRef.set(data).get()
            return bookingWithId
        } catch (e: ExecutionException) {
            throw RuntimeException("Failed to save booking", e)
        }
    }

    fun findByProfessionalIdAndTimeRange(professionalId: Long, startTime: LocalDateTime, endTime: LocalDateTime): List<Booking> {
        return try {
            val bookings = mutableListOf<Booking>()
            val query = firestore.collection(collectionName)
                .whereEqualTo("professionalId", professionalId)
                .whereLessThan("startTime", endTime.toFirestoreTimestamp())
                .whereGreaterThan("endTime", startTime.toFirestoreTimestamp())

            val querySnapshot = query.get().get()
            for (doc in querySnapshot.documents) {
                bookings.add(documentToBooking(doc))
            }
            bookings
        } catch (e: ExecutionException) {
            e.printStackTrace()
            throw RuntimeException("Failed to find overlapping bookings", e)
        }
    }

    fun findAll(): List<Booking> {
        return try {
            val bookings = mutableListOf<Booking>()
            val querySnapshot = firestore.collection(collectionName).get().get()
            for (doc in querySnapshot.documents) {
                bookings.add(documentToBooking(doc))
            }
            bookings
        } catch (e: ExecutionException) {
            throw RuntimeException("Failed to find all bookings", e)
        }
    }

    fun findByClientId(clientId: Long): List<Booking> {
        return try {
            val bookings = mutableListOf<Booking>()
            val querySnapshot = firestore.collection(collectionName)
                .whereEqualTo("clientId", clientId).get().get()
            for (doc in querySnapshot.documents) {
                bookings.add(documentToBooking(doc))
            }
            bookings
        } catch (e: ExecutionException) {
            throw RuntimeException("Failed to find bookings by client ID", e)
        }
    }

    fun findByProfessionalId(professionalId: Long): List<Booking> {
        return try {
            val bookings = mutableListOf<Booking>()
            val querySnapshot = firestore.collection(collectionName)
                .whereEqualTo("professionalId", professionalId).get().get()
            for (doc in querySnapshot.documents) {
                bookings.add(documentToBooking(doc))
            }
            bookings
        } catch (e: ExecutionException) {
            throw RuntimeException("Failed to find bookings by professional ID", e)
        }
    }

    fun findById(id: String): Booking? {
        return try {
            val docRef = firestore.collection(collectionName).document(id.toString())
            val doc = docRef.get().get()
            if (doc.exists()) {
                documentToBooking(doc)
            } else {
                null
            }
        } catch (e: ExecutionException) {
            throw RuntimeException("Failed to find booking by ID", e)
        }
    }

    fun existsById(id: String): Boolean {
        return try {
            val docRef = firestore.collection(collectionName).document(id.toString())
            val doc = docRef.get().get()
            doc.exists()
        } catch (e: ExecutionException) {
            throw RuntimeException("Failed to check if booking exists", e)
        }
    }

    fun deleteById(id: String) {
        try {
            firestore.collection(collectionName).document(id.toString()).delete().get()
        } catch (e: ExecutionException) {
            throw RuntimeException("Failed to delete booking", e)
        }
    }

    private fun documentToBooking(doc: DocumentSnapshot): Booking {
        val id = doc.getString("id") ?: 0L
        val clientId = doc.getLong("clientId") ?: 0L
        val professionalId = doc.getLong("professionalId") ?: 0L

        val startTime = doc.getTimestamp("startTime")?.toLocalDateTime() ?: LocalDateTime.MIN
        val endTime = doc.getTimestamp("endTime")?.toLocalDateTime() ?: LocalDateTime.MIN

        return Booking(id.toString(), clientId, professionalId, startTime, endTime)
    }

    // 🔧 Extension to convert LocalDateTime → Firestore Timestamp
    private fun LocalDateTime.toFirestoreTimestamp(): Timestamp =
        Timestamp.of(java.sql.Timestamp.valueOf(this))

    // 🔧 Extension to convert Firestore Timestamp → LocalDateTime
    private fun Timestamp.toLocalDateTime(): LocalDateTime =
        this.toSqlTimestamp().toLocalDateTime()
}
