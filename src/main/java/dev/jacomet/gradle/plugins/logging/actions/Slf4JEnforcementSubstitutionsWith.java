package dev.jacomet.gradle.plugins.logging.actions;

import dev.jacomet.gradle.plugins.logging.LoggingModuleIdentifiers;
import org.gradle.api.Action;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.component.ComponentSelector;

public class Slf4JEnforcementSubstitutionsWith implements Action<Configuration> {
    @Override
    public void execute(Configuration configuration) {
        configuration.getResolutionStrategy().dependencySubstitution(substitution -> {
            ComponentSelector log4JOverSlf4J = substitution.module(LoggingModuleIdentifiers.LOG4J_OVER_SLF4J.asFirstVersion());
            substitution.substitute(substitution.module(LoggingModuleIdentifiers.LOG4J.moduleId)).with(log4JOverSlf4J);
            substitution.substitute(substitution.module(LoggingModuleIdentifiers.LOG4J12API.moduleId)).with(log4JOverSlf4J);

            substitution.substitute(substitution.module(LoggingModuleIdentifiers.LOG4J_JUL.moduleId)).with(substitution.module(LoggingModuleIdentifiers.JUL_TO_SLF4J.asFirstVersion()));

            ComponentSelector jclOverSlf4J = substitution.module(LoggingModuleIdentifiers.JCL_OVER_SLF4J.asFirstVersion());
            substitution.substitute(substitution.module(LoggingModuleIdentifiers.COMMONS_LOGGING.moduleId)).with(jclOverSlf4J);
            substitution.substitute(substitution.module(LoggingModuleIdentifiers.LOG4J_JCL.moduleId)).with(jclOverSlf4J);
            substitution.substitute(substitution.module(LoggingModuleIdentifiers.SPRING_JCL.moduleId)).with(jclOverSlf4J);
        });
    }
}
