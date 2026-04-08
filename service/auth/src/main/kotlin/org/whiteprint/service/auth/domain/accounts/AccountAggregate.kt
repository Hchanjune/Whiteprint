package org.whiteprint.service.auth.domain.accounts

import org.whiteprint.platform.core.domain.model.aggregate.Aggregate
import java.io.Serializable
import java.time.Instant

class AccountAggregate(
    private val account: Account,
    private val credential: Credential
): Aggregate<Account>() {

    override val schemaVersion: String
        get() = "ALPHA"
    override val id: Serializable
        get() = account.id
    override val root: Account
        get() = account
    override val insertedAt: Instant
        get() = account.insertedAt
    override val updatedAt: Instant
        get() = account.updatedAt
    override val version: Long
        get() = account.version
    override val isDeleted: Boolean
        get() = account.isDeleted
    override val deletedAt: Instant?
        get() = account.deletedAt

}