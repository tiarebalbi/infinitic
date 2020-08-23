package io.infinitic.workflowManager.common.data.branches

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue
import io.infinitic.common.data.SerializedData
import io.infinitic.taskManager.common.data.Input
import java.lang.reflect.Method

class BranchInput(override vararg val data: Any?) : Input(data) {
    @get:JsonValue val json get() = getSerialized()

    companion object {
        @JvmStatic @JsonCreator
        fun fromSerialized(s: List<SerializedData>) =
            BranchInput(*(s.map { it.deserialize() }.toTypedArray())).apply { serialized = s }

        fun from(m: Method, data: Array<out Any>) =
            BranchInput(*data).apply { serialized = getSerialized(m) }
    }
}
