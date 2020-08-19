package com.zenaton.taskManager.tests.inMemory

import com.zenaton.taskManager.client.AvroDispatcher as AvroClientDispatcher
import com.zenaton.taskManager.engine.avroInterfaces.AvroDispatcher as AvroEngineDispatcher
import com.zenaton.taskManager.messages.envelopes.AvroEnvelopeForTaskEngine
import com.zenaton.taskManager.messages.envelopes.AvroEnvelopeForMonitoringGlobal
import com.zenaton.taskManager.messages.envelopes.AvroEnvelopeForMonitoringPerName
import com.zenaton.taskManager.messages.envelopes.AvroEnvelopeForWorker
import com.zenaton.taskManager.worker.AvroDispatcher as AvroWorkerDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

internal class InMemoryDispatcher : AvroWorkerDispatcher, AvroEngineDispatcher, AvroClientDispatcher {
    // Here we favor lambda to avoid a direct dependency with engines instances
    lateinit var taskEngineHandle: (msg: AvroEnvelopeForTaskEngine) -> Unit
    lateinit var monitoringPerNameHandle: (msg: AvroEnvelopeForMonitoringPerName) -> Unit
    lateinit var monitoringGlobalHandle: (msg: AvroEnvelopeForMonitoringGlobal) -> Unit
    lateinit var workerHandle: (msg: AvroEnvelopeForWorker) -> Unit
    lateinit var scope: CoroutineScope

    override fun toTaskEngine(msg: AvroEnvelopeForTaskEngine, after: Float) {
        scope.launch {
            if (after > 0F) {
                delay((1000 * after).toLong())
            }
            taskEngineHandle(msg)
        }
    }

    override fun toTaskEngine(msg: AvroEnvelopeForTaskEngine) = toTaskEngine(msg, 0F)

    override fun toMonitoringPerName(msg: AvroEnvelopeForMonitoringPerName) {
        scope.launch {
            monitoringPerNameHandle(msg)
        }
    }

    override fun toMonitoringGlobal(msg: AvroEnvelopeForMonitoringGlobal) {
        scope.launch {
            monitoringGlobalHandle(msg)
        }
    }

    override fun toWorkers(msg: AvroEnvelopeForWorker) {
        scope.launch {
            workerHandle(msg)
        }
    }
}