package org.whiteprint.platform.adapter.persistence.configurations.jpa.databases.jdbc

interface JdbcUrlResolver {
    fun resolve(): String
}