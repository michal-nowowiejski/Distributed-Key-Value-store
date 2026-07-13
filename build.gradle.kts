plugins {
    // Adds `run` task and packaging support for a runnable Java app.
    application
}

group = "io.github.michalnowowiejski"
version = "0.1.0"

repositories {
    mavenCentral()
}

dependencies {
    // JUnit 5 (Jupiter) for tests. The BOM keeps the module versions aligned.
    testImplementation(platform("org.junit:junit-bom:5.11.3"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    implementation("org.rocksdb:rocksdbjni:9.7.3")
    implementation("info.picocli:picocli:4.7.7")
    implementation("io.javalin:javalin:7.2.2")
    implementation("org.slf4j:slf4j-simple:2.0.17")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.22.1")
}

application {
    mainClass = "io.github.michalnowowiejski.distributedkv.Main"
}

tasks.withType<JavaCompile>().configureEach {
    // Compile to Java 21 (LTS) bytecode regardless of which JDK runs Gradle.
    options.release = 21
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}
