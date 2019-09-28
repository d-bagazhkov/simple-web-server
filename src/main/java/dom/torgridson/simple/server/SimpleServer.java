package dom.torgridson.simple.server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.StringJoiner;
import java.util.stream.Collectors;

public class SimpleServer {

    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;

    public SimpleServer(Socket socket) throws IOException {
        bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
    }

    public static void main(String[] args) {
        try {
            ServerSocket serverSocket = new ServerSocket(8080);
            while (true) {
                Socket socket = serverSocket.accept();
                new Thread(() -> {
                    try {
                        new SimpleServer(socket).run();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }).start();
            }
        } catch (IOException e) {
            System.err.println(e);
        }
    }

    private void run() throws IOException {
        String path = getPath();
        String msg = get(path);
        if (msg == null || path.equals("/"))
            answer(get("/index.html"));
        else
            answer(msg);

    }

    private String getPath() throws IOException {
        String line;
        String path = "";
        while ((line = bufferedReader.readLine()) != null && line.length() != 0)
            if (line.startsWith("GET"))
                path = line;
        System.out.println(path);
        return path.split(" ")[1];
    }

    private String get(String prefix) {
        InputStream in = getClass().getResourceAsStream(prefix);
        if (in == null) return null;
        return new BufferedReader(new InputStreamReader(in))
                .lines()
                .collect(Collectors.joining());
    }

    private void answer(final String msg) throws IOException {
        bufferedWriter.write(
                new StringJoiner("\r\n")
                        .add("HTTP/1.1 200 OK")
                        .add("Server: YarServer/2009-09-09")
                        .add("Content-Type: text/html")
                        .add("Content-Length: " + msg.length())
                        .add("Connection: close")
                        .add("")
                        .add(msg)
                        .toString()
        );
        bufferedWriter.flush();
    }

}
