
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

apply(from = "../gradle/kotlin.gradle")
apply(from = "../gradle/bintray.gradle")
apply(from = "../gradle/dokka.gradle")

val compileTestKotlin: KotlinCompile by tasks

plugins {
    java
}

// IMPORTANT: Required for compiling classes in test dependencies. It *MUST* be before dependencies
compileTestKotlin.dependsOn(tasks.getByPath(":port_http_server:compileTestKotlin"))

val entityTests: SourceSetOutput = project(":port_http_server").sourceSets["test"].output
val entityTestsHexagonWeb: SourceSetOutput = project(":hexagon_web").sourceSets["test"].output

dependencies {
    val jettyVersion = properties["jettyVersion"]

    "api"(project(":http_server_servlet"))
    "api"("org.eclipse.jetty:jetty-webapp:$jettyVersion")
    "api"("org.eclipse.jetty.http2:http2-server:$jettyVersion")
    "api"("org.eclipse.jetty:jetty-alpn-java-server:$jettyVersion")

    "testImplementation"(project(":http_client_ahc"))
    "testImplementation"(entityTests)
    "testImplementation"(project(":hexagon_web"))
    "testImplementation"(entityTestsHexagonWeb)
}
