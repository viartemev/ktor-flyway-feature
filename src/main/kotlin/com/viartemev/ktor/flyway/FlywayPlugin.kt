package com.viartemev.ktor.flyway

import io.ktor.server.application.*
import io.ktor.server.config.*
import io.ktor.util.*
import org.flywaydb.core.Flyway
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.sql.DataSource

private val flyWayFeatureLogger: Logger = LoggerFactory.getLogger("com.viartemev.ktor.flyway.FlywayFeature")

class FlywayPlugin(configuration: Configuration) {
    private val dataSource = configuration.dataSource
    private val locations = configuration.locations
    private val schemas = configuration.schemas
    private val commands: Set<FlywayCommand> = configuration.commands

    class Configuration {
        var dataSource: DataSource? = null
        var locations: Array<String>? = null
        var schemas: Array<String>? = null
        internal var commands: Set<FlywayCommand> = setOf(Info, Migrate)
        fun commands(vararg commandsToExecute: FlywayCommand) {
            commands = commandsToExecute.toSet()
        }
    }

    companion object Static : Plugin<Application, Configuration, FlywayPlugin> {
        override val key = AttributeKey<FlywayPlugin>("FlywayPlugin")

        override fun install(pipeline: Application, configure: Configuration.() -> Unit): FlywayPlugin {
            val configuration = Configuration().apply(configure)
            val flywayFeature = FlywayPlugin(configuration)
            if (configuration.dataSource == null) throw ApplicationConfigurationException("DataSource is not configured")

            flyWayFeatureLogger.info("Flyway migration has started")

            val flyway = Flyway
                .configure(pipeline.environment.classLoader)
                .dataSource(flywayFeature.dataSource)
                .also { config -> flywayFeature.locations?.let { config.locations(*it) } }
                .also { config -> flywayFeature.schemas?.let { config.schemas(*it) } }
                .load()

            flywayFeature.commands.map { command ->
                flyWayFeatureLogger.info("Running command: ${command.javaClass.simpleName}")
                command.run(flyway)
            }

            flyWayFeatureLogger.info("Flyway migration has finished")
            return flywayFeature
        }
    }

}