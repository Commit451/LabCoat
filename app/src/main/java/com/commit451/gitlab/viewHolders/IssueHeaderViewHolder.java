package com.commit451.gitlab.viewHolders;

import android.support.v7.widget.RecyclerView;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Spinner;
import android.widget.TextView;

import com.commit451.gitlab.R;
import com.commit451.gitlab.tools.Repository;

import butterknife.Bind;
import butterknife.ButterKnife;
import in.uncod.android.bypass.Bypass;

/**
 * Created by Jawn on 8/6/2015.
 */
public class IssueHeaderViewHolder extends RecyclerView.ViewHolder {

    public static IssueHeaderViewHolder create(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_header_issue, parent, false);
        return new IssueHeaderViewHolder(view);
    }

    @Bind(R.id.title) TextView title;
    @Bind(R.id.state_spinner) Spinner stateSpinner;
    @Bind(R.id.assignee_spinner) Spinner assigneeSpinner;
    @Bind(R.id.milestone_spinner) Spinner milestoneSpinner;
    @Bind(R.id.description) TextView description;

    public IssueHeaderViewHolder(View view) {
        super(view);
        ButterKnife.bind(this, view);
    }

    public void bind() {
        title.setText(Repository.selectedIssue.getTitle());
        Bypass bypass = new Bypass();
        String desc = Repository.selectedIssue.getDescription();
        if(desc == null) {
            desc = "";
        }
        description.setText(bypass.markdownToSpannable(desc));
        description.setMovementMethod(LinkMovementMethod.getInstance());

//        ArrayList<String> temp3 = new ArrayList<>();
//        if(Repository.selectedIssue.getState().equals("opened")) {
//            temp3.add("opened");
//            temp3.add("closed");
//        }
//        else {
//            temp3.add("closed");
//            temp3.add("reopened");
//        }
//        stateSpinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, temp3));
//        stateSpinner.setSelection(temp3.indexOf(Repository.selectedIssue.getState()));
//        //Hack so that the onItemSelected does not get triggered the first time we create the view
//        stateSpinner.post(new Runnable() {
//            @Override
//            public void run() {
//                stateSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//                    @Override
//                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                        changeStatus();
//                    }
//
//                    @Override
//                    public void onNothingSelected(AdapterView<?> parent) {
//                    }
//                });
//            }
//        });
//
//        if(Repository.selectedIssue.getAssignee() != null) {
//            ArrayList<User> temp = new ArrayList<User>();
//            temp.add(Repository.selectedIssue.getAssignee());
//            assigneeSpinner.setAdapter(new UserAdapter(this, temp));
//        }
//
//        ArrayList<Milestone> temp2 = new ArrayList<Milestone>();
//        if(Repository.selectedIssue.getMilestone() != null) {
//            temp2.add(Repository.selectedIssue.getMilestone());
//        }
//        milestoneSpinner.setAdapter(new MilestonesAdapter(this, temp2));
    }
}