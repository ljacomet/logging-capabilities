package dev.jacomet.gradle.plugins.logging

import spock.lang.Ignore
import spock.lang.Unroll

import static org.gradle.testkit.runner.TaskOutcome.FAILED
import static org.gradle.testkit.runner.TaskOutcome.SUCCESS

class LoggingCapabilitiesPluginSelectionFunctionalTest extends AbstractLoggingCapabilitiesPluginFunctionalTest {

    @Unroll
    def "can select logback in case of conflict (with extra #additional)"() {
        given:
        withBuildScript("""
            plugins {
                `java-library`
                id("dev.jacomet.logging-capabilities")
            }

            repositories {
                mavenCentral()
            }

            loggingCapabilities {
                selectSlf4JBinding("ch.qos.logback:logback-classic:1.2.3")
            }

            dependencies {
                runtimeOnly("ch.qos.logback:logback-classic:1.2.3")
                runtimeOnly("org.slf4j:slf4j-simple:1.7.27")
${additional.collect { "                runtimeOnly(\"$it\")" }.join("\n")}                
            }

            tasks.register("doIt") {
                doLast {
                    println(configurations.runtimeClasspath.files)
                }
            }
""")
        when:
        def result = build(['doIt'])

        then:
        outcomeOf(result, ':doIt') == SUCCESS
        result.output.contains("logback-classic-1.2.3.jar")

        where:
        additional << [[], ["org.slf4j:slf4j-log4j12:1.7.27"], ["org.slf4j:slf4j-jcl:1.7.27", "org.slf4j:slf4j-log4j12:1.7.27"]]
    }

    def "fail to select logback in case of conflict but logback not available"() {
        given:
        withBuildScript("""
            plugins {
                `java-library`
                id("dev.jacomet.logging-capabilities")
            }

            repositories {
                mavenCentral()
            }
            
            loggingCapabilities {
                selectSlf4JBinding("ch.qos.logback:logback-classic:1.2.3")
            }


            dependencies {
                runtimeOnly("org.slf4j:slf4j-simple:1.7.27")
                runtimeOnly("org.slf4j:slf4j-log4j12:1.7.27")
            }

            tasks.register("doIt") {
                doLast {
                    println(configurations.runtimeClasspath.files)
                }
            }
""")
        expect:
        buildAndFail(['doIt']) {
            outcomeOf(delegate, ':doIt') == FAILED
            // TODO improve the error
            delegate.output.contains("ch.qos.logback:logback-classic:0 is not a valid candidate")
        }
    }

    @Ignore
    @Unroll
    def "configuring logger leaves no open conflicts (using #prefered)"() {
        given:
        withBuildScript("""
            plugins {
                `java-library`
                id("dev.jacomet.logging-capabilities")
            }

            repositories {
                mavenCentral()
            }
            
            dependencies {
                implementation("org.slf4j:slf4j-api:1.7.27")
                implementation("org.apache.logging.log4j:log4j-api:2.12.1")
                
                implementation("log4j:log4j:1.2.17")
                implementation("commons-logging:commons-logging:1.2")
                
                runtimeOnly("ch.qos.logback:logback-classic:1.2.3")
                runtimeOnly("org.slf4j:slf4j-simple:1.7.27")
                runtimeOnly("org.slf4j:slf4j-log4j12:1.7.27")
                runtimeOnly("org.slf4j:log4j-over-slf4j:1.7.27")
                runtimeOnly("org.slf4j:jul-to-slf4j:1.7.27")
                runtimeOnly("org.slf4j:slf4j-jdk14:1.7.27")
                runtimeOnly("org.slf4j:slf4j-jcl:1.7.27")
                runtimeOnly("org.slf4j:jcl-over-slf4j:1.7.27")

                runtimeOnly("org.apache.logging.log4j:log4j-jul:2.12.1")
                runtimeOnly("org.apache.logging.log4j:log4j-1.2-api:2.12.1")
                runtimeOnly("org.apache.logging.log4j:log4j-slf4j-impl:2.12.1")
                runtimeOnly("org.apache.logging.log4j:log4j-to-slf4j:2.12.1")
                runtimeOnly("org.apache.logging.log4j:log4j-jcl:2.12.1")
            }

            tasks.register("doIt") {
                doLast {
                    println(configurations.runtimeClasspath.files)
                }
            }
""")
        when:
        def result = build(['doIt'])

        then:
        outcomeOf(result, ':doIt') == SUCCESS
        result.output.contains(selected)

        where:
        prefered                                | selected
        "ch.qos.logback:logback-classic:1.2.3"  | "logback-classic-1.2.3.jar"
        "org.slf4j:slf4j-simple:1.7.27"         | "slf4j-simple-1.7.27.jar"
    }
}
