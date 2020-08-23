package io.infinitic.workflowManager.engine.storages

import io.infinitic.workflowManager.common.avro.AvroConverter
import io.infinitic.workflowManager.common.data.workflows.WorkflowId
import io.infinitic.workflowManager.engine.avroInterfaces.AvroStorage
import io.infinitic.workflowManager.common.states.WorkflowEngineState

class WorkflowEngineStateStorage(val avroStorage: AvroStorage) {

    fun getState(workflowId: WorkflowId): WorkflowEngineState? {
        return avroStorage.getWorkflowEngineState(workflowId.id)?.let { AvroConverter.fromStorage(it) }
    }

    fun updateState(workflowId: WorkflowId, newState: WorkflowEngineState, oldState: WorkflowEngineState?) {
        avroStorage.updateWorkflowEngineState(
            workflowId.id,
            AvroConverter.toStorage(newState),
            oldState?.let { AvroConverter.toStorage(it) }
        )
    }

    fun deleteState(workflowId: WorkflowId) {
        avroStorage.deleteWorkflowEngineState(workflowId.id)
    }
}
