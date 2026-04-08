package org.whiteprint.service.auth.application.port.out

import org.whiteprint.platform.core.domain.repository.AggregateRepository
import org.whiteprint.service.auth.domain.accounts.aggregate.AccountAggregate

interface AccountRepository: AggregateRepository<Long, AccountAggregate> {



}