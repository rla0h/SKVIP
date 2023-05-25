
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

public class ChatServer {
    private static final int MAX_PARTICIPANTS = 3;
    private static Semaphore semaphore;
    private static List<ClientHandler> clients;
    private static List<Socket> waiting;
    public static void main(String[] args) {
        semaphore = new Semaphore(MAX_PARTICIPANTS);
        clients = new ArrayList<>();
        waiting = new ArrayList<>();

        try {
            ServerSocket serverSocket = new ServerSocket(8080);
            System.out.println("Chat server started on port 8080...");
            
            while (true) {
            	Socket clientSocket = serverSocket.accept();
            	
            	if (clients.size() >= MAX_PARTICIPANTS) {
            		waiting.add(clientSocket);
                    System.out.println("The PIN room is full. Waiting for a slot...");
                    PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true);
                    writer.println("The PIN room is currently full. Please try again later.");
                    //clientSocket.close();
                    continue;
                }
            	
            	
        		System.out.println("New client connected.");
                // Client handling thread starts
                ClientHandler clientHandler = new ClientHandler(clientSocket);
                clients.add(clientHandler);
                
                Thread clientThread = new Thread(clientHandler);
                clientThread.start();
            	
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    static class ClientHandler extends Thread {
        private Socket clientSocket;
        private PrintWriter writer;
        
        public ClientHandler(Socket clientSocket) {
            this.clientSocket = clientSocket;
        }
        
        public void run() {
        	String name = getName();
            try {
            	
                semaphore.acquire(); // Acquire the semaphore (enter the chat room)
                
                System.out.println(name+" have entered the chat room.");
                
                writer = new PrintWriter(clientSocket.getOutputStream(), true);
                BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                
                String message;
                while ((message = reader.readLine()) != null) {
                    System.out.println(name+": " + message);
                    
                    // Broadcast the received message to all participants
                    broadcastMessage(message);
                }
                
                semaphore.release(); // Release the semaphore (leave the chat room)
                System.out.println(name+" have left the chat room.");
                clients.remove(this);
				clientSocket.close();
                if (waiting.size()>0) {
                	System.out.println("Waiting now.. " + waiting.size() + "user(s)..");
                	Socket waitSocket = waiting.get(0);
                    // Client handling thread starts
                    ClientHandler clientHandler = new ClientHandler(waitSocket);
                    clients.add(clientHandler);
                    
                    Thread clientThread = new Thread(clientHandler);
                    clientThread.start();
                    waiting.remove(0);
				}
            } catch (IOException | InterruptedException e) {
            	e.printStackTrace();
            }
        }
        
        private void broadcastMessage(String message) {
            for (ClientHandler client : clients) {
            		client.writer.println(message);
            }
        }
    }
}
