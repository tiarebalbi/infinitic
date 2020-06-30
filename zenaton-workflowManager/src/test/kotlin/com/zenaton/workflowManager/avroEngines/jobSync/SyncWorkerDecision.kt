package com.zenaton.workflowManager.avroEngines.jobSync

import com.zenaton.jobManager.avroConverter.AvroConverter
import com.zenaton.jobManager.avroInterfaces.AvroDispatcher
import com.zenaton.jobManager.messages.AvroRunJob
import com.zenaton.jobManager.messages.envelopes.AvroEnvelopeForWorker
import com.zenaton.workflowManager.avroEngines.jobSync.SyncWorker.Status

internal class SyncWorkerDecision : SyncWorker {
    override lateinit var avroDispatcher: AvroDispatcher
    lateinit var behavior: (msg: AvroRunJob) -> Status?
    var workflowA = WorkflowA()

    class WorkflowA { fun handle() {} }

    override fun handle(msg: AvroEnvelopeForWorker) {
        when (val avro = AvroConverter.removeEnvelopeFromWorkerMessage(msg)) {
            is AvroRunJob -> {
                sendJobStarted(avro)
                val out = when (avro.jobName) {
                    "WorkflowA" -> workflowA.handle()
                    else -> throw Exception("Unknown job ${avro.jobName}")
                }
                when (behavior(avro)) {
                    Status.SUCCESS -> sendJobCompleted(avro, out)
                    Status.FAIL_WITH_RETRY -> sendJobFailed(avro, Exception("Will Try Again"), 0.1F)
                    Status.FAIL_WITHOUT_RETRY -> sendJobFailed(avro, Exception("Failed"))
                }
            }
        }
    }
}
