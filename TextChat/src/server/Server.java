package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.io.UnsupportedEncodingException;
import java.io.PrintStream;
import java.net.InetAddress;

    //Основной класс сервера чата
    public class Server {
    private static final int PORT = 1234;
    private final ServerSocket serverSocket;
    private final UserManager userManager;
    private final List<ClientHandler> clients = new ArrayList<>();
    private volatile boolean running = true;

    public Server(int port) throws IOException {
        this.serverSocket = new ServerSocket(port, 50, InetAddress.getByName("127.0.0.1"));
        this.userManager = new UserManager();
        System.out.println("Сервер запущен на 127.0.0.1:" + port);
    }

    public void run() {
        while (running) {
            try {
                Socket clientSocket = serverSocket.accept();
                String clientIP = clientSocket.getInetAddress().getHostAddress();
                System.out.println("Клиент подключён: " + clientIP);

                ClientHandler handler = new ClientHandler(clientSocket, clientIP, userManager, clients);
                
                synchronized (clients) {
                    clients.add(handler);
                }

                Thread clientThread = new Thread(handler);
                clientThread.setDaemon(true);
                clientThread.start();

            } catch (IOException e) {
                if (running) {
                    System.err.println("Ошибка принятия подключения: " + e.getMessage());
                }
            }
        }
    }

    public void stop() {
        running = false;
        synchronized (clients) {
            for (ClientHandler client : clients) {
                client.disconnect();
            }
            clients.clear();
        }
        try { serverSocket.close(); } catch (IOException ignored) {}
        System.out.println("Сервер остановлен");
    }

    public static void main(String[] args) {
         try {
        //Устанавливаем кодировку для консоли
        System.setOut(new PrintStream(System.out, true, "UTF-8"));
        System.setErr(new PrintStream(System.err, true, "UTF-8"));
        
        Server server = new Server(PORT);
        server.run();
    } catch (UnsupportedEncodingException e) {  
        e.printStackTrace();
    } catch (IOException e) {                    
        System.err.println("Server startup error: " + e.getMessage());
    }
    }

}