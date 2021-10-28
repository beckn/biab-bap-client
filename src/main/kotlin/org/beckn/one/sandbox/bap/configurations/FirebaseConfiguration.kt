package org.beckn.one.sandbox.bap.configurations

import org.springframework.context.annotation.Primary
import org.springframework.core.io.ClassPathResource
import java.io.IOException
import com.google.firebase.FirebaseOptions
import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.io.InputStream

@Configuration
class FirebaseConfiguration {
    @Primary
    @Bean
    fun firebaseInit() {
        var inputStream: InputStream? = null
        try {
            inputStream = ClassPathResource("firebase_config.json").inputStream
        } catch (e3: IOException) {
            e3.printStackTrace()
        }
        try {
            val options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(inputStream))
                .build()
            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options)
            }
            println("Firebase Initialize")
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}