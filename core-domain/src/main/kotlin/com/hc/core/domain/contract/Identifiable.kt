package com.hc.core.domain.contract

import java.io.Serializable

interface Identifiable<ID: Serializable> {
    val id: ID
}