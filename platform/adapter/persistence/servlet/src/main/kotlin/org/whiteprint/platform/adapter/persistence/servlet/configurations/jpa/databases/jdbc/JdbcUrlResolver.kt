package org.whiteprint.platform.adapter.persistence.servlet.configurations.jpa.databases.jdbc

interface JdbcUrlResolver {
    fun resolve(): String
}