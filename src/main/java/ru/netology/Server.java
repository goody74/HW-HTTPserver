package ru.netology;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    final static List<String> validPaths = List.of("/index.html", "/spring.svg", "/spring.png", "/resources.html",
            "/styles.css", "/app.js", "/links.html", "/forms.html", "/classic.html", "/events.html", "/events.js");
    static ExecutorService threadPool = Executors.newFixedThreadPool(64);
    static int port;
    public Server(int port){
        Server.port=port;

    }
    public void start(){
        try (var serverSocket = new ServerSocket(port)){
            System.out.println("Server started...");
            while (true){
                try{
                    Socket client = serverSocket.accept();
                    threadPool.execute(new ClientHandler(client, validPaths));
                }catch (IOException e){
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
