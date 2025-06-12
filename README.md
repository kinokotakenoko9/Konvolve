# Konvolve 
Kotlin library for image convolution with parallel processing

# Usage

## Adding library

### JitPack

Add this to the `build.gradle.kts`
```kotlin
repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    implementation("com.github.kinokotakenoko9:Konvolve:1.0.0")
}
```
### Locally 
If you want to install library locally, add the `.jar` dependency:
1. Copy `.jar` file into `/libs`(or other location depending on your project structure)
2. Add this to the `build.gradle.kts` 
```kotlin
dependencies {
    implementation(files("libs/konvolve-1.0.0.jar"))
}
```

## Examples
```kotlin
fun main() {
    Image("assets/images/input", "flower")
        .setParallelMode(GridParallelMode(4, 32))
        .applyKernel(GaussianKernel(5))
        .writeToFile("gs5-grid32", "assets/images/output")
}

```
See `src/main/kotlin/demo` for more example usage

## Api
Documentation is available in the Wiki

# Build
Clone this repository
```
git clone git@github.com:kinokotakenoko9/Parallel-image-filtering.git
```
```
cd Parallel-image-filtering
```
Build the library with
```
./gradlew build
``` 
the compiled `.jar` will be located at: `build/libs/konvolve-1.0.0.jar`
> [!NOTE]
> For a successful build java 23 (or higher) is required
# Benchmarks

## Running
```
./gradlew benchmark
```
```
./gradlew benchmarkPlot
```

## Configuration
Benchmark configuration can be found in `build.gradle.kts` and `src/main/kotlin/benchmark/Benchmark.kt`

## Results 
Performance details and comparisons are located in `assets/benchmarks`

