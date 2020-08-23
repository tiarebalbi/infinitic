package io.infinitic.taskManager.common.data

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue
import io.infinitic.common.data.SerializedData

data class TaskMeta
@JsonCreator(mode = JsonCreator.Mode.DELEGATING)
constructor(@get:JsonValue override val meta: MutableMap<String, SerializedData> = mutableMapOf()) : Meta(meta)
