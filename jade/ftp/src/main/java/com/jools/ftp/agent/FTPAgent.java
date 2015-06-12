package com.jools.ftp.agent;

import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPANames;

import org.apache.log4j.Logger;

import com.jools.ftp.util.DFServiceHelper;
import com.jools.ftp.behaviour.FTPUploadRequestResponderBehaviour;
import com.jools.ftp.ontology.FTPOntology;

import jade.content.lang.Codec;
import jade.content.lang.sl.SLCodec;
import jade.domain.FIPAAgentManagement.FIPAManagementOntology;
import java.net.InetAddress;

@SuppressWarnings("serial")
public class FTPAgent extends Agent {
    private static final Logger logger = Logger.getLogger(FTPAgent.class);

    @Override
    protected void setup() {
        try {
            Codec codec = new SLCodec();
            this.getContentManager().registerLanguage(codec);
            this.getContentManager().registerOntology(FIPAManagementOntology.getInstance());
            this.getContentManager().registerOntology(FTPOntology.getInstance());

            DFServiceHelper.register(
                    this,
                    "ftp-upload",
                    codec,
                    FIPANames.InteractionProtocol.FIPA_REQUEST,
                    FTPOntology.getInstance()
            );

            Object[] arguments = this.getArguments();
            this.addBehaviour(
                new FTPUploadRequestResponderBehaviour(
                    this,
                    (String)arguments[0],
                    (String)arguments[1],
                    InetAddress.getByName((String)arguments[2])
                )
            );
        } catch (Exception ex) {
            logger.fatal("Could not instanciate " + this.getLocalName() + " of type " + this.getClass().getSimpleName(), ex);
            this.doDelete();
            return;
        }
    }

    @Override
    protected void takeDown() {
        try {
            DFService.deregister(this);
        } catch (Exception ex) {
            logger.fatal("Error in DF deregistration", ex);
        }
        super.takeDown();
    }
}
