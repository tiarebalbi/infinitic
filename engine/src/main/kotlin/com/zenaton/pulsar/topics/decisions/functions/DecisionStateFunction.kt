package com.zenaton.pulsar.topics.decisions.functions

import com.zenaton.engine.data.decisions.DecisionState
import com.zenaton.engine.topics.decisions.DecisionEngine
import com.zenaton.pulsar.topics.decisions.DecisionDispatcher
import com.zenaton.pulsar.topics.decisions.messages.DecisionMessageContainer
import com.zenaton.pulsar.utils.Logger
import com.zenaton.pulsar.utils.Stater
import org.apache.pulsar.functions.api.Context
import org.apache.pulsar.functions.api.Function

class DecisionStateFunction : Function<DecisionMessageContainer, Void> {

    override fun process(input: DecisionMessageContainer, context: Context?): Void? {
        val ctx = context ?: throw NullPointerException("Null Context received from decisions.StateFunction")

        try {
            val msg = input.msg()

            DecisionEngine(stater = Stater<DecisionState>(ctx), dispatcher = DecisionDispatcher(ctx), logger = Logger(ctx)).handle(msg)
        } catch (e: Exception) {
            Logger(ctx).error("Error:%s for message:%s", e, input)
        }

        return null
    }
}
