package pl.edu.agh.kis.pz1;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Set;

/**
 * The MainClient class is responsible for establishing a client connection to a server,
 * managing the communication channel, and handling I/O operations via non-blocking sockets.
 * The client can interact with a server over a TCP connection, sending and receiving data.
 * This class uses Java NIO (Non-blocking I/O) to handle socket communication asynchronously,
 * and a selector to multiplex I/O operations on multiple channels.
 */
public class MainClient {
    private static final String HOST = "localhost";
    private static final int PORT = 9999;
    private static boolean stopFlag = false; // Flaga do kontrolowania pętli



    /**
     * The main method that initializes the client connection and sets up the selector for non-blocking I/O.
     * It attempts to open a connection to the server and calls the `runClient` method to handle communication.
     *
     * @param args Command-line arguments (not used).
     */
    public static void main(String[] args) {
        // try with resources zbey sie samo pozamykalo
        try (SocketChannel clientChannel = SocketChannel.open();
             Selector selector = Selector.open()) {

            runClient(clientChannel, selector);

        } catch (IOException e) {
            System.err.println("Blad w watku wejscia: " + e.getMessage());
        }
    }

    /**
     * Establishes a non-blocking connection to the server, configures I/O multiplexing with a selector,
     * and starts a new thread to handle user input. The method enters a loop where it handles server
     * communication (connect, read) using the selector.
     *
     * @param clientChannel The client socket channel used to communicate with the server.
     * @param selector The selector used for multiplexing non-blocking I/O operations.
     * @throws IOException If an I/O error occurs during the client connection or communication.
     */
    public static void runClient(SocketChannel clientChannel, Selector selector) throws IOException {
        clientChannel.configureBlocking(false);
        clientChannel.connect(new InetSocketAddress(HOST, PORT));
        clientChannel.register(selector, SelectionKey.OP_CONNECT);

        Thread inputThread = new Thread(() -> handleInput(clientChannel));  // watek dow prowadznaiua danych
        inputThread.start();

        ByteBuffer readBuffer = ByteBuffer.allocate(1024);

        while (!stopFlag) {
            selector.select();
            Set<SelectionKey> keys = selector.selectedKeys();
            Iterator<SelectionKey> it = keys.iterator();

            while (it.hasNext()) {
                SelectionKey key = it.next();
                it.remove();

                if (key.isConnectable()) {
                    handleConnect(clientChannel, selector);
                } else if (key.isReadable()) {
                    handleRead(clientChannel, readBuffer);
                }
            }
        }
    }

    /**
     * Handles user input from the command line and sends the entered commands to the server.
     * The input is sent asynchronously by writing the data to the client channel.
     *
     * @param clientChannel The client socket channel used to send data to the server.
     */
    public static void handleInput(SocketChannel clientChannel) {
        try (Scanner scanner = new Scanner(System.in)) {
            while (true) {
                String line = scanner.nextLine();
                synchronized (clientChannel) {
                    ByteBuffer writeBuffer = ByteBuffer.wrap((line + "\n").getBytes(StandardCharsets.UTF_8));
                    clientChannel.write(writeBuffer);
                }
            }
        } catch (IOException e) {
            System.err.println("Blad podczas wysylania komendy: " + e.getMessage());
        }
    }

    /**
     * Handles the connection process for the client socket channel.
     * This method checks if the connection to the server has been successfully established.
     * If successful, it registers the channel for read operations with the selector.
     * If the connection fails, an error message is printed.
     *
     * @param clientChannel The client socket channel that is used to communicate with the server.
     * @param selector The selector used for multiplexing non-blocking I/O operations.
     * @throws IOException If an I/O error occurs while handling the connection.
     */
    public static void handleConnect(SocketChannel clientChannel, Selector selector) throws IOException {
        if (clientChannel.finishConnect()) {
            System.out.println("Polaczono z serwerem: " + clientChannel.getRemoteAddress());
            clientChannel.register(selector, SelectionKey.OP_READ);
        } else {
            System.err.println("Nie udalo się polaczyc z serwerem");
        }
    }

    /**
     * Reads data from the server via the client socket channel.
     * The method clears the read buffer, attempts to read from the server,
     * and if the server has closed the connection, it prints a message and closes the client channel.
     * If data is successfully read, it converts the byte buffer to a string and prints the server's message.
     *
     * @param clientChannel The client socket channel used to read data from the server.
     * @param readBuffer The buffer that stores the data read from the server.
     * @throws IOException If an I/O error occurs during the reading process.
     */
    public static void handleRead(SocketChannel clientChannel, ByteBuffer readBuffer) throws IOException {
        readBuffer.clear();
        int bytesRead = clientChannel.read(readBuffer);
        if (bytesRead == -1) {
            System.out.println("Serwer zamknal polaczenie");
            clientChannel.close();
        } else {
            readBuffer.flip();
            byte[] data = new byte[readBuffer.remaining()];
            readBuffer.get(data);
            String message = new String(data, StandardCharsets.UTF_8);
            if (!message.trim().isBlank()) {
                System.out.print("\n## OD SERWERA: " + message + "\n");
            }
        }
    }

    /**
     * Stops the client by setting the stopFlag to true.
     * This flag is checked during the client loop to determine when to exit the program.
     */
    public static void stopClient() {
        stopFlag = true;
    }
}
