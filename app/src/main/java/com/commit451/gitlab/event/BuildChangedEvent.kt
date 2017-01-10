package com.commit451.gitlab.event

import com.commit451.gitlab.model.api.Build

/**
 * A build changed
 */
class BuildChangedEvent(val build: Build)
