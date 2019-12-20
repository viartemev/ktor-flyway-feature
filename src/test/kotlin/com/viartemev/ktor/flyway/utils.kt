package com.viartemev.ktor.flyway

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.testcontainers.containers.JdbcDatabaseContainer
import java.sql.ResultSet
import javax.sql.DataSource

fun getDataSource(container: JdbcDatabaseContainer<*>): DataSource {
    val hikariConfig = HikariConfig().apply {
        jdbcUrl = container.getJdbcUrl()
        username = container.getUsername()
        password = container.getPassword()
    }
    return HikariDataSource(hikariConfig)
}

fun performQuery(container: JdbcDatabaseContainer<*>, sql: String): ResultSet {
    val dataSource = getDataSource(container)
    val statement = dataSource.connection.createStatement()
    statement.execute(sql)
    val resultSet = statement.resultSet
    resultSet.next()
    return resultSet
}