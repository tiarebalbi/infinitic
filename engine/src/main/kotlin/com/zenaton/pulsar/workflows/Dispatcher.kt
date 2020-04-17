package com.zenaton.pulsar.workflows

import com.zenaton.engine.decisions.Message.DecisionDispatched
import com.zenaton.engine.tasks.Message.TaskDispatched
import com.zenaton.engine.workflows.DispatcherInterface
import com.zenaton.engine.workflows.WorkflowState
import com.zenaton.pulsar.serializer.MessageSerDe
import com.zenaton.pulsar.serializer.MessageSerDeInterface
import org.apache.pulsar.functions.api.Context

class Dispatcher(private val context: Context, private val serde: MessageSerDeInterface = MessageSerDe) : DispatcherInterface {

    override fun getState(key: String): WorkflowState? {
        return context.getState(key) ?. let { serde.deSerializeState(it) }
    }

    override fun updateState(state: WorkflowState) {
        context.putState(state.workflowId.id, serde.serializeState(state))
    }

    override fun deleteState(state: WorkflowState) {
        context.deleteState(state.workflowId.id)
    }

    override fun dispatchTask(msg: TaskDispatched) {

        TODO("Not yet implemented")
    }

    override fun dispatchDecision(msg: DecisionDispatched) {
        TODO("Not yet implemented")
    }

    override fun log() {
        TODO("Not yet implemented")
    }
}
