package com.jools.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.util.StringTokenizer;

public class Ping {

    public static void reach(InetAddress ipstr) throws IOException {
        Ping.reach(ipstr, 2);
    }

    public static void reach(InetAddress ipstr, int timeout) throws IOException {
        try {
            InputStream ins = Runtime.getRuntime().exec("ping -c 1 -W " + timeout + " " + ipstr.getHostAddress()).getInputStream();
            while (ins.available() <= 0) {
                Thread.sleep(10);
            }
            byte[] prsbuf = new byte[ins.available()];
            ins.read(prsbuf);
            String parsstr = new StringTokenizer(new String(prsbuf), "%").nextToken().trim();
            if (parsstr.endsWith("100")) {
                throw new IOException(ipstr.getHostAddress() + " is not reachable");
            }
        } catch (InterruptedException ex) {
            throw new IOException(ipstr.getHostAddress() + " ping interrupted", ex);
        } catch (NullPointerException ex) {
            throw new IOException("unknown host", ex);
        }
    }
}
