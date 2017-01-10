package com.commit451.gitlab.event

import com.commit451.gitlab.model.api.MergeRequest

/**
 * The merge request changed
 */
class MergeRequestChangedEvent(val mergeRequest: MergeRequest)
