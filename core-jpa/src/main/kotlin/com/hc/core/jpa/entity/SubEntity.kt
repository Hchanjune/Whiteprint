package com.hc.core.jpa.entity

import com.hc.core.domain.entity.Identifiable
import com.hc.core.domain.identifier.TsidGenerator
import jakarta.persistence.Column
import jakarta.persistence.Id
import jakarta.persistence.MappedSuperclass

@MappedSuperclass
abstract class SubEntity(
    @Id
    @Column(name = "id", nullable = false)
    override val id: Long = TsidGenerator.generate()
) : Identifiable<Long>