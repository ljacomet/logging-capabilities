package dev.jacomet.gradle.plugins.logging

import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

abstract class AbstractLoggingCapabilitiesPluginFunctionalTest extends Specification {

    @Rule
    TemporaryFolder testFolder = new TemporaryFolder()

    def setup() {
        testFolder.newFile('settings.gradle.kts') << 'rootProject.name = "test-project"'
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
