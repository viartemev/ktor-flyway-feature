package com.viartemev.ktor.flyway

import com.viartemev.ktor.feature.flyway.FlywayCommand
import io.ktor.application.Application
import io.ktor.application.ApplicationFeature
import io.ktor.application.log
import io.ktor.config.ApplicationConfigurationException
import io.ktor.util.AttributeKey
import io.ktor.util.KtorExperimentalAPI
import org.flywaydb.core.Flyway
import javax.sql.DataSource

@KtorExperimentalAPI
class Flyway(configuration: Configuration) {
    private val dataSource = configuration.dataSource
    private val location = configuration.location
    private val commands: List<FlywayCommand> = configuration.commands

    class Configuration {
        var dataSource: DataSource? = null
        var location: String? = null
        internal val commands = mutableListOf<FlywayCommand>()
        fun commands(vararg commandsToExecute: FlywayCommand) = commands.addAll(commandsToExecute)
    }

    companion object Feature : ApplicationFeature<Application, Configuration, com.viartemev.ktor.flyway.Flyway> {
        override val key = AttributeKey<com.viartemev.ktor.flyway.Flyway>("Flyway")

        override fun install(pipeline: Application, configure: Configuration.() -> Unit): com.viartemev.ktor.flyway.Flyway {
            val configuration = Configuration().apply(configure)
            val flywayFeature = Flyway(configuration)
            if (configuration.dataSource == null) throw ApplicationConfigurationException("DataSource is not configured")

            pipeline.log.info("Flyway migration has started")

            val flyway = Flyway
                    .configure(pipeline.environment.classLoader)
                    .dataSource(flywayFeature.dataSource)
                    .also { config -> flywayFeature.location?.let { config.locations(it) } }
                    .load()

            flywayFeature.commands.map { command ->
                pipeline.log.info("Running command: ${command.javaClass.simpleName}")
                command.run(flyway)
            }

            pipeline.log.info("Flyway migration has finished")
            return flywayFeature
        }
    }

}