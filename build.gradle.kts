plugins {
  kotlin("jvm") version "1.9.20"
  application
}

group = "org.itmo"
version = "1.0-SNAPSHOT"

repositories {
  mavenCentral()
}

dependencies {
  testImplementation(kotlin("test"))
  testImplementation("org.assertj:assertj-core:3.6.1")
}

tasks.test {
  useJUnitPlatform()
}

kotlin {
  jvmToolchain(21)
}

application {
  mainClass.set("MainKt")
}