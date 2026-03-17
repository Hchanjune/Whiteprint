package com.hc.core.domain.model.contract

import com.hc.core.domain.model.contract.Deletable
import com.hc.core.domain.model.contract.Insertable
import com.hc.core.domain.model.contract.Updatable

interface Auditable: Insertable, Updatable, Deletable