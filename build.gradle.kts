plugins {
    kotlin("jvm") version "1.5.10"
    java
}

group = "org.example.e3fxgaming"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {

    implementation(kotlin("stdlib"))

    val junitVersion = "5.8.1"

    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-params:$junitVersion")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}