package com.commit451.gitlab.model;

import org.parceler.Parcel;

import java.util.ArrayList;
import java.util.List;
@Parcel
public class Diff {
	
	String diff;
	String new_path;
	String old_path;
	int a_mode;
	int b_mode;
	boolean new_file;
	boolean renamed_file;
	boolean deleted_file;

	public Diff(){}
	
	public String getDiff() {
		return diff;
	}
	
	public void setDiff(String diff) {
		this.diff = diff;
	}
	
	public List<Line> getLines() {
		ArrayList<Line> lines = new ArrayList<Line>();
		
		int curOldLine = 0;
		int curNewLine = 0;
		
		String[] temp = diff.split("\\r?\\n");
		for(String s : temp) {
			if(s.startsWith("+++") || s.startsWith("---"))
				continue;
			
			if(s.startsWith("@@")) {
				int index = s.indexOf(',');
				if(index == -1 || index >= s.indexOf('+'))
					index = s.indexOf('-') + 2;
				curOldLine = Integer.parseInt(s.substring(s.indexOf('-') + 1, index));
				
				index = s.indexOf(',', s.indexOf('+'));
				if(index == -1)
					index = s.indexOf('+') + 2;
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
			
			if(s.length() < 1)
				s = " ";
			
			switch(s.charAt(0)) {
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
			
			if(line.lineType != LineType.ADDED) {
				line.oldLine = String.valueOf(curOldLine);
				curOldLine++;
			}
			if(line.lineType != LineType.REMOVED) {
				line.newLine = String.valueOf(curNewLine);
				curNewLine++;
			}
			
			lines.add(line);
		}
		
		return lines;
	}
	
	public String getNewPath() {
		return new_path;
	}
	
	public void setNewPath(String new_path) {
		this.new_path = new_path;
	}
	
	public String getOldPath() {
		return old_path;
	}
	
	public void setOldPath(String old_path) {
		this.old_path = old_path;
	}
	
	public int getAMode() {
		return a_mode;
	}
	
	public void setAMode(int a_mode) {
		this.a_mode = a_mode;
	}
	
	public int getBMode() {
		return b_mode;
	}
	
	public void setBMode(int b_mode) {
		this.b_mode = b_mode;
	}
	
	public boolean isNewFile() {
		return new_file;
	}
	
	public void setNewFile(boolean new_file) {
		this.new_file = new_file;
	}
	
	public boolean isRenamedFile() {
		return renamed_file;
	}
	
	public void setRenamedFile(boolean renamed_file) {
		this.renamed_file = renamed_file;
	}
	
	public boolean isDeletedFile() {
		return deleted_file;
	}
	
	public void setDeletedFile(boolean deleted_file) {
		this.deleted_file = deleted_file;
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
