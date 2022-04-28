package com.viartemev.ktor.flyway

import io.ktor.server.config.*
import io.ktor.server.testing.*
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

@Testcontainers
class FlywayFeatureTest {

    @Container
    val postgres: PostgreSQLContainer<*> = PostgreSQLContainer<Nothing>("postgres:13.4").apply {
        withDatabaseName("flyway")
        withUsername("flyway")
        withPassword("12345")
    }

    @Test
    fun `Install Flyway feature without datasource should throw exception`() {
        Assertions.assertThrows(ApplicationConfigurationException::class.java) {
            testApplication {
                install(FlywayFeature)
            }
        }
    }

    @Test
    fun `Install Flyway feature with Hikari datasource`() {
        testApplication {
            install(FlywayFeature) {
                dataSource = getDataSource(postgres)
            }
        }
        Assertions.assertEquals(1, performQuery(postgres, "select count(*) from flyway_schema_history").getLong(1))
    }

    @Test
    fun `Install Flyway feature with Hikari datasource and overridden location`() {
        testApplication {
            install(FlywayFeature) {
                dataSource = getDataSource(postgres)
                locations = arrayOf("db/migration-custom")
            }
        }
        Assertions.assertEquals(2, performQuery(postgres, "select count(*) from flyway_schema_history").getLong(1))
    }

    @Test
    fun `Install Flyway feature with Hikari datasource and multiple locations`() {
        testApplication {
            install(FlywayFeature) {
                dataSource = getDataSource(postgres)
                locations = arrayOf("db/migration-custom", "db/migration-custom2")
            }
        }
        Assertions.assertEquals(3, performQuery(postgres, "select count(*) from flyway_schema_history").getLong(1))
    }

    @Test
    fun `Install Flyway feature with Hikari datasource and overridden commands`() {
        testApplication {
            install(FlywayFeature) {
                dataSource = getDataSource(postgres)
                commands(Info)
            }
        }
        Assertions.assertFalse(
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
        testApplication {
            install(FlywayFeature) {
                dataSource = getDataSource(postgres)
                schemas = arrayOf("CUSTOM_SCHEMA")
            }
        }

        Assertions.assertTrue(
            performQuery(
                postgres, "SELECT EXISTS (\n" +
                    "   SELECT 1\n" +
                    "   FROM   information_schema.tables \n" +
                    "   WHERE  table_schema = 'CUSTOM_SCHEMA'\n" +
                    "   AND    table_name = 'flyway_schema_history'\n" +
                    "   );\n"
            ).getBoolean(1)
        )

        Assertions.assertFalse(
            performQuery(
                postgres, "SELECT EXISTS (\n" +
                    "   SELECT 1\n" +
                    "   FROM   information_schema.tables \n" +
                    "   WHERE  table_schema = 'public'\n" +
                    "   AND    table_name = 'flyway_schema_history'\n" +
                    "   );\n"
            ).getBoolean(1)
        )

        Assertions.assertEquals(
            2,
            performQuery(postgres, "select count(*) from \"CUSTOM_SCHEMA\".flyway_schema_history").getLong(1)
        )
    }

    @Test
    fun `Install Flyway feature with Hikari datasource in multiple schemas`() {
        testApplication {
            install(FlywayFeature) {
                dataSource = getDataSource(postgres)
                schemas = arrayOf("CUSTOM_SCHEMA", "ANOTHER_CUSTOM_SCHEMA")
            }
        }

        Assertions.assertTrue(
            performQuery(
                postgres, "SELECT EXISTS (\n" +
                    "   SELECT 1\n" +
                    "   FROM   information_schema.tables \n" +
                    "   WHERE  table_schema = 'CUSTOM_SCHEMA'\n" +
                    "   AND    table_name = 'flyway_schema_history'\n" +
                    "   );\n"
            ).getBoolean(1)
        )

        Assertions.assertTrue(
            performQuery(
                postgres, "SELECT EXISTS (\n" +
                    "   SELECT 1\n" +
                    "   FROM   information_schema.schemata \n" +
                    "   WHERE  schema_name = 'ANOTHER_CUSTOM_SCHEMA'\n" +
                    "   );\n"
            ).getBoolean(1)
        )

        Assertions.assertFalse(
            performQuery(
                postgres, "SELECT EXISTS (\n" +
                    "   SELECT 1\n" +
                    "   FROM   information_schema.tables \n" +
                    "   WHERE  table_schema = 'ANOTHER_CUSTOM_SCHEMA'\n" +
                    "   AND    table_name = 'flyway_schema_history'\n" +
                    "   );\n"
            ).getBoolean(1)
        )

        Assertions.assertFalse(
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