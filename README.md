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
git clone git@github.com:kinokotakenoko9/Konvolve.git
```
```
cd Konvolve
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

Asynchronous convolution of images on the [dataset](https://data.mendeley.com/datasets/sp4g8h7v8k/1) with Gaussian 3x3 kernel.

```
main summary:
Benchmark                        (kernelName)   (modeName)  Mode  Cnt      Score   Error  Units
BenchmarkPipeline.processImages    Gaussian 9  no parallel  avgt       10524.517          ms/op
BenchmarkPipeline.processImages    Gaussian 9       column  avgt        7806.752          ms/op
BenchmarkPipeline.processImages    Gaussian 9          row  avgt        7716.154          ms/op
BenchmarkPipeline.processImages    Gaussian 9        pixel  avgt       19927.265          ms/op
BenchmarkPipeline.processImages    Gaussian 9      grid 16  avgt        7865.259          ms/op
BenchmarkPipeline.processImages    Gaussian 9      grid 32  avgt        7752.672          ms/op
BenchmarkPipeline.processImages    Gaussian 9     grid 128  avgt        7637.647          ms/op
```

# Testing

Run tests with 
```
./gradlew test
```

Additionally, preview images can be generated with 
```
./gradlew generateFilteredImages
```
for manual testing. Output location: `assets/images/output`
