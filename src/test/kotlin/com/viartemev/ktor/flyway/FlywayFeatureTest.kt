package com.viartemev.ktor.flyway

import io.ktor.application.*
import io.ktor.config.*
import io.ktor.server.testing.*
import org.junit.Assert.*
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

@Testcontainers
class FlywayFeatureTest {

    @Container
    val postgres: PostgreSQLContainer<*> = PostgreSQLContainer<Nothing>("postgres:9.6.12").apply {
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
                locations = arrayOf("db/migration-custom")
            }
        }
        assertEquals(2, performQuery(postgres, "select count(*) from flyway_schema_history").getLong(1))
    }

    @Test
    fun `Install Flyway feature with Hikari datasource and multiple locations`() {
        withTestApplication {
            application.install(FlywayFeature) {
                dataSource = getDataSource(postgres)
                locations = arrayOf("db/migration-custom", "db/migration-custom2")
            }
        }
        assertEquals(3, performQuery(postgres, "select count(*) from flyway_schema_history").getLong(1))
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

    @Test
    fun `Install Flyway feature with Hikari datasource in custom schema`() {
        withTestApplication {
            application.install(FlywayFeature) {
                dataSource = getDataSource(postgres)
                schemas = arrayOf("CUSTOM_SCHEMA")
            }
        }

        assertTrue(
            performQuery(
                postgres, "SELECT EXISTS (\n" +
                        "   SELECT 1\n" +
                        "   FROM   information_schema.tables \n" +
                        "   WHERE  table_schema = 'CUSTOM_SCHEMA'\n" +
                        "   AND    table_name = 'flyway_schema_history'\n" +
                        "   );\n"
            ).getBoolean(1)
        )

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

        assertEquals(
            2,
            performQuery(postgres, "select count(*) from \"CUSTOM_SCHEMA\".flyway_schema_history").getLong(1)
        )
    }

    @Test
    fun `Install Flyway feature with Hikari datasource in multiple schemas`() {
        withTestApplication {
            application.install(FlywayFeature) {
                dataSource = getDataSource(postgres)
                schemas = arrayOf("CUSTOM_SCHEMA", "ANOTHER_CUSTOM_SCHEMA")
            }
        }

        assertTrue(
            performQuery(
                postgres, "SELECT EXISTS (\n" +
                        "   SELECT 1\n" +
                        "   FROM   information_schema.tables \n" +
                        "   WHERE  table_schema = 'CUSTOM_SCHEMA'\n" +
                        "   AND    table_name = 'flyway_schema_history'\n" +
                        "   );\n"
            ).getBoolean(1)
        )

        assertTrue(
            performQuery(
                postgres, "SELECT EXISTS (\n" +
                        "   SELECT 1\n" +
                        "   FROM   information_schema.schemata \n" +
                        "   WHERE  schema_name = 'ANOTHER_CUSTOM_SCHEMA'\n" +
                        "   );\n"
            ).getBoolean(1)
        )

        assertFalse(
            performQuery(
                postgres, "SELECT EXISTS (\n" +
                        "   SELECT 1\n" +
                        "   FROM   information_schema.tables \n" +
                        "   WHERE  table_schema = 'ANOTHER_CUSTOM_SCHEMA'\n" +
                        "   AND    table_name = 'flyway_schema_history'\n" +
                        "   );\n"
            ).getBoolean(1)
        )

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