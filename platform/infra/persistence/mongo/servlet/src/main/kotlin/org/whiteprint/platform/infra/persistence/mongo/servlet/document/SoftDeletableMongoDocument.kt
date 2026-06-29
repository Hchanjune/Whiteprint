package org.whiteprint.platform.infra.persistence.mongo.servlet.document

import java.io.Serializable

abstract class SoftDeletableMongoDocument<ID : Serializable> : MongoDocument<ID>() {
    override val useSoftDelete: Boolean = true
}
