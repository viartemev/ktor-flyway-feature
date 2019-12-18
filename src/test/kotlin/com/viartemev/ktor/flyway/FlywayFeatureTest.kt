package com.viartemev.ktor.flyway

import com.viartemev.ktor.flyway.DbSpec.Companion.postgres
import io.kotlintest.shouldThrow
import io.kotlintest.specs.StringSpec
import io.ktor.application.install
import io.ktor.config.ApplicationConfigurationException
import io.ktor.server.testing.withTestApplication
import io.ktor.util.KtorExperimentalAPI
import org.junit.jupiter.api.Assertions.assertTrue

@KtorExperimentalAPI
class FlywayFeatureTest : StringSpec({

    "Flyway feature without datasource should throw exception" {
        shouldThrow<ApplicationConfigurationException> {
            withTestApplication {
                application.install(FlywayFeature)
            }
        }
    }

    "Postgres is running" {
        assertTrue(postgres.isRunning());
    }

})