import java.io.*;
import java.util.*;
import java.net.*;

public class Bootstrap {
    private final int boot_port = 5050;
    private Socket clientSocket;
    private DataInputStream input;
    private Map<String, DataOutputStream> list_output = new HashMap<>();
    private TreeSet<String> ring_map = new TreeSet<>((a,b) -> {
        int x = compareHelper(a);
        int y = compareHelper(b);
        return Integer.compare(x, y);
    });
    private String client_name;

    private int compareHelper(String ring_list) {
        String[] str = ring_list.split("-");
        for (String part : str) {
            if (part.startsWith("n")) {
                return Integer.parseInt(part.substring(1));
            }
        }
        return -1;
    }

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
                    if(msg.equals("JOIN")){
                        connect(sender);
                        figure_pre_succ(sender);
                        print_pre_suc(sender);
                    }
                    else if(msg.startsWith("REQUEST")){
                        connect(sender);
                        client_name = sender;
                        inject_to_first_peer(msg);
                    }
                    else if(msg.startsWith("OBJ_STORED")){
                        String obj_msg = get_obj_succ(msg, "STORED");
                        sendMSG(obj_msg, client_name);
                    }
                    else if(msg.startsWith("OBJ_RETRIEVED")){
                        String obj_msg = get_obj_succ(msg, "RETRIEVED");
                        sendMSG(obj_msg, client_name);
                    }
                    else if(msg.startsWith("FAILED")){
                        String obj_msg = get_obj_succ(msg, "NOT_FOUND");
                        sendMSG(obj_msg, client_name);
                    }
                }
            }
        }
        catch(Exception e){
            System.err.println(e);
        }
    }

    private String get_obj_succ(String msg, String type){
        String[] str = msg.split(",");
        String obj_msg = type + ": " + Integer.parseInt(str[1]);
        return obj_msg;
    }

    private void inject_to_first_peer(String msg){
        String first_peer = ring_map.first();
        sendMSG(msg, first_peer);
    }

    private void figure_pre_succ(String sender){
        ring_map.add(sender);
        String predecessor = ring_map.lower(sender);
        String successor = ring_map.higher(sender);
        if (predecessor == null) {
            predecessor = ring_map.last();
        }
        if (successor == null) {
            successor = ring_map.first();
        }

        String msg =  "update_pre_suc," + predecessor + "," + successor;
        sendMSG(msg, sender);

        String msg_to_pre = "update_suc," + sender;
        sendMSG(msg_to_pre, predecessor);

        String msg_to_suc = "update_pre," + sender;
        sendMSG(msg_to_suc, successor);
    }

    private void print_pre_suc(String sender){
        System.err.println("node id: " + sender + " joined -> current ring: " + ring_map);
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

    public static void main(String[] args) throws IOException {
        try{
            Bootstrap bootstrap = new Bootstrap();
            new Thread(bootstrap::server).start();
            Thread.sleep(1000);

        } catch (Exception e) {
            System.err.println(e);
        }
    }

}
