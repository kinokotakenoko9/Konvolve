plugins {
    kotlin("jvm") version "2.1.10"
    id("org.jetbrains.kotlinx.benchmark") version "0.4.14"
    kotlin("plugin.serialization") version "2.1.10"
}

group = "konvolve"
version = "0.0.1"

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

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.1")
    testImplementation("org.junit.jupiter:junit-jupiter:5.8.1")

    val mockkVersion = "1.14.2"
    testImplementation("io.mockk:mockk:${mockkVersion}")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(23)
}

benchmark {
    targets {
        register("main") {
            this as kotlinx.benchmark.gradle.JvmBenchmarkTarget
            jmhVersion = "1.36"
        }
    }

    configurations {
        named("main") {
            iterations = 1
            warmups = 1
            outputTimeUnit = "ms"
            mode = "AverageTime"
            reportFormat = "json"
        }
    }

}

tasks.register<JavaExec>("benchmarkPlot") {
    val latestJson = file("build/reports/benchmarks/main")
        .walkTopDown()
        .filter { it.name == "main.json" }
        .maxByOrNull { it.lastModified() }
        ?: throw GradleException("No benchmark JSON file found.")

    group = "reporting"
    description = "Generates benchmark plot from the latest benchmark result."

    mainClass.set("benchmark.PlotGeneratorKt")
    classpath = sourceSets["main"].runtimeClasspath
    args(latestJson.absolutePath)
}
