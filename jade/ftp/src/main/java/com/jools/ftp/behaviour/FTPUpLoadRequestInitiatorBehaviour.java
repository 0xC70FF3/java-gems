package com.jools.ftp.behaviour;

import java.io.File;
import java.util.Vector;

import org.apache.log4j.Logger;

import com.jools.ftp.util.DFServiceHelper;
import com.jools.ftp.ontology.FTPOntology;
import com.jools.ftp.ontology.bean.FTPUpload;
import com.jools.ftp.ontology.bean.FileWrapper;

import jade.content.lang.Codec.CodecException;
import jade.content.onto.OntologyException;
import jade.content.onto.basic.Action;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.WakerBehaviour;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.proto.AchieveREInitiator;

@SuppressWarnings("serial")
public class FTPUpLoadRequestInitiatorBehaviour extends AchieveREInitiator {

    private static Logger logger = Logger.getLogger(FTPUpLoadRequestInitiatorBehaviour.class);
    private final File source;
    private final String destination;
    private final long timeout;
    private final int retries;

    public FTPUpLoadRequestInitiatorBehaviour(Agent a, File src, String dst) {
        this(a, src, dst, 0, 0);
    }

    public FTPUpLoadRequestInitiatorBehaviour(Agent a, File src, String dst, long timeout, int nbMaxRetries) {
        super(a, null);
        this.source = src;
        this.destination = dst;
        this.timeout = timeout;
        this.retries = nbMaxRetries;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected Vector prepareRequests(ACLMessage r) {
        Vector<ACLMessage> requests = new Vector<ACLMessage>();
        try {
            AID recipient = DFServiceHelper.findFirstByService(this.myAgent,
                    "ftp-upload");
            ACLMessage request = new ACLMessage(ACLMessage.REQUEST);
            request.addReceiver(recipient);
            request.setLanguage(FIPANames.ContentLanguage.FIPA_SL);
            request.setOntology(FTPOntology.getInstance().getName());
            request.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
            FTPUpload ftpUploadRequest = new FTPUpload();
            ftpUploadRequest.setDestination(this.destination);
            ftpUploadRequest.setSource(new FileWrapper(
                    this.source));
            this.myAgent.getContentManager().fillContent(request,
                    new Action(recipient, ftpUploadRequest));
            requests.add(request);
            return requests;
        } catch (FIPAException e) {
            logger.fatal("unexpected-error", e);
        } catch (CodecException e) {
            logger.fatal("unexpected-error", e);
        } catch (OntologyException e) {
            logger.fatal("unexpected-error", e);
        }
        return requests;
    }

    @Override
    protected void handleInform(ACLMessage inform) {
        logger.info("Agent " + inform.getSender().getName()
                + " successfully performed the requested action");
    }

    @Override
    protected void handleRefuse(ACLMessage refuse) {
        logger.warn("Agent " + refuse.getSender().getName()
                + " refused to perform the requested action");
        this.handleError();
    }

    @Override
    protected void handleFailure(ACLMessage failure) {
        if (failure.getSender().equals(myAgent.getAMS())) {
            logger.fatal("Notification from the JADE runtime: the receiver does not exist");
        } else {
            logger.fatal("Agent " + failure.getSender().getName()
                    + " failed to perform the requested action");
        }
        this.handleError();
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void handleAllResultNotifications(Vector notifications) {
        if (notifications.size() < 1) {
            logger.warn("Timeout expired: missing response");
            this.handleError();
        }
    }

    private void handleError() {
        if (this.retries > 0 && this.timeout > 0) {
            logger.warn("Action is delayed.");
            this.myAgent.addBehaviour(new WakerBehaviour(this.myAgent, timeout) {

                @Override
                protected void onWake() {
                    super.onWake();
                    this.myAgent.addBehaviour(new FTPUpLoadRequestInitiatorBehaviour(
                            this.myAgent,
                            FTPUpLoadRequestInitiatorBehaviour.this.source,
                            FTPUpLoadRequestInitiatorBehaviour.this.destination,
                            FTPUpLoadRequestInitiatorBehaviour.this.timeout,
                            FTPUpLoadRequestInitiatorBehaviour.this.retries - 1));
                }
            });
        } else {
            logger.warn("Action is aborted: file " + this.source.getAbsolutePath() + " not transmitted.");
        }
    }
}
