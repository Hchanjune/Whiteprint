package org.whiteprint.platform.infra.persistence.mongo.servlet.document

import org.springframework.data.annotation.Transient
import java.io.Serializable

abstract class SoftDeletableMongoDocument<ID : Serializable> : MongoDocument<ID>() {
    @get:Transient
    override val useSoftDelete: Boolean get() = true
}
