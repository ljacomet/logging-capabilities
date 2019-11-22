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

import dev.jacomet.gradle.plugins.logging.rules.CommonsLoggingImplementationRule;
import dev.jacomet.gradle.plugins.logging.rules.Log4J2vsSlf4J;
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

class LoggingCapabilitiesPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        configureCommonsLogging(project.getDependencies());
        configureJavaUtilLogging(project.getDependencies());
        configureLog4J(project.getDependencies());
        configureSlf4J(project.getDependencies());
        configureLog4J2(project.getDependencies());
    }

    /**
     * Log4J2 can act as an Slf4J implementation with `log4j-slf4j-impl`.
     * It can also delegate to Slf4J with `log4j-to-slf4j`.
     *
     * Given the above:
     * * `log4j-slf4j-impl` and `log4j-to-slf4j` are exclusive
     */
    private void configureLog4J2(DependencyHandler dependencies) {
        dependencies.components(handler -> {
            handler.withModule("org.apache.logging.log4j:log4j-slf4j-impl", Log4J2vsSlf4J.class);
            handler.withModule("org.apache.logging.log4j:log4j-to-slf4j", Log4J2vsSlf4J.class);
        });

    }

    /**
     * Slf4J provides an API, which requires an implementation.
     * Only one implementation can be on the classpath, selected between:
     * * `slf4j-simple`
     * * `logback-classic`
     * * `slf4j-log4j12` to use Log4J 1.2
     * * `sl4j-jcl` to use Jakarta Commons Logging
     * * `slf4j-jdk14` to use Java Util Logging
     * * `log4j-slf4j-impl` to use Log4J2
     */
    private void configureSlf4J(DependencyHandler dependencies) {
        dependencies.components(handler -> {
            handler.withModule("org.slf4j:slf4j-simple", Slf4JImplementation.class);
            handler.withModule("ch.qos.logback:logback-classic", Slf4JImplementation.class);
            handler.withModule("org.slf4j:slf4j-log4j12", Slf4JImplementation.class);
            handler.withModule("org.slf4j:slf4j-jcl", Slf4JImplementation.class);
            handler.withModule("org.slf4j:slf4j-jdk14", Slf4JImplementation.class);
            handler.withModule("org.apache.logging.log4j:log4j-slf4j-impl", Slf4JImplementation.class);
        });
    }

    /**
     * `log4j:log4j` can be replaced by:
     * * Slf4j with `log4j-over-slf4j`
     * * Log4J2 with `log4j-1.2-api`
     *
     * Log4J can be used from:
     * * Slf4J API delegating to it with `slf4j-log4j12`
     * * Log4J2 API only through Slf4J delegation
     *
     * Given the above:
     * * `log4j-over-slf4j` and `slf4j-log4j12` are exclusive
     * * `log4j-over-slf4j` and `log4j-1.2-api` and `log4j` are exclusive
     */
    private void configureLog4J(DependencyHandler dependencies) {
        dependencies.components(handler -> {
            handler.withModule("org.slf4j:log4j-over-slf4j", Slf4JvsLog4J.class);
            handler.withModule("org.slf4j:slf4j-log4j12", Slf4JvsLog4J.class);

            handler.withModule("org.slf4j:log4j-over-slf4j", Slf4JvsLog4J2ForLog4J.class);
            handler.withModule("org.apache.logging.log4j:log4j-1.2-api", Slf4JvsLog4J2ForLog4J.class);
            handler.withModule("log4j:log4j", Slf4JvsLog4J2ForLog4J.class);
        });
    }

    /**
     * Java Util Logging can be replaced by:
     * * Slf4J with `jul-to-slf4j`
     * * Log4J2 with `log4j-jul`
     *
     * Java Util Logging can be used from:
     * * Slf4J API delegating to it with `slf4j-jdk14`
     * * Log4J2 API only through SLF4J delegation
     *
     * Given the above:
     * * `jul-to-slf4j` and `slf4j-jdk14` are exclusive
     * * `jul-to-slf4j` and `log4j-jul` are exclusive
     */
    private void configureJavaUtilLogging(DependencyHandler dependencies) {
        dependencies.components( handler -> {
            handler.withModule("org.slf4j:jul-to-slf4j", Slf4JvsJUL.class);
            handler.withModule("org.slf4j:slf4j-jdk14", Slf4JvsJUL.class);

            handler.withModule("org.slf4j:jul-to-slf4j", Slf4JvsLog4J2ForJUL.class);
            handler.withModule("org.apache.logging.log4j:log4j-jul", Slf4JvsLog4J2ForJUL.class);
        });
    }

    /**
     * `commons-logging:commons-logging` can be replaced by:
     * * Slf4J with `org.slf4j:jcl-over-slf4j`
     * * Log4J2 with `org.apache.logging.log4j:log4j-jcl` _which requires `commons-logging`_
     *
     * `commons-logging:commons-logging` can be used from:
     * * Slf4J API delegating to it with `org.slf4j:slf4j-jcl`
     * * Log4J2 API only through Slf4J delegation
     *
     * Given the above:
     * * `jcl-over-slf4j` and `slf4j-jcl` are exclusive
     * * `commons-logging` and `jcl-over-slf4j` are exclusive
     * * `jcl-over-slf4j` and `log4j-jcl` are exclusive
     */
    private void configureCommonsLogging(DependencyHandler dependencies) {
        dependencies.components( handler -> {
            handler.withModule("commons-logging:commons-logging", CommonsLoggingImplementationRule.class);
            handler.withModule("org.slf4j:jcl-over-slf4j", CommonsLoggingImplementationRule.class);

            handler.withModule("org.slf4j:jcl-over-slf4j", Slf4JVsJCL.class);
            handler.withModule("org.slf4j:slf4j-jcl", Slf4JVsJCL.class);

            handler.withModule("org.slf4j:jcl-over-slf4j", Slf4JVsLog4J2ForJCL.class);
            handler.withModule("org.apache.logging.log4j:log4j-jcl", Slf4JVsLog4J2ForJCL.class);
        });
    }
}