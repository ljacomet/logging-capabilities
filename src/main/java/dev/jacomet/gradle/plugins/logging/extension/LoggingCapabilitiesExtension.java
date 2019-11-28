package dev.jacomet.gradle.plugins.logging.extension;

import dev.jacomet.gradle.plugins.logging.LoggingModuleIdentifiers;
import dev.jacomet.gradle.plugins.logging.rules.CommonsLoggingImplementationRule;
import dev.jacomet.gradle.plugins.logging.rules.Log4J2vsSlf4J;
import dev.jacomet.gradle.plugins.logging.rules.Slf4JImplementation;
import dev.jacomet.gradle.plugins.logging.rules.Slf4JVsJCL;
import dev.jacomet.gradle.plugins.logging.rules.Slf4JVsLog4J2ForJCL;
import dev.jacomet.gradle.plugins.logging.rules.Slf4JvsJUL;
import dev.jacomet.gradle.plugins.logging.rules.Slf4JvsLog4J;
import dev.jacomet.gradle.plugins.logging.rules.Slf4JvsLog4J2ForJUL;
import dev.jacomet.gradle.plugins.logging.rules.Slf4JvsLog4J2ForLog4J;
import org.gradle.api.Action;
import org.gradle.api.artifacts.CapabilitiesResolution;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.ExternalDependency;
import org.gradle.api.artifacts.dsl.DependencyHandler;

/**
 * Project extension that enables expressing preference over potential logging capabilities conflicts.
 */
public class LoggingCapabilitiesExtension {
    private final ConfigurationContainer configurations;
    private final DependencyHandler dependencies;

    public LoggingCapabilitiesExtension(ConfigurationContainer configurations, DependencyHandler dependencies) {
        this.configurations = configurations;
        this.dependencies = dependencies;
    }

    /**
     * Selects the provided module as the Slf4J binding to use.
     * <p>
     * This also resolves all other potential conflicts with the passed in module in favor of it.
     *
     * @param dependencyNotation the Slf4J binding module as a dependency or {@code group:name:version} notation
     */
    public void selectSlf4JBinding(Object dependencyNotation) {
        ExternalDependency dependency = validateNotation(dependencyNotation);
        String because = "Logging capabilities plugin selected Slf4J binding";
        if (LoggingModuleIdentifiers.SLF4J_LOG4J12.matches(dependency)) {
            selectCapabilityConflict(Slf4JvsLog4J.CAPABILITY_ID, dependencyNotation, because);
            selectCapabilityConflict(Slf4JImplementation.CAPABILITY_ID, dependencyNotation, because);
        } else if (LoggingModuleIdentifiers.SLF4J_JDK14.matches(dependency)) {
            selectCapabilityConflict(Slf4JvsJUL.CAPABILITY_ID, dependencyNotation, because);
            selectCapabilityConflict(Slf4JImplementation.CAPABILITY_ID, dependencyNotation, because);
        } else if (LoggingModuleIdentifiers.SLF4J_JCL.matches(dependency)) {
            selectCapabilityConflict(Slf4JVsJCL.CAPABILITY_ID, dependencyNotation, because);
            selectCapabilityConflict(Slf4JImplementation.CAPABILITY_ID, dependencyNotation, because);
        } else if (LoggingModuleIdentifiers.LOG4J_SLF4J_IMPL.matches(dependency)) {
            selectCapabilityConflict(Log4J2vsSlf4J.CAPABILITY_ID, dependencyNotation, because);
            selectCapabilityConflict(Slf4JImplementation.CAPABILITY_ID, dependencyNotation, because);
        } else if (LoggingModuleIdentifiers.LOGBACK_CLASSIC.matches(dependency) || LoggingModuleIdentifiers.SLF4J_SIMPLE.matches(dependency)) {
            selectCapabilityConflict(Slf4JImplementation.CAPABILITY_ID, dependencyNotation, because);
        } else {
            throw new IllegalArgumentException("Provided dependency '" + dependency + "' is not a valid Slf4J binding");
        }
    }

    /**
     * Selects the provided module as the Log4J 1.2 implementation to use.
     * <p>
     * This also resolves all other potential conflicts with the passed in module in favor of it.
     *
     * @param dependencyNotation the Log4J 1.2 implementation module as a dependency or {@code group:name:version} notation
     */
    public void selectLog4JImplementation(Object dependencyNotation) {
        ExternalDependency dependency = validateNotation(dependencyNotation);
        String because = "Logging capabilities plugin selected Log4J implementation";
        if (LoggingModuleIdentifiers.LOG4J_OVER_SLF4J.matches(dependency)) {
            selectCapabilityConflict(Slf4JvsLog4J2ForLog4J.CAPABILITY_ID, dependencyNotation, because);
            selectCapabilityConflict(Slf4JvsLog4J.CAPABILITY_ID, dependencyNotation, because);
        } else if (LoggingModuleIdentifiers.LOG4J12API.matches(dependency)) {
            selectCapabilityConflict(Slf4JvsLog4J2ForLog4J.CAPABILITY_ID, dependencyNotation, because);
        } else if (LoggingModuleIdentifiers.LOG4J.matches(dependency)) {
            selectCapabilityConflict(Slf4JvsLog4J2ForLog4J.CAPABILITY_ID, dependencyNotation, because);
        } else if (LoggingModuleIdentifiers.SLF4J_LOG4J12.matches(dependency)) {
            selectCapabilityConflict(Slf4JvsLog4J.CAPABILITY_ID, dependencyNotation, because);
        } else {
            throw new IllegalArgumentException("Provided dependency '" + dependency + "' is not a valid Log4J implementation");
        }
    }

    /**
     * Selects the provided module as the java util logging delegation to use.
     * <p>
     * This also resolves all other potential conflicts with the passed in module in favor of it.
     *
     * @param dependencyNotation the JUL delegation module as a dependency or {@code group:name:version} notation
     */
    public void selectJulDelegation(Object dependencyNotation) {
        ExternalDependency dependency = validateNotation(dependencyNotation);
        String because = "Logging capabilities plugin selected JUL delegation";
        if (LoggingModuleIdentifiers.JUL_TO_SLF4J.matches(dependency)) {
            selectCapabilityConflict(Slf4JvsLog4J2ForJUL.CAPABILITY_ID, dependencyNotation, because);
            selectCapabilityConflict(Slf4JvsJUL.CAPABILITY_ID, dependencyNotation, because);
        } else if (LoggingModuleIdentifiers.SLF4J_JDK14.matches(dependency)) {
            selectCapabilityConflict(Slf4JvsJUL.CAPABILITY_ID, dependencyNotation, because);
        } else if (LoggingModuleIdentifiers.LOG4J_JUL.matches(dependency)) {
            selectCapabilityConflict(Slf4JvsLog4J2ForJUL.CAPABILITY_ID, dependencyNotation, because);
        } else {
            throw new IllegalArgumentException("Provided dependency '" + dependency + "' is not a valid JUL delegation");
        }
    }

    /**
     * Selects the provided module as the commons-logging implementation to use.
     * <p>
     * This also resolves all other potential conflicts with the passed in module in favor of it.
     *
     * @param dependencyNotation the commons-logging implementation module as a dependency or {@code group:name:version} notation
     */
    public void selectJCLImplementation(Object dependencyNotation) {
        ExternalDependency dependency = validateNotation(dependencyNotation);
        String because = "Logging capabilities plugin selected JCL implementation";
        if (LoggingModuleIdentifiers.JCL_OVER_SLF4J.matches(dependency)) {
            selectCapabilityConflict(CommonsLoggingImplementationRule.CAPABILITY_ID, dependencyNotation, because);
            selectCapabilityConflict(Slf4JVsJCL.CAPABILITY_ID, dependencyNotation, because);
            selectCapabilityConflict(Slf4JVsLog4J2ForJCL.CAPABILITY_ID, dependencyNotation, because);
        } else if (LoggingModuleIdentifiers.COMMONS_LOGGING.matches(dependency)) {
            selectCapabilityConflict(CommonsLoggingImplementationRule.CAPABILITY_ID, dependencyNotation, because);
        } else if (LoggingModuleIdentifiers.SLF4J_JCL.matches(dependency)) {
            selectCapabilityConflict(Slf4JVsJCL.CAPABILITY_ID, dependencyNotation, because);
        } else if (LoggingModuleIdentifiers.LOG4J_JCL.matches(dependency)) {
            selectCapabilityConflict(Slf4JVsLog4J2ForJCL.CAPABILITY_ID, dependencyNotation, because);
        } else {
            throw new IllegalArgumentException("Provided dependency '" + dependency + "' is not a valid JCL implementation");
        }
    }

    /**
     * Selects the provided module as the Slf4J / Log4J 2 interaction to use.
     * <p>
     * This also resolves all other potential conflicts with the passed in module in favor of it.
     *
     * @param dependencyNotation the Slf4J / Log4J 2 interaction module as a dependency or {@code group:name:version} notation
     */
    public void selectSlf4JLog4J2Interaction(Object dependencyNotation) {
        ExternalDependency dependency = validateNotation(dependencyNotation);
        String because = "Logging capabilities plugin selected Slf4J Log4J 2 interaction";
        if (LoggingModuleIdentifiers.LOG4J_TO_SLF4J.matches(dependency)) {
            selectCapabilityConflict(Log4J2vsSlf4J.CAPABILITY_ID, dependencyNotation, because);
        } else if (LoggingModuleIdentifiers.LOG4J_SLF4J_IMPL.matches(dependency)) {
            selectCapabilityConflict(Log4J2vsSlf4J.CAPABILITY_ID, dependencyNotation, because);
            selectCapabilityConflict(Slf4JImplementation.CAPABILITY_ID, dependencyNotation, because);
        } else {
            throw new IllegalArgumentException("Provided dependency '" + dependency + "' is not a valid Slf4J / Log4J 2 interaction");
        }
    }

    private ExternalDependency validateNotation(Object dependencyNotation) {
        Dependency dependency = dependencies.create(dependencyNotation);
        if (dependency instanceof ExternalDependency) {
            return (ExternalDependency) dependency;
        } else {
            throw new IllegalArgumentException("Provided notation '" + dependencyNotation + "' cannot be converted to an ExternalDependency");
        }
    }

    private void selectCapabilityConflict(Configuration configuration, String capabilityId, Object identifierNotation, String because) {
        configuration.getResolutionStrategy().capabilitiesResolution(getCapabilitiesResolutionAction(capabilityId, identifierNotation, because));
    }

    private void selectCapabilityConflict(String capabilityId, Object identifierNotation, String because) {
        configurations.all(conf -> conf.getResolutionStrategy().capabilitiesResolution(getCapabilitiesResolutionAction(capabilityId, identifierNotation, because)));
    }

    private Action<CapabilitiesResolution> getCapabilitiesResolutionAction(String capabilityId, Object identifierNotation, String because) {
        return resolution -> resolution.withCapability(capabilityId, details -> details.select(identifierNotation).because(because));
    }

}
