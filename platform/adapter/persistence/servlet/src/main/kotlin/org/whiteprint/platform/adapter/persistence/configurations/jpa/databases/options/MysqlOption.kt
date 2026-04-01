package org.whiteprint.platform.adapter.persistence.configurations.jpa.databases.options

data class MysqlOption(
    var rewriteBatchEnabled: Boolean = false,
    var rewriteBatchedStatements: Boolean = true,
    var cachePreparedStatements: Boolean = true,
    var prepStmtCacheSize: Int = 250,
    var prepStmtCacheSqlLimit: Int = 2048,
    var useServerPrepStmts: Boolean = true
)