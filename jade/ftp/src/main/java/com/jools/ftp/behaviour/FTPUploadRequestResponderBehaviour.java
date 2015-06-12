package com.jools.ftp.behaviour;

import java.io.IOException;
import java.net.InetAddress;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.jools.util.FTPHelper;
import com.jools.ftp.ontology.FTPOntology;
import com.jools.ftp.ontology.bean.FTPUpload;
import com.jools.util.Ping;

import jade.content.AgentAction;
import jade.content.lang.Codec.CodecException;
import jade.content.onto.OntologyException;
import jade.content.onto.UngroundedException;
import jade.content.onto.basic.Action;
import jade.core.Agent;
import jade.domain.FIPANames;
import jade.domain.FIPAAgentManagement.FailureException;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.AchieveREResponder;

@SuppressWarnings("serial")
public class FTPUploadRequestResponderBehaviour extends AchieveREResponder {

    private static final Logger logger = Logger.getLogger(FTPUploadRequestResponderBehaviour.class);
    private final String username;
    private final String password;
    private final InetAddress host;

    public FTPUploadRequestResponderBehaviour(Agent a, String username, String password, InetAddress host) {
        super(a, MessageTemplate.and(
                MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST),
                MessageTemplate.MatchOntology(FTPOntology.ONTOLOGY_NAME)));
        this.username = username;
        this.password = password;
        this.host = host;
    }

    @Override
    protected ACLMessage handleRequest(ACLMessage request) throws NotUnderstoodException, RefuseException {
        try {
            AgentAction myAction = this.decodeRequest(request);
            if (myAction instanceof FTPUpload) {
                this.checkAction((FTPUpload) myAction);
                ACLMessage agree = request.createReply();
                agree.setPerformative(ACLMessage.AGREE);
                return agree;
            } else {
                throw new NotUnderstoodException("Action " + myAction.getClass() + " is not supported.\n" + request.getContent());
            }
        } catch (NotUnderstoodException e) {
            logger.fatal("Agent " + this.myAgent.getLocalName() + ": NotUnderstood");
            throw e;
        } catch (RefuseException e) {
            logger.fatal("Agent " + this.myAgent.getLocalName() + ": Refuse");
            throw e;
        }
    }

    @Override
    protected ACLMessage prepareResultNotification(ACLMessage request, ACLMessage response) throws FailureException {
        try {
            AgentAction myAction = this.decodeRequest(request);
            if (myAction instanceof FTPUpload) {
                if (this.performAction((FTPUpload) myAction)) {
                    ACLMessage inform = request.createReply();
                    inform.setPerformative(ACLMessage.INFORM);
                    return inform;
                } else {
                    logger.fatal("Agent " + this.myAgent.getLocalName() + ": Action failed");
                    throw new FailureException("unexpected-error");
                }
            } else {
                logger.fatal("Agent " + this.myAgent.getLocalName() + ": Action failed");
                throw new FailureException("unexpected-error");
            }
        } catch (NotUnderstoodException e) {
            logger.fatal("Agent " + this.myAgent.getLocalName() + ": Action failed");
            throw new FailureException("unexpected-error");
        }
    }

    private void checkAction(FTPUpload action) throws NotUnderstoodException, RefuseException {
        if (!action.getSource().exists() || !action.getSource().canRead()) {
            throw new RefuseException(action.getSource().getAbsolutePath() + " don't exists or can not be read.");
        }
        if (!Pattern.compile("(?:/[\\w-]+)?$").matcher(action.getDestination()).find()) {
            throw new NotUnderstoodException(action.getDestination() + " is not a valid remote path.");
        }
        try {
            Ping.reach(this.host);
        } catch (IOException e) {
            throw new RefuseException(this.host.getHostAddress() + " is not reachable.");
        }
    }

    private boolean performAction(FTPUpload action) {
        return FTPHelper.getInstance(this.username, this.password, this.host, false).upload(action.getSource().getFile(), action.getDestination());
    }

    private AgentAction decodeRequest(ACLMessage request) throws NotUnderstoodException {
        try {
            return (AgentAction) ((Action) this.myAgent.getContentManager().extractContent(request)).getAction();
        } catch (UngroundedException e) {
            throw new NotUnderstoodException(request.getContent() + "\n" + e.getMessage());
        } catch (CodecException e) {
            throw new NotUnderstoodException(request.getContent() + "\n" + e.getMessage());
        } catch (OntologyException e) {
            throw new NotUnderstoodException(request.getContent() + "\n" + e.getMessage());
        }
    }
}
