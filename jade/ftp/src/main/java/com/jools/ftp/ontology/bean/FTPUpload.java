package com.jools.ftp.ontology.bean;

import jade.content.onto.annotations.Slot;
import jade.content.AgentAction;

@SuppressWarnings("serial")
public class FTPUpload implements AgentAction{
	private FileWrapper source;
	private String destination;
	
	@Slot(mandatory = true)
	public FileWrapper getSource() {
		return source;
	}
	
	public void setSource(FileWrapper source) {
		this.source = source;
	}
	
	@Slot(mandatory = true)
	public String getDestination() {
		return destination;
	}
	
	public void setDestination(String destination) {
		this.destination = destination;
	}
	
    @Override
    public String toString() {
        return "FTPUpload (" + source + ", " + destination + ")";
    }
}