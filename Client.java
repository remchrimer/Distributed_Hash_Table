import java.io.*;
import java.util.*;
import java.net.*;

public class Client {
    private static Map<String, String> argMap = new HashMap<>();
    private String server_name;
    private int delay;
    private int test_case;
    private String hostName;
    private int c_client_id;
    private int c_object_id;
    private static int reqID = 0;
    private String c_operationType;

    private final int boot_port = 5050;
    private Socket clientSocket;
    private DataInputStream input;
    private Map<String, DataOutputStream> list_output = new HashMap<>();

    private void server(){
        try{
            ServerSocket serverSocket = new ServerSocket(boot_port);
            while(true){
                clientSocket = serverSocket.accept();
                input = new DataInputStream(clientSocket.getInputStream());

                new Thread(() -> msgListener(clientSocket, input)).start();
                Thread.sleep(1000);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void msgListener(Socket clientSocket, DataInputStream input){
        try {
            String sender = clientSocket.getInetAddress().getHostName().split("\\.")[0];
            while (true){
                String msg = input.readUTF();
                synchronized (this) {
                    print_res(msg);
                }
            }
        }
        catch(Exception e){
            System.err.println(e);
        }
    }

    private void print_res(String msg){
        System.err.println(msg);
    }

    private void sendMSG(String msg, String sendToAddr){
        try{
            if (list_output.containsKey(sendToAddr)) {
                list_output.get(sendToAddr).writeUTF(msg);
                list_output.get(sendToAddr).flush();
            }
        }
        catch(Exception e){
            System.err.println(e);
        }
    }

    private void connect(String name) throws IOException {
        Socket host_socket = new Socket(name, boot_port);
        list_output.put(name, new DataOutputStream(host_socket.getOutputStream()));
    }

    private static void readCommand(String[] args){
        for(int i = 0; i < args.length; i++){
            String curr = args[i];
            if (curr.equals("-b") || curr.equals("-d") || curr.equals("-t")) {
                if(i + 1 < args.length){
                    argMap.put(args[i], args[i + 1]);
                }
                else{
                    System.err.println("Command line arguments missing");
                    break;
                }
            }
        }
    }

    public static void main(String[] args) {
        try{
            Client client = new Client();
            readCommand(args);

            if (argMap.containsKey("-b")) {
                client.server_name = argMap.get("-b");
            }
            if (argMap.containsKey("-d")) {
                client.delay = Integer.parseInt(argMap.get("-d"));
            }
            if (argMap.containsKey("-t")) {
                client.test_case = Integer.parseInt(argMap.get("-t"));
            }

            client.hostName = InetAddress.getLocalHost().getHostName().split("\\.")[0];

            new Thread(client::server).start();
            Thread.sleep(1000 * client.delay);
            client.connect(client.server_name);

            // Hard coded the different test cases as mentioned in the pdf
            if (client.test_case == 3){
                client.c_client_id = 3;
                client.c_object_id = 127;
                client.c_operationType = "STORE";
                String msg = "REQUEST," + reqID + "," + client.c_operationType + ","
                        + client.c_object_id + "," + client.c_client_id;
                client.sendMSG(msg, client.server_name);
                reqID++;
            } else if (client.test_case == 4) {
                client.c_client_id = 3;
                client.c_object_id = 127;
                client.c_operationType = "RETRIEVE";
                String msg = "REQUEST," + reqID + "," + client.c_operationType + ","
                        + client.c_object_id + "," + client.c_client_id;
                client.sendMSG(msg, client.server_name);
                reqID++;
            } else if (client.test_case == 5) {
                client.c_client_id = 5;
                client.c_object_id = 77;
                client.c_operationType = "RETRIEVE";
                String msg = "REQUEST," + reqID + "," + client.c_operationType + ","
                        + client.c_object_id + "," + client.c_client_id;
                client.sendMSG(msg, client.server_name);
                reqID++;
            }
        } catch (Exception e){
            System.err.println(e);
        }
    }
}
