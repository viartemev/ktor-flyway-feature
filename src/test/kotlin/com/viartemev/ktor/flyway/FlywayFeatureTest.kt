package com.viartemev.ktor.flyway

import com.viartemev.ktor.flyway.DbSpec.Companion.getDataSource
import com.viartemev.ktor.flyway.DbSpec.Companion.performQuery
import com.viartemev.ktor.flyway.DbSpec.Companion.postgres
import io.kotlintest.shouldBe
import io.kotlintest.shouldThrow
import io.kotlintest.specs.StringSpec
import io.ktor.application.install
import io.ktor.config.ApplicationConfigurationException
import io.ktor.server.testing.withTestApplication
import io.ktor.util.KtorExperimentalAPI

@KtorExperimentalAPI
class FlywayFeatureTest : StringSpec({

    "Install Flyway feature without datasource should throw exception" {
        shouldThrow<ApplicationConfigurationException> {
            withTestApplication {
                application.install(FlywayFeature)
            }
        }
    }

    "Install Flyway feature with Hikari datasource" {
        withTestApplication {
            application.install(FlywayFeature) {
                dataSource = getDataSource(postgres)
            }
        }
        performQuery(postgres, "select count(*) from test").getLong(1) shouldBe 0
    }

})