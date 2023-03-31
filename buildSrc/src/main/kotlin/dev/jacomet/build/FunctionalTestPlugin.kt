/*
 * Copyright 2023 the original author or authors.
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
package dev.jacomet.build

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.creating
import org.gradle.kotlin.dsl.getValue
import org.gradle.kotlin.dsl.getting
import org.gradle.kotlin.dsl.the
import org.gradle.plugin.devel.GradlePluginDevelopmentExtension

class FunctionalTestPlugin : Plugin<Project> {
    override
    fun apply(project: Project) {
        val functionalTestSourceSet = project.the<JavaPluginExtension>().sourceSets.create("functionalTest") {

        }
        project.the<GradlePluginDevelopmentExtension>().testSourceSets(functionalTestSourceSet)
        project.configurations.getByName("functionalTestImplementation").extendsFrom(project.configurations.getByName("testImplementation"))

        val test by project.tasks.getting

        val functionalTest by project.tasks.creating(Test::class) {
            testClassesDirs = functionalTestSourceSet.output.classesDirs
            classpath = functionalTestSourceSet.runtimeClasspath
            shouldRunAfter(test)
        }

        val check by project.tasks.getting(Task::class) {
            // Run the functional tests as part of `check`
            dependsOn(functionalTest)
        }
    }
}