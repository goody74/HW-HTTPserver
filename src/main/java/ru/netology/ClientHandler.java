package ru.netology;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ClientHandler implements Runnable{
     Socket client;
    List<String> validPaths;

   public ClientHandler(Socket client, List<String> validPaths){
       this.client=client;
       this.validPaths=validPaths;
   }

    @Override
    public void run() {
    try(
         var in = new BufferedReader(new InputStreamReader(client.getInputStream()));
         var out = new BufferedOutputStream(client.getOutputStream());
    ) {
         var requestLine = in.readLine();
         if(requestLine==null){
             return;
         }
         var parts = requestLine.split(" ");


        if (parts.length != 3) {
            in.close();
            out.close();
            return;
        }

        var path = parts[1];
        if (!validPaths.contains(path)) {
            out.write((
                    "HTTP/1.1 404 Not Found\r\n" +
                            "Content-Length: 0\r\n" +
                            "Connection: close\r\n" +
                            "\r\n"
            ).getBytes());
            out.flush();
            return;

        }

        var filePath = Path.of(".", "public", path);
        var mimeType = Files.probeContentType(filePath);

        // special case for classic
        if (path.equals("/classic.html")) {
            var template = Files.readString(filePath);
            var content = template.replace(
                    "{time}",
                    LocalDateTime.now().toString()
            ).getBytes();
            out.write((
                    "HTTP/1.1 200 OK\r\n" +
                            "Content-Type: " + mimeType + "\r\n" +
                            "Content-Length: " + content.length + "\r\n" +
                            "Connection: close\r\n" +
                            "\r\n"
            ).getBytes());
            out.write(content);
            out.flush();
            return;

        }

        var length = Files.size(filePath);
        out.write((
                "HTTP/1.1 200 OK\r\n" +
                        "Content-Type: " + mimeType + "\r\n" +
                        "Content-Length: " + length + "\r\n" +
                        "Connection: close\r\n" +
                        "\r\n"
        ).getBytes());
        Files.copy(filePath, out);
        out.flush();
        return;

    }catch (IOException e) {
        e.printStackTrace();
    }

    }
}
