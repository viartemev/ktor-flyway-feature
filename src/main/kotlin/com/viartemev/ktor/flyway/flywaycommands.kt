package com.viartemev.ktor.feature.flyway

import org.flywaydb.core.Flyway

sealed class FlywayCommand {
    abstract fun run(flyway: Flyway)
}

object Migrate : FlywayCommand() {
    override fun run(flyway: Flyway) {
        flyway.migrate()
    }
}

object Clean : FlywayCommand() {
    override fun run(flyway: Flyway) {
        flyway.clean()
    }
}

object Info : FlywayCommand() {
    override fun run(flyway: Flyway) {
        flyway.info()
    }
}

object Validate : FlywayCommand() {
    override fun run(flyway: Flyway) {
        flyway.validate()
    }
}

object Baseline : FlywayCommand() {
    override fun run(flyway: Flyway) {
        flyway.baseline()
    }
}

object Repair : FlywayCommand() {
    override fun run(flyway: Flyway) {
        flyway.repair()
    }
}