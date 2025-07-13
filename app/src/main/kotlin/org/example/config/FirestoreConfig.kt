package org.example.config

import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.firestore.Firestore
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.cloud.FirestoreClient
import jakarta.annotation.PostConstruct
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.io.InputStream

@Configuration
open class FirestoreConfig {

    @PostConstruct
    fun init() {
        try {
            val serviceAccount: InputStream = javaClass
                .classLoader
                .getResourceAsStream("ServiceAccountKey2.json")

            val options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .setProjectId("booking-98ee7")
                .build()

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options)
            }
        } catch (e: Exception) {
            throw RuntimeException("Failed to initialize Firebase", e)
        }
    }

    @Bean
    open fun getFirestore(): Firestore {
        return FirestoreClient.getFirestore()
    }
}
