package dev.jacomet.gradle.plugins.logging;

public enum LoggingModuleIdentifiers {
    LOG4J_SLF4J_IMPL("org.apache.logging.log4j:log4j-slf4j-impl"),
    LOG4J_TO_SLF4J("org.apache.logging.log4j:log4j-to-slf4j"),
    SLF4J_SIMPLE("org.slf4j:slf4j-simple"),
    LOGBACK_CLASSIC("ch.qos.logback:logback-classic"),
    SLF4J_LOG4J12("org.slf4j:slf4j-log4j12"),
    SLF4J_JCL("org.slf4j:slf4j-jcl"),
    SLF4J_JDK14("org.slf4j:slf4j-jdk14"),
    LOG4J_OVER_SLF4J("org.slf4j:log4j-over-slf4j"),
    LOG4J12API("org.apache.logging.log4j:log4j-1.2-api"),
    LOG4J("log4j:log4j"),
    JUL_TO_SLF4J("org.slf4j:jul-to-slf4j"),
    LOG4J_JUL("org.apache.logging.log4j:log4j-jul"),
    COMMONS_LOGGING("commons-logging:commons-logging"),
    JCL_OVER_SLF4J("org.slf4j:jcl-over-slf4j"),
    LOG4J_JCL("org.apache.logging.log4j:log4j-jcl");

    public final String moduleId;

    LoggingModuleIdentifiers(String moduleId) {
        this.moduleId = moduleId;
    }
}
