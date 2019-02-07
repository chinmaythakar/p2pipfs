import java.io.File;
import java.io.Serializable;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class p2pNode implements Serializable {

    public static HashMap<String, Integer> peerTable = new HashMap<String, Integer>();
    public static HashMap<String, ArrayList<String>> contentTracker = new HashMap<String, ArrayList<String>>();
    public static HashMap<String, String> hashToFile = new HashMap<String, String>();
    public static String ipAdd;
    public static int port;

    public static void main(String[] args) throws Exception {
        Scanner sc = new Scanner(System.in);
        port = 5555;
        ipAdd = InetAddress.getLocalHost().getHostName();
        ExecutorService executor = Executors.newFixedThreadPool(500);
        // start listening to client request
        Runnable r1 = new clientGateway();
        executor.execute(r1);
        // start listening on local port for application request
        Runnable r2 = new AppMessages();
        executor.execute(r2);
        while (true) {
            System.out.println("Help");
            System.out.println("PEER <peer-hostname> <peer-port>");
            System.out.println("PUBLISH <filename>");
            System.out.println("UNPPUBLISH <filename>");
            System.out.println("SHOW_PEERS");
            System.out.println("SHOW_METADATA");
            System.out.println("SHOW_PUBLISHED");
            System.out.println("QUIT");
            System.out.println("Your selection: ");
            String input = sc.nextLine();
            input = input.trim();
            String inp[] = input.split(" ");
            switch (inp[0].toUpperCase()) {
                case "PEER": {
                    Runnable r = new Peer(inp[1], inp[2]);
                    executor.execute(r);
                    break;
                }
                case "PUBLISH": {
                    File f = new File(inp[1]);
                    Runnable r = null;
                    try {
                        r = new Publish(f);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (r != null)
                        executor.execute(r);
                    break;
                }

                case "UNPUBLISH": {
                    Runnable r = new Unpublish(inp[1]);
                    executor.execute(r);
                    break;
                }

                case "SHOW_PEERS": {
                    for (Map.Entry<String, Integer> entry : peerTable
                            .entrySet()) {
                        System.out.println(
                                entry.getKey() + " : " + entry.getValue());
                    }
                    break;
                }
                case "SHOW_METADATA": {
                    for (Map.Entry<String, ArrayList<String>> entry : contentTracker
                            .entrySet()) {
                        System.out.println(
                                entry.getKey() + " : " + entry.getValue());
                    }
                    break;
                }
                case "SHOW_PUBLISHED": {
                    for (Map.Entry<String, ArrayList<String>> entry : contentTracker
                            .entrySet()) {
                        String key = entry.getKey();
                        ArrayList<String> value = entry.getValue();
                        String[] str = key.split("~");
                        if (str[1].equals(ipAdd))
                            System.out.println(str[0] + ":" + value);
                    }
                    break;
                }
                case "FILES": {
                    for (Map.Entry<String, String> entry : hashToFile
                            .entrySet()) {
                        System.out.println(
                                entry.getKey() + " : " + entry.getValue());
                    }
                    break;
                }
                case "QUIT": {
                    executor.shutdownNow();
                    System.exit(0);
                }
                default:
                    System.out.println("Incorrect Selection, Try again!");
            }
        }
    }
}
