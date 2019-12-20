package com.viartemev.ktor.flyway

import io.ktor.application.install
import io.ktor.config.ApplicationConfigurationException
import io.ktor.server.testing.withTestApplication
import io.ktor.util.KtorExperimentalAPI
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import kotlin.test.assertEquals
import kotlin.test.assertFalse

@Testcontainers
@KtorExperimentalAPI
class FlywayFeatureTest {

    @Container
    val postgres: PostgreSQLContainer<*> = PostgreSQLContainer<Nothing>().apply {
        withDatabaseName("flyway")
        withUsername("flyway")
        withPassword("12345")
    }

    @Test
    fun `Install Flyway feature without datasource should throw exception`() {
        Assertions.assertThrows(ApplicationConfigurationException::class.java) {
            withTestApplication {
                application.install(FlywayFeature)
            }
        }
    }

    @Test
    fun `Install Flyway feature with Hikari datasource`() {
        withTestApplication {
            application.install(FlywayFeature) {
                dataSource = getDataSource(postgres)
            }
        }
        assertEquals(1, performQuery(postgres, "select count(*) from flyway_schema_history").getLong(1))
    }

    @Test
    fun `Install Flyway feature with Hikari datasource and overridden location`() {
        withTestApplication {
            application.install(FlywayFeature) {
                dataSource = getDataSource(postgres)
                location = "db/migration-custom"
            }
        }
        assertEquals(2, performQuery(postgres, "select count(*) from flyway_schema_history").getLong(1))
    }

    @Test
    fun `Install Flyway feature with Hikari datasource and overridden commands`() {
        withTestApplication {
            application.install(FlywayFeature) {
                dataSource = getDataSource(postgres)
                commands(Info)
            }
        }
        assertFalse(
            performQuery(
                postgres, "SELECT EXISTS (\n" +
                        "   SELECT 1\n" +
                        "   FROM   information_schema.tables \n" +
                        "   WHERE  table_schema = 'public'\n" +
                        "   AND    table_name = 'flyway_schema_history'\n" +
                        "   );\n"
            ).getBoolean(1)
        )
    }
}