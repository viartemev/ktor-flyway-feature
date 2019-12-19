package com.viartemev.ktor.flyway

import com.viartemev.ktor.flyway.DbSpec.Companion.postgres
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
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

    "Flyway feature with datasource" {
        val hikariConfig = HikariConfig().apply {
            jdbcUrl = postgres.getJdbcUrl()
            username = postgres.getUsername()
            password = postgres.getPassword()
        }
        val hikariDataSource = HikariDataSource(hikariConfig)

        withTestApplication {
            application.install(FlywayFeature) {
                dataSource = hikariDataSource
            }
        }
    }

})