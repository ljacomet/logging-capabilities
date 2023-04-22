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
            substitution.substitute(substitution.module(LoggingModuleIdentifiers.SPRING_JCL.moduleId)).using(jclOverSlf4J);
        });
    }
}
