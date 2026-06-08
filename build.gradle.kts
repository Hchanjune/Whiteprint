import org.gradle.api.plugins.BasePluginExtension
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.bundling.Jar
import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
    id("org.springframework.boot")  version "4.0.6" apply false
    id("io.spring.dependency-management") version "1.1.7" apply false
    kotlin("jvm") version "2.3.20" apply false
    kotlin("plugin.jpa") version "2.3.20" apply false
    kotlin("plugin.spring") version "2.3.20" apply false

    id("com.google.protobuf") version "0.9.4" apply false
}


allprojects {
    group = "org.whiteprint"
    // JitPack injects the requested git tag/commit via the VERSION env var so the
    // published artifact version always matches the coordinate consumers ask for.
    version = System.getenv("VERSION") ?: "0.0.1"

    repositories {
        mavenCentral()
        maven("https://jitpack.io")
    }
}

// Publish every platform:* module as a standalone Maven artifact (the sample:* apps stay unpublished).
subprojects {
    // `include("platform:adapter:web:servlet")`-style paths also create empty intermediate
    // "phantom" projects (e.g. ":platform:adapter") with no java component — gate on the
    // java plugin actually being applied so only real leaf modules get configured.
    if (project.path.startsWith(":platform:")) {
        plugins.withId("java") {
            // Gradle defaults both the jar's archive base name and the Maven artifactId to the
            // leaf project name, which collides across modules that share a leaf directory name
            // (e.g. five different ":...:servlet" projects all producing "servlet-<version>.jar").
            // That collision breaks more than just publishing — bundling two same-named jars into
            // a service's bootJar fails with "BOOT-INF/lib/servlet-<version>.jar is a duplicate".
            // Deriving a unique name from the full module path fixes both at once.
            val uniqueModuleName = project.path.removePrefix(":platform:").replace(":", "-")

            extensions.configure<BasePluginExtension> {
                archivesName.set(uniqueModuleName)
            }

            apply(plugin = "maven-publish")

            // Spring Boot disables the plain `jar` task in favor of the executable `bootJar`;
            // library modules need the reverse so `components["java"]` has something to publish.
            pluginManager.withPlugin("org.springframework.boot") {
                tasks.named<Jar>("jar") {
                    enabled = true
                    // Spring Boot gives the plain jar an "-plain" classifier to avoid clashing
                    // with bootJar's output. Clear it so we publish the standard <name>-<version>.jar
                    // that Maven/Gradle consumers resolve by default.
                    archiveClassifier.set("")
                }
                tasks.named<BootJar>("bootJar") { enabled = false }
            }

            afterEvaluate {
                extensions.configure<PublishingExtension> {
                    publications {
                        create<MavenPublication>("maven") {
                            from(components["java"])
                            artifactId = uniqueModuleName
                        }
                    }
                }
            }
        }
    }
}