plugins {
	kotlin("jvm")
	kotlin("plugin.spring")
	kotlin("plugin.jpa")
	id("org.springframework.boot")
	id("io.spring.dependency-management")
}

group = "org.whiteprint.service.user"
version = "0.0.1-SNAPSHOT"
description = "SpringMVC CommandSideServer"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

repositories {
	mavenCentral()
	maven("https://jitpack.io")
}

dependencies {
	implementation(project(":platform:core:kernel"))

	implementation(project(":platform:adapter:web:servlet"))
	implementation(project(":platform:adapter:security:verifier:servlet"))
	implementation(project(":platform:adapter:lock:distributed"))
	implementation(project(":platform:adapter:event:outbox"))
	implementation(project(":platform:adapter:event:publisher"))
	implementation(project(":platform:adapter:event:subscriber"))

	runtimeOnly("org.postgresql:postgresql")

	implementation("org.mockito.kotlin:mockito-kotlin:5.2.1")
	testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

kotlin {
	compilerOptions {
		freeCompilerArgs.addAll("-Xjsr305=strict", "-Xannotation-default-target=param-property")
	}
}

allOpen {
	annotation("jakarta.persistence.Entity")
	annotation("jakarta.persistence.MappedSuperclass")
	annotation("jakarta.persistence.Embeddable")
}

tasks.withType<Test> {
	useJUnitPlatform()
}
