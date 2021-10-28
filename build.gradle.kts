import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  id("jacoco")
  id("org.springframework.boot") version "2.5.0"
  id("io.spring.dependency-management") version "1.0.11.RELEASE"
  id("org.jetbrains.kotlin.plugin.serialization") version "1.5.10"
  id("org.jetbrains.kotlin.plugin.allopen") version ("1.5.20")
  kotlin("jvm") version "1.4.31"
  kotlin("plugin.spring") version "1.4.31"
  kotlin("kapt") version "1.4.31"
}

group = "org.beckn.one.sandbox"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_11

allOpen {
  annotation("org.beckn.one.sandbox.bap.Open")
}

repositories {
  mavenCentral()
  maven {
    url = uri("s3://beckn-maven-artifacts/releases")
    authentication {
      val awsIm by registering(AwsImAuthentication::class)
    }
  }
  mavenLocal()
}

dependencies {
  implementation("org.projectlombok:lombok:1.18.20")
  val retrofitVersion = "2.9.+"
  val resilience4jVersion = "1.7.+"

  kapt("io.arrow-kt:arrow-meta:0.13.2")
  kapt("org.mapstruct:mapstruct-processor:1.4.2.Final")
  kapt("org.mapstruct:mapstruct-jdk8:1.4.2.Final")

  implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.2.+")
  implementation("org.springframework.boot:spring-boot-starter-actuator")
  implementation("org.springframework.boot:spring-boot-starter-web")
  implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
  implementation("org.jetbrains.kotlin:kotlin-reflect")
  implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
  implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.12.+")
  implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.12.+")
  implementation("io.arrow-kt:arrow-optics:0.13.2")
  implementation("com.squareup.retrofit2:retrofit:$retrofitVersion")
  implementation("com.squareup.retrofit2:converter-jackson:$retrofitVersion")
  implementation("com.squareup.retrofit2:retrofit-mock:$retrofitVersion")
  implementation("org.mapstruct:mapstruct:1.4.2.Final")
  implementation("org.beckn.jvm:beckn-protocol-dtos:0.9.1.18")
  implementation("io.github.resilience4j:resilience4j-retrofit:$resilience4jVersion")
  implementation("io.github.resilience4j:resilience4j-retry:$resilience4jVersion")
  implementation("io.github.resilience4j:resilience4j-circuitbreaker:$resilience4jVersion")
  implementation("org.bouncycastle:bcprov-jdk15on:1.69")
  implementation("commons-codec:commons-codec:1.15")
  implementation("org.springframework.boot:spring-boot-starter-security")
  implementation("org.springframework.security:spring-security-test")
  implementation("io.jsonwebtoken:jjwt:0.9.1")
  implementation("jakarta.xml.bind:jakarta.xml.bind-api:2.3.2")
  implementation("com.google.firebase:firebase-admin:8.1.0")
  implementation("org.litote.kmongo:kmongo:4.2.8")

  testImplementation("org.springframework.boot:spring-boot-starter-test")
  testImplementation("io.kotest:kotest-runner-junit5:4.4.3")
  testImplementation("io.kotest:kotest-extensions-spring:4.4.3")
  testImplementation("com.github.tomakehurst:wiremock-jre8:2.28.0")
  testImplementation("org.mockito.kotlin:mockito-kotlin:3.2.0")
  testImplementation("io.kotest.extensions:kotest-assertions-arrow:1.0.2")
  testImplementation("org.testcontainers:mongodb:1.15.3")

}

tasks.withType<KotlinCompile> {
  kotlinOptions {
    freeCompilerArgs = listOf("-Xjsr305=strict")
    jvmTarget = "11"
  }
}

tasks.withType<Test> {
  useJUnitPlatform()
}

jacoco {
  toolVersion = "0.8.7"
}

tasks.jacocoTestReport {
  dependsOn("build")
  reports {
    xml.required.set(false)
    csv.required.set(false)
    html.outputLocation.set(layout.buildDirectory.dir("jacocoHtml"))
  }
}
