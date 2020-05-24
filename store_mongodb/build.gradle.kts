
apply(from = "../gradle/kotlin.gradle")
apply(from = "../gradle/bintray.gradle")
apply(from = "../gradle/dokka.gradle")

dependencies {
    val mongodbVersion = properties["mongodbVersion"]

    "api"(project(":port_store"))
    "api"("org.mongodb:mongodb-driver-sync:$mongodbVersion")
}
