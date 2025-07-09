import java.io.*;
import java.util.*;
import java.net.*;

public class Peer {
    private static Map<String, String> argMap = new HashMap<>();
    private String server_name;
    private int delay;
    private String object_path;
    private String hostName;

    private final int boot_port = 5050;
    private Socket clientSocket;
    private DataInputStream input;
    private Map<String, DataOutputStream> list_output = new HashMap<>();

    private String predecessor;
    private String successor;

    private int c_client_id;
    private int c_object_id;
    private String c_operationType;
    private int id;
    private boolean attempt_object_search = false;

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
                    if(msg.startsWith("update_pre_suc")){
                        update_pre_succ(msg);
                        print_pre_suc();
                    }
                    if(msg.startsWith("update_suc")){
                        update_succ(msg);
                        print_pre_suc();
                    }
                    if(msg.startsWith("update_pre")){
                        update_pre(msg);
                        print_pre_suc();
                    }
                    if (msg.startsWith("REQUEST")){
                        handle_client_req(msg);
                        if(c_operationType.equals("STORE")){
                            if(verify_store() && !attempt_object_search){
                                attempt_object_search = true;
                                store_object(c_client_id, c_object_id);
                                print_object();
                                String ok = msg_to_bootstrap("OBJ_STORED");
                                sendMSG(ok, server_name);
                            }
                            else if(!attempt_object_search){
                                attempt_object_search = true;
                                if (!list_output.containsKey(successor)){
                                    connect(successor);
                                }
                                sendMSG(msg, successor);
                            }
                            else{
                                sendMSG("FAILED", server_name);
                            }
                        }
                        else if(c_operationType.equals("RETRIEVE")){
                            if(verify_retrieve(c_client_id, c_object_id) && !attempt_object_search){
                                attempt_object_search = true;
                                String ok = msg_to_bootstrap("OBJ_RETRIEVED");
                                sendMSG(ok, server_name);
                            }
                            else if(!attempt_object_search){
                                attempt_object_search = true;
                                if (!list_output.containsKey(successor)){
                                    connect(successor);
                                }
                                sendMSG(msg, successor);
                            }
                            else{
                                String fail = msg_to_bootstrap("FAILED");
                                sendMSG(fail, server_name);
                            }
                        }
                    }
                }
            }
        }
        catch(Exception e){
            System.err.println(e);
        }
    }

    private String msg_to_bootstrap(String operation){
        String msg = operation + "," + c_object_id + "," + c_client_id + "," + hostName;
        return msg;
    }

    private void store_object(int client, int object){
        String input = client + "::" + object;
        try (BufferedWriter br = new BufferedWriter(new FileWriter(object_path, true))) {
            br.write(input);
            br.newLine();
        } catch (IOException e) {
            System.err.println(e);
        }
    }

    private void print_object(){
        try (BufferedReader br = new BufferedReader(new FileReader(object_path))) {
            String line;
            while ((line = br.readLine()) != null) {
                System.err.println(line);
            }
        } catch (IOException e) {
            System.err.println(e);
        }
    }

    private boolean verify_retrieve(int client, int object){
        if(verify_store()){
            String input = client + "::" + object;
            try (BufferedReader br = new BufferedReader(new FileReader(object_path))) {
                String line;
                while ((line = br.readLine()) != null) {
                    if (line.equals(input)) {
                        return true;
                    }
                }
            } catch (IOException e) {
                System.err.println(e);
            }
        }
        return false;
    }

    private boolean verify_store(){
        String[] str = predecessor.split("-");
        int pre = Integer.parseInt(str[1].substring(1));
        if (pre < id) {
            return c_object_id > pre && c_object_id <= id;
        }
        return c_object_id > pre || c_object_id <= id;
    }

    private void handle_client_req(String msg){
        String[] str = msg.split(",");
        c_operationType = str[2];
        c_object_id = Integer.parseInt(str[3]);
        c_client_id = Integer.parseInt(str[4]);
    }

    private void update_pre_succ(String msg){
        String[] str = msg.split(",");
        predecessor = str[1];
        successor = str[2];
    }
    private void update_succ(String msg){
        String[] str = msg.split(",");
        successor = str[1];
    }
    private void update_pre(String msg){
        String[] str = msg.split(",");
        predecessor = str[1];
    }
    private void print_pre_suc(){
        System.err.println("node id: " + hostName + ", Predecessor: " + predecessor + ", Successor: " + successor);
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
            if (curr.equals("-b") || curr.equals("-d") || curr.equals("-o")) {
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
            Peer peer = new Peer();
            readCommand(args);

            if (argMap.containsKey("-b")) {
                peer.server_name = argMap.get("-b");
            }
            if (argMap.containsKey("-d")) {
                peer.delay = Integer.parseInt(argMap.get("-d"));
            }
            if (argMap.containsKey("-o")) {
                peer.object_path = argMap.get("-o");
            }

            peer.hostName = InetAddress.getLocalHost().getHostName().split("\\.")[0];
            peer.id = Integer.parseInt(peer.hostName.substring(1));
            
            new Thread(peer::server).start();
            Thread.sleep(1000 * peer.delay);
            peer.connect(peer.server_name);
            peer.sendMSG("JOIN", peer.server_name);

        } catch (Exception e){
            System.err.println(e.getMessage());
        }
    }
}
