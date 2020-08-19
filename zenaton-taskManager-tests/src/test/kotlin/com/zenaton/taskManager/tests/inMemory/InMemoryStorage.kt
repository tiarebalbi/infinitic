package com.zenaton.taskManager.tests.inMemory

import com.zenaton.taskManager.common.data.Task
import com.zenaton.taskManager.engine.avroInterfaces.AvroStorage
import com.zenaton.taskManager.states.AvroTaskEngineState
import com.zenaton.taskManager.states.AvroMonitoringGlobalState
import com.zenaton.taskManager.states.AvroMonitoringPerNameState

internal class InMemoryStorage : AvroStorage {
    var taskEngineStore: Map<String, AvroTaskEngineState> = mapOf()
    var monitoringPerNameStore: Map<String, AvroMonitoringPerNameState> = mapOf()
    var monitoringGlobalStore: AvroMonitoringGlobalState? = null

    fun isTerminated(task: Task): Boolean {
        return taskEngineStore[task.taskId.id] == null
    }

    fun reset() {
        taskEngineStore = mapOf()
        monitoringPerNameStore = mapOf()
        monitoringGlobalStore = null
    }

    override fun getTaskEngineState(taskId: String): AvroTaskEngineState? {
        return taskEngineStore[taskId]
    }

    override fun updateTaskEngineState(taskId: String, newState: AvroTaskEngineState, oldState: AvroTaskEngineState?) {
        taskEngineStore = taskEngineStore.plus(taskId to newState)
    }

    override fun deleteTaskEngineState(taskId: String) {
        taskEngineStore = taskEngineStore.minus(taskId)
    }

    override fun getMonitoringPerNameState(taskName: String): AvroMonitoringPerNameState? = monitoringPerNameStore[taskName]

    override fun updateMonitoringPerNameState(taskName: String, newState: AvroMonitoringPerNameState, oldState: AvroMonitoringPerNameState?) {
        monitoringPerNameStore = monitoringPerNameStore.plus(taskName to newState)
    }

    override fun deleteMonitoringPerNameState(taskName: String) {
        monitoringPerNameStore = monitoringPerNameStore.minus(taskName)
    }

    override fun getMonitoringGlobalState(): AvroMonitoringGlobalState? = monitoringGlobalStore

    override fun updateMonitoringGlobalState(newState: AvroMonitoringGlobalState, oldState: AvroMonitoringGlobalState?) {
        monitoringGlobalStore = newState
    }

    override fun deleteMonitoringGlobalState() {
        monitoringGlobalStore = null
    }
}