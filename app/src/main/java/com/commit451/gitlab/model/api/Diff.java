package com.commit451.gitlab.model.api;

import com.google.gson.annotations.SerializedName;

import org.parceler.Parcel;

import java.util.ArrayList;
import java.util.List;

@Parcel
public class Diff {
    @SerializedName("old_path")
    String mOldPath;
    @SerializedName("new_path")
    String mNewPath;
    @SerializedName("a_mode")
    int mAMode;
    @SerializedName("b_mode")
    int mBMode;
    @SerializedName("diff")
    String mDiff;
    @SerializedName("new_file")
    boolean mNewFile;
    @SerializedName("renamed_file")
    boolean mRenamedFile;
    @SerializedName("deleted_file")
    boolean mDeletedFile;

    public Diff() {}

    public String getOldPath() {
        return mOldPath;
    }

    public String getNewPath() {
        return mNewPath;
    }

    public int getAMode() {
        return mAMode;
    }

    public int getBMode() {
        return mBMode;
    }

    public String getDiff() {
        return mDiff;
    }

    public boolean isNewFile() {
        return mNewFile;
    }

    public boolean isRenamedFile() {
        return mRenamedFile;
    }

    public boolean isDeletedFile() {
        return mDeletedFile;
    }

    public List<Line> getLines() {
        List<Line> lines = new ArrayList<>();

        int curOldLine = 0;
        int curNewLine = 0;

        String[] temp = mDiff.split("\\r?\\n");
        for (String s : temp) {
            if (s.startsWith("+++") || s.startsWith("---")) {
                continue;
            }

            if (s.startsWith("@@")) {
                int index = s.indexOf(',');
                if (index == -1 || index >= s.indexOf('+')) {
                    index = s.indexOf('-') + 2;
                }
                curOldLine = Integer.parseInt(s.substring(s.indexOf('-') + 1, index));

                index = s.indexOf(',', s.indexOf('+'));
                if (index == -1) {
                    index = s.indexOf('+') + 2;
                }
                curNewLine = Integer.parseInt(s.substring(s.indexOf('+') + 1, index));

                Line line = new Line();
                line.lineContent = s;
                line.lineType = LineType.COMMENT;
                line.oldLine = "...";
                line.newLine = "...";

                lines.add(line);
                continue;
            }

            Line line = new Line();
            line.lineContent = s;

            if (s.length() < 1) {
                s = " ";
            }

            switch (s.charAt(0)) {
                case ' ':
                    line.lineType = LineType.NORMAL;
                    break;
                case '+':
                    line.lineType = LineType.ADDED;
                    break;
                case '-':
                    line.lineType = LineType.REMOVED;
                    break;
            }

            line.oldLine = "";
            line.newLine = "";

            if (line.lineType != LineType.ADDED) {
                line.oldLine = String.valueOf(curOldLine);
                curOldLine++;
            }
            if (line.lineType != LineType.REMOVED) {
                line.newLine = String.valueOf(curNewLine);
                curNewLine++;
            }

            lines.add(line);
        }

        return lines;
    }

    public class Line {
        public String oldLine;
        public String newLine;
        public String lineContent;
        public LineType lineType;
    }

    public enum LineType {
        NORMAL, ADDED, REMOVED, COMMENT
    }
}
