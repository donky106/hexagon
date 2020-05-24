
apply(from = "../gradle/kotlin.gradle")
apply(from = "../gradle/bintray.gradle")
apply(from = "../gradle/dokka.gradle")

dependencies {
    val slf4jVersion = properties["slf4jVersion"]
    val logbackVersion = properties["logbackVersion"]
    val jacksonVersion = properties["jacksonVersion"]

    "runtimeOnly"("org.slf4j:jcl-over-slf4j:$slf4jVersion")
    "runtimeOnly"("org.slf4j:jul-to-slf4j:$slf4jVersion")

    "api"("org.slf4j:slf4j-api:$slf4jVersion")
    "api"("ch.qos.logback:logback-classic:$logbackVersion") {
        exclude(group = "org.slf4j")
    }

    "api"("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:$jacksonVersion")
    "api"("com.fasterxml.jackson.dataformat:jackson-dataformat-csv:$jacksonVersion")
    "api"("com.fasterxml.jackson.dataformat:jackson-dataformat-properties:$jacksonVersion")
    "api"("com.fasterxml.jackson.datatype:jackson-datatype-jdk8:$jacksonVersion")
    "api"("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:$jacksonVersion")
    "api"("com.fasterxml.jackson.module:jackson-module-parameter-names:$jacksonVersion")
    "api"("com.fasterxml.jackson.module:jackson-module-kotlin:$jacksonVersion")
}
