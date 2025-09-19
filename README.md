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

asynchronous convolution of images on the [dataset](https://data.mendeley.com/datasets/sp4g8h7v8k/1) with Gaussian 3x3 kernel.
```
main summary:
Benchmark                        (kernelName)   (modeName)  Mode  Cnt     Score   Error  Units
BenchmarkPipeline.processImages    Gaussian 3  no parallel  avgt       1749.699          ms/op
BenchmarkPipeline.processImages    Gaussian 3       column  avgt       2341.896          ms/op
BenchmarkPipeline.processImages    Gaussian 3          row  avgt       2203.797          ms/op
BenchmarkPipeline.processImages    Gaussian 3        pixel  avgt       6562.766          ms/op
BenchmarkPipeline.processImages    Gaussian 3      grid 16  avgt       2208.743          ms/op
BenchmarkPipeline.processImages    Gaussian 3      grid 32  avgt       1962.827          ms/op
BenchmarkPipeline.processImages    Gaussian 3     grid 128  avgt       2117.524          ms/op
```

or 

```
main summary:
Benchmark                        (kernelName)   (modeName)  Mode  Cnt      Score   Error  Units
BenchmarkPipeline.processImages    Gaussian 9  no parallel  avgt        7073.196          ms/op
BenchmarkPipeline.processImages    Gaussian 9       column  avgt        7939.791          ms/op
BenchmarkPipeline.processImages    Gaussian 9          row  avgt        7798.996          ms/op
BenchmarkPipeline.processImages    Gaussian 9        pixel  avgt       14800.969          ms/op
BenchmarkPipeline.processImages    Gaussian 9      grid 16  avgt        9264.687          ms/op
BenchmarkPipeline.processImages    Gaussian 9      grid 32  avgt        7659.843          ms/op
BenchmarkPipeline.processImages    Gaussian 9     grid 128  avgt        7890.941          ms/op
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
