package com.viartemev.ktor.flyway

import io.kotlintest.shouldThrow
import io.kotlintest.specs.StringSpec
import io.ktor.application.install
import io.ktor.config.ApplicationConfigurationException
import io.ktor.server.testing.withTestApplication
import io.ktor.util.KtorExperimentalAPI

@KtorExperimentalAPI
class FlywayFeatureTest : StringSpec({

    "Flyway feature without datasource should throw exception" {
        shouldThrow<ApplicationConfigurationException> {
            withTestApplication {
                application.install(FlywayFeature)
            }
        }
    }
})