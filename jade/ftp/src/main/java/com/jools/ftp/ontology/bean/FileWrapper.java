package com.jools.ftp.ontology.bean;

import jade.content.onto.annotations.Slot;
import jade.content.Concept;

@SuppressWarnings("serial")
public class FileWrapper implements Concept {

	private String absolutePath;

	public FileWrapper() {
	}

	public FileWrapper(java.io.File file) {
		this.absolutePath = file.getAbsolutePath();
	}
	
	public FileWrapper(String absolutePath) {
		this.absolutePath = absolutePath;
	}
	
	@Slot(mandatory = true)
	public String getAbsolutePath() {
		return absolutePath;
	}
	
	public void setAbsolutePath(String absolutePath) {
		this.absolutePath = absolutePath;
	}	
	
	public java.io.File getFile() {
		return new java.io.File(this.absolutePath);
	}
	
	public boolean equals(java.io.File file){
		return new java.io.File(this.absolutePath).equals(file);
	}
	
	public boolean exists() {
		return new java.io.File(this.absolutePath).exists();
	}
	
	public boolean canRead() {
		return new java.io.File(this.absolutePath).canRead();
	}
	
	@Override
    public String toString() {
        return "File (" + this.absolutePath + ")";
    }
}