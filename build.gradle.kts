plugins {
	java
	id("org.springframework.boot") version "3.5.7"
	id("io.spring.dependency-management") version "1.1.7"
	id("com.graphql-java-generator.graphql-gradle-plugin3") version "3.0.1"
}

group = "com.fsi.graphql"
version = "0.0.1-SNAPSHOT"
description = "Client generation of graph ql api"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(25)
	}
}

configurations {
	compileOnly {
		extendsFrom(configurations.annotationProcessor.get())
	}
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter-actuator")
	implementation("com.graphql-java-generator:graphql-java-client-runtime:3.0.1")
	compileOnly("org.projectlombok:lombok")
	developmentOnly("org.springframework.boot:spring-boot-devtools")
	annotationProcessor("org.projectlombok:lombok")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
	useJUnitPlatform()
}

// GraphQL Java Generator configuration
afterEvaluate {
	extensions.configure<Any>("graphql") {
		val groovyObj = this as groovy.lang.GroovyObject
		groovyObj.setProperty("schemaFileFolder", "src/main/resources")
		groovyObj.setProperty("schemaFilePattern", "schema.graphql")
		groovyObj.setProperty("packageName", "com.fsi.graphql.client.generation.generated")
	}
}
