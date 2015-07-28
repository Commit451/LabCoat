package com.commit451.gitlab.events;

/**
 * We switched to a different project
 * Created by Jawn on 7/28/2015.
 */
public class ProjectChangedEvent {
    public int position;

    public ProjectChangedEvent(int position) {
        this.position = position;
    }
}
