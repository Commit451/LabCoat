package com.commit451.gitlab.model.api;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;
import com.commit451.gitlab.util.ObjectUtil;

import org.parceler.Parcel;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Parcel
@JsonObject
public class Contributor {
    @JsonField(name = "name")
    String name;
    @JsonField(name = "email")
    String email;
    @JsonField(name = "commits")
    int commits;
    @JsonField(name = "additions")
    int additions;
    @JsonField(name = "deletions")
    int deletions;

    public Contributor() {}

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public int getCommits() {
        return commits;
    }

    public int getAdditions() {
        return additions;
    }

    public int getDeletions() {
        return deletions;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Contributor)) {
            return false;
        }

        Contributor contributor = (Contributor) o;
        return ObjectUtil.equals(name, contributor.name) && ObjectUtil.equals(email, contributor.email);
    }

    @Override
    public int hashCode() {
        return ObjectUtil.hash(name, email);
    }

    public static List<Contributor> groupContributors(List<Contributor> contributors) {
        Set<ContributorEntry> contributorEntries = new LinkedHashSet<>();
        for (Contributor contributor : contributors) {
            ContributorEntry contrib = null;
            for (ContributorEntry entry : contributorEntries) {
                if ((contributor.name != null && contributor.name.equals(entry.mContributor.name))
                        || (contributor.email != null && contributor.email.equals(entry.mContributor.email))) {

                    while (entry.mReplacement != null) {
                        entry = entry.mReplacement;
                    }

                    if (contrib == entry) {
                        continue;
                    }

                    if (contrib == null) {
                        contrib = entry;
                    } else {
                        contrib.mContributor.commits += entry.mContributor.commits;
                        contrib.mContributor.additions += entry.mContributor.additions;
                        contrib.mContributor.deletions += entry.mContributor.deletions;

                        entry.mReplacement = contrib;
                    }
                }
            }

            if (contrib == null) {
                contrib = new ContributorEntry();
                contrib.mContributor = contributor;

                contributorEntries.add(contrib);
            } else {
                contrib.mContributor.commits += contributor.commits;
                contrib.mContributor.additions += contributor.additions;
                contrib.mContributor.deletions += contributor.deletions;
            }
        }

        contributors = new ArrayList<>();

        for (ContributorEntry entry : contributorEntries) {
            if (entry.mReplacement == null) {
                contributors.add(entry.mContributor);
            }
        }

        return contributors;
    }

    private static class ContributorEntry {
        Contributor mContributor;
        ContributorEntry mReplacement;
    }
}
