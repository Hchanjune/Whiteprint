package com.hc.core.domain.model.contract

import java.io.Serializable

interface Identifiable<ID: Serializable> {
    val id: ID
}