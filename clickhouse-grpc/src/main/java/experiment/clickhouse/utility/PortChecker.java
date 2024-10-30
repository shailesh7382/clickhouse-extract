package experiment.clickhouse.utility;

import java.io.IOException;
import java.net.Socket;

public class PortChecker {

    public static boolean isPortOpen(String host, int port) {
        try (Socket socket = new Socket(host, port)) {
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}