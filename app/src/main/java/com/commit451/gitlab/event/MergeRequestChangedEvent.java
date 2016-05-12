package com.commit451.gitlab.event;

import com.commit451.gitlab.model.api.MergeRequest;

/**
 * The merge request changed
 */
public class MergeRequestChangedEvent {
    public final MergeRequest mergeRequest;

    public MergeRequestChangedEvent(MergeRequest mergeRequest) {
        this.mergeRequest = mergeRequest;
    }
}
