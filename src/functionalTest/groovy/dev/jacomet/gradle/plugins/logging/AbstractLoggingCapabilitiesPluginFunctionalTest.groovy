package dev.jacomet.gradle.plugins.logging

import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import spock.lang.Specification
import spock.lang.TempDir

import java.nio.file.Path

abstract class AbstractLoggingCapabilitiesPluginFunctionalTest extends Specification {

    @TempDir
    Path testFolder
    File buildFile

    def setup() {
        buildFile = testFolder.resolve('build.gradle.kts').toFile()
        testFolder.resolve('settings.gradle.kts').toFile() << 'rootProject.name = "test-project"'
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
                .withProjectDir(testFolder.toFile())
                .withArguments(args + ["-s"])
    }

    void withBuildScript(String content) {
        buildFile << content
    }

    void withBuildScriptWithDependencies(String... dependencies) {
        buildFile << """
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
