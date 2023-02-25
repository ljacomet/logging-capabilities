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
package dev.jacomet.gradle.plugins.logging;

import dev.jacomet.gradle.plugins.logging.extension.LoggingCapabilitiesExtension;
import dev.jacomet.gradle.plugins.logging.rules.CommonsLoggingImplementationRule;
import dev.jacomet.gradle.plugins.logging.rules.Log4J2Alignment;
import dev.jacomet.gradle.plugins.logging.rules.Log4J2Implementation;
import dev.jacomet.gradle.plugins.logging.rules.Log4J2vsSlf4J;
import dev.jacomet.gradle.plugins.logging.rules.Slf4JAlignment;
import dev.jacomet.gradle.plugins.logging.rules.Slf4JImplementation;
import dev.jacomet.gradle.plugins.logging.rules.Slf4JVsJCL;
import dev.jacomet.gradle.plugins.logging.rules.Slf4JVsLog4J2ForJCL;
import dev.jacomet.gradle.plugins.logging.rules.Slf4JvsJUL;
import dev.jacomet.gradle.plugins.logging.rules.Slf4JvsLog4J;
import dev.jacomet.gradle.plugins.logging.rules.Slf4JvsLog4J2ForJUL;
import dev.jacomet.gradle.plugins.logging.rules.Slf4JvsLog4J2ForLog4J;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.util.GradleVersion;

public class LoggingCapabilitiesPlugin implements Plugin<Project> {

    public static final GradleVersion GRADLE_7_0 = GradleVersion.version("7.0");
    private static final GradleVersion GRADLE_6_2 = GradleVersion.version("6.2");
    private static final GradleVersion GRADLE_6 = GradleVersion.version("6.0");
    private static final GradleVersion GRADLE_5_2 = GradleVersion.version("5.2");

    @Override
    public void apply(Project project) {
        DependencyHandler dependencies = project.getDependencies();
        GradleVersion gradleVersion = GradleVersion.current();
        if (gradleVersion.compareTo(GRADLE_6) >= 0) {
            // Only add the extension for Gradle 6 and above
            project.getExtensions().create("loggingCapabilities", LoggingCapabilitiesExtension.class, project.getConfigurations(), dependencies, getAlignmentActivation(dependencies, gradleVersion));
        }
        configureCommonsLogging(dependencies);
        configureJavaUtilLogging(dependencies);
        configureLog4J(dependencies);
        configureSlf4J(dependencies);
        configureLog4J2(dependencies);
        configureLog4J2Implementation(dependencies);

        // ljacomet/logging-capabilities#4
        if (gradleVersion.compareTo(GRADLE_5_2) < 0 || gradleVersion.compareTo(GRADLE_6_2) >= 0) {
            configureAlignment(dependencies);
        }
    }

    private Runnable getAlignmentActivation(DependencyHandler dependencies, GradleVersion gradleVersion) {
        if (gradleVersion.compareTo(GRADLE_6_2) < 0) {
            return () -> configureAlignment(dependencies);
        }
        return () -> {};
    }

    private void configureAlignment(DependencyHandler dependencies) {
        dependencies.components(handler -> {
            handler.all(Slf4JAlignment.class);
            handler.all(Log4J2Alignment.class);
        });
    }

    /**
     * Log4J2 can act as an Slf4J implementation with `log4j-slf4j-impl` or `log4j-slf4j2-impl`.
     * It can also delegate to Slf4J with `log4j-to-slf4j`.
     * <p>
     * Given the above:
     * * `log4j-slf4j-impl`, `log4j-slf4j2-impl` and `log4j-to-slf4j` are exclusive
     */
    private void configureLog4J2(DependencyHandler dependencies) {
        dependencies.components(handler -> {
            handler.withModule(LoggingModuleIdentifiers.LOG4J_SLF4J_IMPL.moduleId, Log4J2vsSlf4J.class);
            handler.withModule(LoggingModuleIdentifiers.LOG4J_SLF4J2_IMPL.moduleId, Log4J2vsSlf4J.class);
            handler.withModule(LoggingModuleIdentifiers.LOG4J_TO_SLF4J.moduleId, Log4J2vsSlf4J.class);
        });
    }

    /**
     * Log4J2 has its own implementation with `log4j-core`.
     * It can also delegate to Slf4J with `log4j-to-slf4j`.
     * <p>
     * Given the above:
     * * `log4j-core` and `log4j-to-slf4j` are exclusive
     */
    private void configureLog4J2Implementation(DependencyHandler dependencies) {
        dependencies.components(handler -> {
            handler.withModule(LoggingModuleIdentifiers.LOG4J_TO_SLF4J.moduleId, Log4J2Implementation.class);
            handler.withModule(LoggingModuleIdentifiers.LOG4J_CORE.moduleId, Log4J2Implementation.class);
        });
    }

    /**
     * Slf4J provides an API, which requires an implementation.
     * Only one implementation can be on the classpath, selected between:
     * * `slf4j-simple`
     * * `logback-classic`
     * * `slf4j-log4j12` to use Log4J 1.2
     * * `slf4j-jcl` to use Jakarta Commons Logging
     * * `slf4j-jdk14` to use Java Util Logging
     * * `log4j-slf4j-impl` to use Log4J2
     */
    private void configureSlf4J(DependencyHandler dependencies) {
        dependencies.components(handler -> {
            handler.withModule(LoggingModuleIdentifiers.SLF4J_SIMPLE.moduleId, Slf4JImplementation.class);
            handler.withModule(LoggingModuleIdentifiers.LOGBACK_CLASSIC.moduleId, Slf4JImplementation.class);
            handler.withModule(LoggingModuleIdentifiers.SLF4J_LOG4J12.moduleId, Slf4JImplementation.class);
            handler.withModule(LoggingModuleIdentifiers.SLF4J_JCL.moduleId, Slf4JImplementation.class);
            handler.withModule(LoggingModuleIdentifiers.SLF4J_JDK14.moduleId, Slf4JImplementation.class);
            handler.withModule(LoggingModuleIdentifiers.LOG4J_SLF4J_IMPL.moduleId, Slf4JImplementation.class);
            handler.withModule(LoggingModuleIdentifiers.LOG4J_SLF4J2_IMPL.moduleId, Slf4JImplementation.class);
        });
    }

    /**
     * `log4j:log4j` can be replaced by:
     * * Slf4j with `log4j-over-slf4j`
     * * Log4J2 with `log4j-1.2-api`
     * <p>
     * Log4J can be used from:
     * * Slf4J API delegating to it with `slf4j-log4j12`
     * * Log4J2 API only through Slf4J delegation
     * <p>
     * Given the above:
     * * `log4j-over-slf4j` and `slf4j-log4j12` are exclusive
     * * `log4j-over-slf4j` and `log4j-1.2-api` and `log4j` are exclusive
     */
    private void configureLog4J(DependencyHandler dependencies) {
        dependencies.components(handler -> {
            handler.withModule(LoggingModuleIdentifiers.LOG4J_OVER_SLF4J.moduleId, Slf4JvsLog4J.class);
            handler.withModule(LoggingModuleIdentifiers.SLF4J_LOG4J12.moduleId, Slf4JvsLog4J.class);

            handler.withModule(LoggingModuleIdentifiers.LOG4J_OVER_SLF4J.moduleId, Slf4JvsLog4J2ForLog4J.class);
            handler.withModule(LoggingModuleIdentifiers.LOG4J12API.moduleId, Slf4JvsLog4J2ForLog4J.class);
            handler.withModule(LoggingModuleIdentifiers.LOG4J.moduleId, Slf4JvsLog4J2ForLog4J.class);
        });
    }

    /**
     * Java Util Logging can be replaced by:
     * * Slf4J with `jul-to-slf4j`
     * * Log4J2 with `log4j-jul`
     * <p>
     * Java Util Logging can be used from:
     * * Slf4J API delegating to it with `slf4j-jdk14`
     * * Log4J2 API only through SLF4J delegation
     * <p>
     * Given the above:
     * * `jul-to-slf4j` and `slf4j-jdk14` are exclusive
     * * `jul-to-slf4j` and `log4j-jul` are exclusive
     */
    private void configureJavaUtilLogging(DependencyHandler dependencies) {
        dependencies.components( handler -> {
            handler.withModule(LoggingModuleIdentifiers.JUL_TO_SLF4J.moduleId, Slf4JvsJUL.class);
            handler.withModule(LoggingModuleIdentifiers.SLF4J_JDK14.moduleId, Slf4JvsJUL.class);

            handler.withModule(LoggingModuleIdentifiers.JUL_TO_SLF4J.moduleId, Slf4JvsLog4J2ForJUL.class);
            handler.withModule(LoggingModuleIdentifiers.LOG4J_JUL.moduleId, Slf4JvsLog4J2ForJUL.class);
        });
    }

    /**
     * `commons-logging:commons-logging` can be replaced by:
     * * Slf4J with `org.slf4j:jcl-over-slf4j`
     * * Log4J2 with `org.apache.logging.log4j:log4j-jcl` _which requires `commons-logging`_
     * * Spring JCL with `org.springframework:spring-jcl`
     * <p>
     * `commons-logging:commons-logging` can be used from:
     * * Slf4J API delegating to it with `org.slf4j:slf4j-jcl`
     * * Log4J2 API only through Slf4J delegation
     * <p>
     * Given the above:
     * * `jcl-over-slf4j` and `slf4j-jcl` are exclusive
     * * `commons-logging`, `jcl-over-slf4j` and `spring-jcl` are exclusive
     * * `jcl-over-slf4j` and `log4j-jcl` are exclusive
     */
    private void configureCommonsLogging(DependencyHandler dependencies) {
        dependencies.components( handler -> {
            handler.withModule(LoggingModuleIdentifiers.COMMONS_LOGGING.moduleId, CommonsLoggingImplementationRule.class);
            handler.withModule(LoggingModuleIdentifiers.JCL_OVER_SLF4J.moduleId, CommonsLoggingImplementationRule.class);
            handler.withModule(LoggingModuleIdentifiers.SPRING_JCL.moduleId, CommonsLoggingImplementationRule.class);

            handler.withModule(LoggingModuleIdentifiers.JCL_OVER_SLF4J.moduleId, Slf4JVsJCL.class);
            handler.withModule(LoggingModuleIdentifiers.SLF4J_JCL.moduleId, Slf4JVsJCL.class);

            handler.withModule(LoggingModuleIdentifiers.JCL_OVER_SLF4J.moduleId, Slf4JVsLog4J2ForJCL.class);
            handler.withModule(LoggingModuleIdentifiers.LOG4J_JCL.moduleId, Slf4JVsLog4J2ForJCL.class);
        });
    }
}