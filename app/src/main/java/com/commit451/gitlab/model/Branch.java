package com.commit451.gitlab.model;

import org.parceler.Parcel;

@Parcel
public class Branch {
    String name;
    boolean protected_;

    public Branch(){}

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public boolean isProtected() {
        return protected_;
    }
    public void setProtected(boolean protected_) {
        this.protected_ = protected_;
    }

    public String toString() {
        return name;
    }
}
