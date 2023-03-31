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
package dev.jacomet.gradle.plugins.logging.rules

import org.gradle.api.Action
import org.gradle.api.artifacts.ComponentMetadataContext
import org.gradle.api.artifacts.ComponentMetadataDetails
import org.gradle.api.artifacts.ModuleVersionIdentifier
import org.gradle.api.artifacts.VariantMetadata
import org.gradle.api.capabilities.MutableCapabilitiesMetadata
import spock.lang.Specification

class LoggingCapabilitiesRulesTest extends Specification {

    def "versioned capability rule adds capability with module version"() {
        given:
        def name = "testName"
        def version = "2.2"
        def rule = new MyVersionedCapabilityRule(name)
        def context = Mock(ComponentMetadataContext)
        def details = Mock(ComponentMetadataDetails)
        def identifier = Mock(ModuleVersionIdentifier)
        def variant = Mock(VariantMetadata)
        def capabilities = Mock(MutableCapabilitiesMetadata)

        when:
        rule.execute(context)

        then:
        1 * context.details >> details
        1 * details.id >> identifier
        1 * identifier.version >> version
        1 * details.allVariants(_) >> { Action action -> action.execute(variant) }
        1 * variant.withCapabilities(_) >> { Action action -> action.execute(capabilities) }
        1 * capabilities.addCapability("dev.jacomet.logging", "testName", "2.2")
    }

    def "fixed capability rule adds capability with version 1.0"() {
        given:
        def name = "testName"
        def rule = new MyFixedCapabilityRule(name)
        def context = Mock(ComponentMetadataContext)
        def details = Mock(ComponentMetadataDetails)
        def variant = Mock(VariantMetadata)
        def capabilities = Mock(MutableCapabilitiesMetadata)

        when:
        rule.execute(context)

        then:
        1 * context.details >> details
        1 * details.allVariants(_) >> { Action action -> action.execute(variant) }
        1 * variant.withCapabilities(_) >> { Action action -> action.execute(capabilities) }
        1 * capabilities.addCapability("dev.jacomet.logging", "testName", "1.0")
    }

    static class MyVersionedCapabilityRule extends VersionedCapabilityRule {
        MyVersionedCapabilityRule(String name) {
            super(name)
        }
    }

    static class MyFixedCapabilityRule extends FixedCapabilityRule {
        MyFixedCapabilityRule(String name) {
            super(name)
        }
    }
}
