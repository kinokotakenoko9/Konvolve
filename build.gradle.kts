plugins {
    kotlin("jvm") version "2.1.10"
    id("org.jetbrains.kotlinx.benchmark") version "0.4.14"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))

    implementation("org.jetbrains.kotlinx:kotlinx-benchmark-runtime:0.4.14")

    implementation("org.slf4j:slf4j-simple:1.6.1")
    implementation("org.slf4j:slf4j-api:1.6.1")

    implementation("org.jetbrains.lets-plot:lets-plot-kotlin-jvm:4.10.0")
    implementation("org.jetbrains.lets-plot:lets-plot-image-export:4.4.0")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(23)
}
