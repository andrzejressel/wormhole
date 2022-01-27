plugins {
    java
    groovy
}

version = "1.0"
description = "Spock Framework - Example Project"

// Spock works with Java 1.8 and above
//sourceCompatibility = 11

repositories {
    // Spock releases are available from Maven Central
    mavenCentral()
    // Spock snapshots are available from the Sonatype OSS snapshot repository
    maven { setUrl("https://oss.sonatype.org/content/repositories/snapshots/") }
    maven { setUrl("https://packages.jetbrains.team/maven/p/ij/intellij-dependencies") }
}

dependencies {
//    // mandatory dependencies for using Spock
    implementation("org.codehaus.groovy:groovy:3.0.8")
    testImplementation(platform("org.spockframework:spock-bom:2.0-groovy-3.0"))
    testImplementation("org.spockframework:spock-core")
//
//    // optional dependencies for using Spock
    testImplementation("org.hamcrest:hamcrest-core:2.2")   // only necessary if Hamcrest matchers are used
    testRuntimeOnly("net.bytebuddy:byte-buddy:1.12.7") // allows mocking of classes (in addition to interfaces)
    testRuntimeOnly("org.objenesis:objenesis:3.2" )     // allows mocking of classes without default constructor (together with ByteBuddy or CGLIB)
//
    implementation("org.jetbrains.pty4j:pty4j:0.12.7")
    implementation("net.java.dev.jna:jna:5.10.0")
    implementation("net.java.dev.jna:jna-platform:5.10.0")
    implementation("org.jetbrains.pty4j:purejavacomm:0.0.11.1")
    implementation("org.slf4j:slf4j-api:1.7.33")
    testImplementation("com.google.guava:guava:31.0.1-jre")
//
}


tasks {
    named<Test>("test") {
        useJUnitPlatform()
        testLogging {
            events("passed", "skipped", "failed")
        }
    }
}