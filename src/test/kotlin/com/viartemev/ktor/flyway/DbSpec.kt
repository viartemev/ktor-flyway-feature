package com.viartemev.ktor.flyway

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.kotlintest.specs.BehaviorSpec
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.PostgreSQLContainer

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
    }

    init {
        this.body()
    }
}