plugins {
    id("java")
    application
}

var mainClassName = "com.yassenhigazi.jlox.JLox"

group = "com.yassenhigazi.jlox"
version = "0.0.1"

application {
    mainClass = mainClassName
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.named<Jar>("jar") {
    manifest {
        attributes["Main-Class"] = mainClassName
    }
}

tasks.test {
    useJUnitPlatform()
}