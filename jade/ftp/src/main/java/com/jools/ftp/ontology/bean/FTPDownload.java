package com.jools.ftp.ontology.bean;

import jade.content.onto.annotations.Slot;
import jade.content.AgentAction;

@SuppressWarnings("serial")
public class FTPDownload implements AgentAction {
	private String source;
	private FileWrapper destination;

	@Slot(mandatory = true)
	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	@Slot(mandatory = true)
	public FileWrapper getDestination() {
		return destination;
	}

	public void setDestination(FileWrapper destination) {
		this.destination = destination;
	}

	@Override
	public String toString() {
		return "FTPDownload (" + source + ", " + destination + ")";
	}

}