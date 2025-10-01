plugins {
    java
    application
    id("org.javamodularity.moduleplugin") version "1.8.15"
    id("org.openjfx.javafxplugin") version "0.0.13"
    id("org.beryx.jlink") version "2.25.0"
}

group = "pl.kalin"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

val junitVersion = "5.12.1"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

application {
    mainModule.set("pl.kalin.simpledms")
    mainClass.set("pl.kalin.simpledms.HelloApplication")
}

javafx {
    version = "21.0.6"
    modules = listOf("javafx.controls", "javafx.fxml")
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter-api:", junitVersion)
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:", junitVersion)
    // ZXing do QR kodów
    implementation("com.google.zxing:core:3.5.3")
    implementation("com.google.zxing:javase:3.5.3")
    // PDFBox do obsługi PDF
    implementation("org.apache.pdfbox:pdfbox:2.0.30")
    // Spring Boot
    implementation("org.springframework.boot:spring-boot-starter:3.3.0")
    // Spring Boot autoconfigure (wymagane do @SpringBootApplication)
    implementation("org.springframework.boot:spring-boot-autoconfigure:3.3.0")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

jlink {
    imageZip.set(layout.buildDirectory.file("/distributions/app-${javafx.platform.classifier}.zip"))
    options.set(listOf("--strip-debug", "--compress", "2", "--no-header-files", "--no-man-pages"))
    launcher {
        name = "app"
    }
}
