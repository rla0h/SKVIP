import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ChatClient {
    public static void main(String[] args) {
        try {
            Socket socket = new Socket("localhost", 8080);
            
            PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in));
            
            Thread receiveThread = new Thread(new MessageReceiver(socket));
            receiveThread.start();
            
            System.out.println("Enter the pin room.");
            
            String message;
            while ((message = consoleReader.readLine()) != null) {
                writer.println(message);
            }
            
            System.out.println("Left the pin room.");
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    static class MessageReceiver extends Thread {
        private Socket socket;
        //String name = getName();
        public MessageReceiver(Socket socket) {
            this.socket = socket;
        }
        
        @Override
        public void run() {
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                
                String message;
                while ((message = reader.readLine()) != null) {
                    System.out.println("Received: " + message);
                }
                
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
