package org.whiteprint.platform.core.persistence.model.contract

import java.io.Serializable

interface Identifiable<ID: Serializable> {
    val id: ID
}