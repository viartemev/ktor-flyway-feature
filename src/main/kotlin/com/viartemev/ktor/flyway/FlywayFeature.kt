package com.viartemev.ktor.flyway

import io.ktor.application.Application
import io.ktor.application.ApplicationFeature
import io.ktor.config.ApplicationConfigurationException
import io.ktor.util.AttributeKey
import io.ktor.util.KtorExperimentalAPI
import org.flywaydb.core.Flyway
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.sql.DataSource

private val flyWayFeatureLogger: Logger = LoggerFactory.getLogger("com.viartemev.ktor.flyway.FlywayFeature")

@KtorExperimentalAPI
class FlywayFeature(configuration: Configuration) {
    private val dataSource = configuration.dataSource
    private val location = configuration.location
    private val commands: Set<FlywayCommand> = configuration.commands

    class Configuration {
        var dataSource: DataSource? = null
        var location: String? = null
        internal var commands: Set<FlywayCommand> = setOf(Info, Migrate)
        fun commands(vararg commandsToExecute: FlywayCommand) {
            commands = commandsToExecute.toSet()
        }
    }

    companion object Feature : ApplicationFeature<Application, Configuration, FlywayFeature> {
        override val key = AttributeKey<FlywayFeature>("FlywayFeature")

        override fun install(pipeline: Application, configure: Configuration.() -> Unit): FlywayFeature {
            val configuration = Configuration().apply(configure)
            val flywayFeature = FlywayFeature(configuration)
            if (configuration.dataSource == null) throw ApplicationConfigurationException("DataSource is not configured")

            flyWayFeatureLogger.info("Flyway migration has started")

            val flyway = Flyway
                .configure(pipeline.environment.classLoader)
                .dataSource(flywayFeature.dataSource)
                .also { config -> flywayFeature.location?.let { config.locations(it) } }
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