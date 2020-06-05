package com.zenaton.decisionmanager.data

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue
import com.zenaton.commons.data.interfaces.IdInterface
import java.util.UUID

data class DecisionAttemptId
    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    constructor(@get:JsonValue override val id: String = UUID.randomUUID().toString()) :
    IdInterface
