plugins {
  kotlin("jvm") version "1.9.20"
  application
  id("io.github.reyerizo.gradle.jcstress") version "0.8.15"
}

jcstress {
  mode = "quick"
  iterations = "10"
  forks = "1"
  cpuCount = "6"
  timeMillis = "1000"
}

group = "org.itmo"
version = "1.0-SNAPSHOT"

repositories {
  mavenCentral()
}

dependencies {
  implementation("com.fasterxml.jackson.core:jackson-core:2.20.0")
  implementation("com.fasterxml.jackson.core:jackson-databind:2.20.0")

  testImplementation(kotlin("test"))
  testImplementation("org.assertj:assertj-core:3.6.1")
  testImplementation("org.openjdk.jcstress:jcstress-core:0.16")
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