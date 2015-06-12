package com.jools;

import jade.core.Profile;
import jade.core.Runtime;
import jade.util.ExtendedProperties;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;
import jade.util.leap.Properties;
import jade.core.ProfileImpl;

public class App {
    public static void main(String[] args) throws StaleProxyException {
        // Start a new JADE runtime system
        Runtime.instance().setCloseVM(true);
        Runtime rt = jade.core.Runtime.instance();
        Properties props = new ExtendedProperties();
        props.setProperty(Profile.GUI, "true");
        props.setProperty(Profile.LOCAL_HOST, "172.16.0.83");
        AgentContainer cc = rt.createMainContainer(new ProfileImpl(props));

        // Start a FTPAgent
        AgentController ftpAgent = cc.createNewAgent("ftp", "com.jools.jade.agent.FTPAgent",
            new String[]{
                "username",
                "password",
                "ftp.server.net"
            }
        );
        ftpAgent.start();
    }
}
