package com.viartemev.ktor.flyway

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.kotlintest.specs.BehaviorSpec
import org.testcontainers.containers.JdbcDatabaseContainer
import org.testcontainers.containers.PostgreSQLContainer
import java.sql.ResultSet
import javax.sql.DataSource

abstract class DbSpec(body: DbSpec.() -> Unit = {}) : BehaviorSpec() {
    companion object {
        @JvmStatic
        val postgres: PostgreSQLContainer<*> = PostgreSQLContainer<Nothing>()
            .apply {
                withExposedPorts(5432)
                withDatabaseName("flyway-db")
                withUsername("flyway-user")
                withPassword("12345678")
            }.apply {
                start()
            }

        fun getDataSource(container: JdbcDatabaseContainer<*>): DataSource {
            val hikariConfig = HikariConfig().apply {
                jdbcUrl = postgres.getJdbcUrl()
                username = postgres.getUsername()
                password = postgres.getPassword()
            }
            return HikariDataSource(hikariConfig)
        }

        fun performQuery(container: JdbcDatabaseContainer<*>, sql: String): ResultSet {
            val dataSource = getDataSource(postgres)
            val statement = dataSource.connection.createStatement()
            statement.execute(sql)
            val resultSet = statement.resultSet
            resultSet.next()
            return resultSet
        }
    }

    init {
        this.body()
    }
}