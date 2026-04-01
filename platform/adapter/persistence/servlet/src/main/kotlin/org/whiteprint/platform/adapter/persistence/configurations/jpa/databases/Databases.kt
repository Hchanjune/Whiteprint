package org.whiteprint.platform.adapter.persistence.configurations.jpa.databases

enum class Databases(
    val dialect: String
) {
    POSTGRESQL("org.hibernate.dialect.PostgreSQLDialect"),
    MYSQL("org.hibernate.dialect.MySQLDialect"),
    MARIADB("org.hibernate.dialect.MariaDBDialect"),
    SQLSERVER("org.hibernate.dialect.SQLServerDialect"),
    ORACLE("org.hibernate.dialect.OracleDialect"),
    H2("org.hibernate.dialect.H2Dialect");

    fun toDialect(): String = dialect
}