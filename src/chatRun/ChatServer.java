package chatRun;

import chatHandler.ChatDAO;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class ChatServer {
    private ServerSocket serverSocket;
    private ArrayList<ChatDAO> list;
    public ChatServer() {
        try{
            serverSocket = new ServerSocket(8099);
            System.out.println("서버 준비 완료");
            list = new ArrayList<ChatDAO>();

            while(true) {
                Socket socket = serverSocket.accept();
                ChatDAO handler = new ChatDAO(socket, list);
                handler.start();
                list.add(handler);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void main(String[] args) {
        new ChatServer();
    }
}

