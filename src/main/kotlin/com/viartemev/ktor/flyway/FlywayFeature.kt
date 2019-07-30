package com.viartemev.ktor.flyway

import com.viartemev.ktor.feature.flyway.FlywayCommand
import com.viartemev.ktor.feature.flyway.Info
import com.viartemev.ktor.feature.flyway.Migrate
import io.ktor.application.Application
import io.ktor.application.ApplicationFeature
import io.ktor.application.log
import io.ktor.config.ApplicationConfigurationException
import io.ktor.util.AttributeKey
import io.ktor.util.KtorExperimentalAPI
import org.flywaydb.core.Flyway
import javax.sql.DataSource

@KtorExperimentalAPI
class FlywayFeature(flywayConfiguration: FlywayConfiguration) {
    private val dataSource = flywayConfiguration.dataSource
    private val location = flywayConfiguration.location
    private val commands: List<FlywayCommand> = flywayConfiguration.commands

    class FlywayConfiguration {
        var dataSource: DataSource? = null
        var location: String? = null
        var commands: List<FlywayCommand> = listOf(Info, Migrate)
    }

    companion object Feature : ApplicationFeature<Application, FlywayConfiguration, FlywayFeature> {
        override val key = AttributeKey<FlywayFeature>("Flyway")

        override fun install(pipeline: Application, configure: FlywayConfiguration.() -> Unit): FlywayFeature {
            val configuration = FlywayConfiguration().apply(configure)
            val flywayFeature = FlywayFeature(configuration)
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