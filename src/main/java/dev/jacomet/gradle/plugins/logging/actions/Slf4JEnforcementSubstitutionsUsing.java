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
package dev.jacomet.gradle.plugins.logging.actions;

import dev.jacomet.gradle.plugins.logging.LoggingModuleIdentifiers;
import org.gradle.api.Action;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.component.ComponentSelector;

public class Slf4JEnforcementSubstitutionsUsing implements Action<Configuration> {
    @Override
    public void execute(Configuration configuration) {
        configuration.getResolutionStrategy().dependencySubstitution(substitution -> {
            ComponentSelector log4JOverSlf4J = substitution.module(LoggingModuleIdentifiers.LOG4J_OVER_SLF4J.asFirstVersion());
            substitution.substitute(substitution.module(LoggingModuleIdentifiers.LOG4J.moduleId)).using(log4JOverSlf4J);
            substitution.substitute(substitution.module(LoggingModuleIdentifiers.LOG4J12API.moduleId)).using(log4JOverSlf4J);

            substitution.substitute(substitution.module(LoggingModuleIdentifiers.LOG4J_JUL.moduleId)).using(substitution.module(LoggingModuleIdentifiers.JUL_TO_SLF4J.asFirstVersion()));

            ComponentSelector jclOverSlf4J = substitution.module(LoggingModuleIdentifiers.JCL_OVER_SLF4J.asFirstVersion());
            substitution.substitute(substitution.module(LoggingModuleIdentifiers.COMMONS_LOGGING.moduleId)).using(jclOverSlf4J);
            substitution.substitute(substitution.module(LoggingModuleIdentifiers.LOG4J_JCL.moduleId)).using(jclOverSlf4J);
        });
    }
}
