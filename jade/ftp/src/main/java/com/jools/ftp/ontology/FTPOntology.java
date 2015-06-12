package com.jools.ftp.ontology;

import jade.content.onto.BeanOntology;
import jade.content.onto.Ontology;
import jade.content.onto.OntologyException;

import com.jools.ftp.ontology.bean.FTPDownload;
import com.jools.ftp.ontology.bean.FTPUpload;
import com.jools.ftp.ontology.bean.FileWrapper;

@SuppressWarnings("serial")
public class FTPOntology extends BeanOntology {
	public static final String ONTOLOGY_NAME = "ftp-ontology";
	private static Ontology theInstance = new FTPOntology();

	public static Ontology getInstance() {
		return theInstance;
	}

	private FTPOntology() {
		super(ONTOLOGY_NAME);
		try {
			this.add(FileWrapper.class);
			this.add(FTPUpload.class);
			this.add(FTPDownload.class);
		} catch (OntologyException e) {
			e.printStackTrace();
		}
	}
}
