/*
 * Copyright 2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dev.jacomet.gradle.plugins.logging

import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification
import spock.lang.Unroll

import static org.gradle.testkit.runner.TaskOutcome.FAILED

class LoggingCapabilitiesPluginFunctionalTest extends Specification {

    @Rule
    TemporaryFolder testFolder = new TemporaryFolder()

    def setup() {
        testFolder.newFile('settings.gradle.kts') << 'rootProject.name = "test-project"'
    }

    @Unroll
    def "can detect Slf4J logger implementation conflicts with #first and #second"() {
        given:
        withBuildScriptWithDependencies(first, second)

        expect:
        buildAndFail(['doIt']) {
            assert outcomeOf(delegate, ':doIt') == FAILED
            assert output.contains("conflict on capability 'dev.jacomet.logging:slf4j-impl:1.0'")
        }

        where:
        first                           | second
        'org.slf4j:slf4j-simple:1.7.27' | 'ch.qos.logback:logback-classic:1.2.3'
        'org.slf4j:slf4j-simple:1.7.27' | 'org.slf4j:slf4j-log4j12:1.7.27'
        'org.slf4j:slf4j-simple:1.7.27' | 'org.slf4j:slf4j-jcl:1.7.27'
        'org.slf4j:slf4j-simple:1.7.27' | 'org.slf4j:slf4j-jdk14:1.7.27'
        'org.slf4j:slf4j-simple:1.7.27' | 'org.apache.logging.log4j:log4j-slf4j-impl:2.9.1'

    }

    @Unroll
    def "can detect Slf4J logger implementation / bridge implementation conflicts with #first and #second"() {
        given:
        withBuildScriptWithDependencies(first, second)

        expect:
        buildAndFail(['doIt']) {
            assert outcomeOf(delegate, ':doIt') == FAILED
            assert output.contains("conflict on capability 'dev.jacomet.logging:$capability:1.7.27'")
        }

        where:
        first                               | second                            | capability
        'org.slf4j:jcl-over-slf4j:1.7.27'   | 'org.slf4j:slf4j-jcl:1.7.27'      | 'slf4j-vs-jcl'
        'org.slf4j:jul-to-slf4j:1.7.27'     | 'org.slf4j:slf4j-jdk14:1.7.27'    | 'slf4j-vs-jul'
        'org.slf4j:log4j-over-slf4j:1.7.27' | 'org.slf4j:slf4j-log4j12:1.7.27'  | 'slf4j-vs-log4j'
    }

    @Unroll
    def "can detect Slf4J bridge implementations vs native logger implementations with #first and #second"() {
        given:
        withBuildScriptWithDependencies(first, second)

        expect:
        buildAndFail(['doIt']) {
            assert outcomeOf(delegate, ':doIt') == FAILED
            assert output.contains("conflict on capability 'dev.jacomet.logging:$capability:1.0'")
        }

        where:
        first                               | second                                            | capability
        'org.slf4j:jcl-over-slf4j:1.7.27'   | 'commons-logging:commons-logging:1.2'             | 'commons-logging-impl'
        'org.slf4j:log4j-over-slf4j:1.7.27' | 'log4j:log4j:1.2.9'                               | 'slf4j-vs-log4j2-log4j'
        'org.slf4j:log4j-over-slf4j:1.7.27' | 'org.apache.logging.log4j:log4j-1.2-api:2.9.1'    | 'slf4j-vs-log4j2-log4j'
    }

    def "can detect Log4J2 logger implementation / bridge implementation conflict"() {
        given:
        withBuildScriptWithDependencies('org.apache.logging.log4j:log4j-slf4j-impl:2.9.1', 'org.apache.logging.log4j:log4j-to-slf4j:2.9.1')

        expect:
        buildAndFail(['doIt']) {
            assert outcomeOf(delegate, ':doIt') == FAILED
            assert output.contains("conflict on capability 'dev.jacomet.logging:log4j2-vs-slf4j:2.9.1'")
        }
    }

    @Unroll
    def "can detect conflicting bridge implementations from Slf4J and Log4J2 with #first and #second"() {
        given:
        withBuildScriptWithDependencies(first, second)

        expect:
        buildAndFail(['doIt']) {
            assert outcomeOf(delegate, ':doIt') == FAILED
            assert output.contains("conflict on capability 'dev.jacomet.logging:$capability:1.0'")
        }

        where:
        first                               | second                                        | capability
        'org.slf4j:jul-to-slf4j:1.7.27'     | 'org.apache.logging.log4j:log4j-jul:2.9.1'    | 'slf4j-vs-log4j2-jul'
        'org.slf4j:jcl-over-slf4j:1.7.27'   | 'org.apache.logging.log4j:log4j-jcl:2.9.1'    | 'slf4j-vs-log4j2-jcl'
    }

    TaskOutcome outcomeOf(BuildResult result, String path) {
        result.task(path)?.outcome
    }

    BuildResult build(List<String> args) {
        gradleRunnerFor(args).build()
    }

    void buildAndFail(List<String> args, @DelegatesTo(BuildResult) Closure verifications) {
        def result = gradleRunnerFor(args).buildAndFail()
        verifications.delegate = result
        verifications()
    }

    GradleRunner gradleRunnerFor(List<String>  args) {
        GradleRunner.create()
                .forwardOutput()
                .withPluginClasspath()
                .withProjectDir(testFolder.root)
                .withArguments(args + ["-s"])
    }

    void withBuildScript(String content) {
        testFolder.newFile("build.gradle.kts") << content
    }

    void withBuildScriptWithDependencies(String... dependencies) {
        testFolder.newFile("build.gradle.kts") << """
            plugins {
                `java-library`
                id("dev.jacomet.logging-capabilities")
            }

            repositories {
                mavenCentral()
            }

            dependencies {
${dependencies.collect { "                implementation(\"$it\")" }.join("\n")}
            }

            tasks.register("doIt") {
                doLast {
                    println(configurations.compileClasspath.files)
                }
            }
        """
    }
}