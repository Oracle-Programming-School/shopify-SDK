
package com.ordg.utl.configurations;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;

public class SystemInformation {
    public static String[] getSystemInformation() {
        String[] systemInformation = new String[3];
        try {
            InetAddress localHost = InetAddress.getLocalHost();
            systemInformation[0] = localHost.getHostAddress(); // Get the IP address
            NetworkInterface networkInterface = NetworkInterface.getByInetAddress(localHost);
            byte[] macAddressBytes = networkInterface.getHardwareAddress();
            StringBuilder macAddressBuilder = new StringBuilder();
            for (int i = 0; i < macAddressBytes.length; i++) {
                macAddressBuilder.append(String.format("%02X%s", macAddressBytes[i], (i < macAddressBytes.length - 1) ? "-" : ""));
            }
            systemInformation[1] = macAddressBuilder.toString(); // Get the MAC address
            systemInformation[2] = localHost.getHostName(); // Get the hostname
        } catch (UnknownHostException | SocketException e) {
            e.printStackTrace();
        }
        return systemInformation;
    }
}
