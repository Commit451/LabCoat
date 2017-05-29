package com.commit451.gitlab.event

import com.commit451.gitlab.model.api.Pipeline

/**
 * A pipeline changed
 */
class PipelineChangedEvent(val pipeline: Pipeline)
