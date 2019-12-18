package com.viartemev.ktor.flyway

import io.kotlintest.specs.BehaviorSpec
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.PostgreSQLContainer

abstract class DbSpec(body: DbSpec.() -> Unit = {}) : BehaviorSpec() {
    companion object {
        @JvmStatic
        val postgres: GenericContainer<*> = PostgreSQLContainer<Nothing>()
            .apply {
                withDatabaseName("flyway-db")
                withUsername("flyway")
                withPassword("flyway")
                start()
            }
    }

    init {
        this.body()
    }
}