plugins {
    java
    id("org.springframework.boot") version "4.2.0"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "nl.tijsbeek"
version = "0.0.1-SNAPSHOT"
description = "privacy-guardian"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(27)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
    testImplementation("org.springframework.boot:spring-boot-starter-thymeleaf-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
