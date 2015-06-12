package com.jools.ftp.util;

import java.util.ArrayList;


import jade.content.lang.Codec;
import jade.content.onto.Ontology;
import jade.core.AID;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;

public class DFServiceHelper {

    private DFServiceHelper() {
    }

    public static void register(Agent a, String service, Codec codec, String protocol, Ontology ontology) throws FIPAException {
        DFAgentDescription dfad = new DFAgentDescription();
        dfad.setName(a.getAID());
        dfad.addLanguages(codec.getName());
        dfad.addProtocols(protocol);

        ServiceDescription sd = new ServiceDescription();
        sd.setName(service + "-" + a.getLocalName());
        sd.setType(service);
        sd.addOntologies(ontology.getName());
        sd.addLanguages(codec.getName());
        sd.addProtocols(protocol);

        dfad.addServices(sd);
        DFService.register(a, dfad);
    }

    public static void register(Agent a, String... services) throws FIPAException {
        DFAgentDescription dfad = new DFAgentDescription();
        dfad.setName(a.getAID());
        for (String serviceName : services) {
            ServiceDescription sd = new ServiceDescription();
            sd.setName(serviceName + "-" + a.getLocalName());
            sd.setType(serviceName);
            dfad.addServices(sd);
        }
        DFService.register(a, dfad);
    }

    public static ArrayList<AID> findAllByService(Agent a, String serviceName) throws FIPAException {
        DFAgentDescription adTemplate = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType(serviceName);
        adTemplate.addServices(sd);

        DFAgentDescription[] dfAgentDescriptions = DFService.search(a, adTemplate);
        if (dfAgentDescriptions.length <= 0) {
            throw new FIPAException("Could not find proper service");
        }
        ArrayList<AID> aids = new ArrayList<AID>();
        for (DFAgentDescription dfAgentDescription : dfAgentDescriptions) {
            aids.add(dfAgentDescription.getName());
        }
        return aids;
    }

    public static AID findFirstByService(Agent a, String serviceName) throws FIPAException {
        return findAllByService(a, serviceName).get(0);
    }
}
