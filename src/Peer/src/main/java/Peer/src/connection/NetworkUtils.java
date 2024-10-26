package Peer.src.connection;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class NetworkUtils {
    public static String getHostDefaultInterfaceIp() {
        try (DatagramSocket socket = new DatagramSocket()) {
            socket.connect(InetAddress.getByName("8.8.8.8"), 53); // Connect to Google's public DNS server
            return socket.getLocalAddress().getHostAddress();
        } catch (SocketException | UnknownHostException e) {
            e.printStackTrace();
            return "127.0.0.1"; // If an error occurs, return localhost
        }
    }


}