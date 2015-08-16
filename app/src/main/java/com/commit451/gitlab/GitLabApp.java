package com.commit451.gitlab;

import android.app.Application;

import com.commit451.gitlab.model.Branch;
import com.commit451.gitlab.model.Project;
import com.commit451.gitlab.model.User;
import com.commit451.gitlab.tools.Repository;
import com.squareup.otto.Bus;

import net.danlew.android.joda.JodaTimeAndroid;

import timber.log.Timber;

/**
 * App for one time init things
 * Created by Jawn on 7/27/2015.
 */
public class GitLabApp extends Application {

    public Project selectedProject;
    public Branch selectedBranch;
    public User selectedUser;

    public Project getSelectedProject() {
        return selectedProject;
    }

    public void setSelectedProject(Project project) {
        selectedProject = project;
    }

    public Branch getSelectedBranch() {
        return selectedBranch;
    }

    public void setSelectedBranch(Branch branch) {
        selectedBranch = branch;
    }

    public User getSelectedUser() {
        return selectedUser;
    }

    public void setSelectedUser(User selectedUser) {
        this.selectedUser = selectedUser;
    }

    private static Bus bus;
    public static Bus bus() {
        if (bus == null) {
            bus = new Bus();
        }
        return bus;
    }

    private static GitLabApp instance;
    public static GitLabApp instance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }
        Repository.init();
        JodaTimeAndroid.init(this);
    }
}
