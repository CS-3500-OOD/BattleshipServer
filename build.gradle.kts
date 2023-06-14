
plugins {
    id("java")
    application
}

group = "org.example"
version = "1.0"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    // https://mvnrepository.com/artifact/commons-cli/commons-cli
    implementation("commons-cli:commons-cli:1.3.1")
    // https://mvnrepository.com/artifact/com.fasterxml.jackson.core/jackson-databind
    implementation("com.fasterxml.jackson.core:jackson-databind:2.15.0")
    // https://central.sonatype.com/artifact/org.apache.logging.log4j/log4j-api/2.20.0
    implementation("org.apache.logging.log4j:log4j-api:2.20.0")
    // https://central.sonatype.com/artifact/org.apache.logging.log4j/log4j-core/2.20.0
    implementation("org.apache.logging.log4j:log4j-core:2.20.0")
    // https://www.mongodb.com/docs/drivers/java/sync/current/quick-start/#quick-start
    implementation("org.mongodb:mongodb-driver-sync:4.9.1")
}


tasks.test {
    useJUnitPlatform()
}

var makeServerJar : Boolean = true

application {
    mainClass.set(if(makeServerJar) "server.Server" else "client.Client")
}

tasks {
    val fatJar = register<Jar>("fatJar") {
        dependsOn.addAll(listOf("compileJava", "processResources")) // We need this for Gradle optimization to work
        archiveFileName.set(if(makeServerJar) "Server.jar" else "Client.jar")
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        manifest { attributes(mapOf("Main-Class" to application.mainClass)) } // Provided we set it up in the application plugin configuration
        val sourcesMain = sourceSets.main.get()
        val contents = configurations.runtimeClasspath.get()
            .map { if (it.isDirectory) it else zipTree(it) } +
                sourcesMain.output
        from(contents)
    }
    build {
        dependsOn(fatJar) // Trigger fat jar creation during build
    }
}