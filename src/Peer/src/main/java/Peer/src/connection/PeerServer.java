package Peer.src.connection;

import Peer.src.file_management.DownloadFile;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class PeerServer {
    private String ipAddress; // Địa chỉ IP của máy chủ
    private int port; // Cổng để server lắng nghe
    private boolean isRunning; // Cờ để kiểm soát trạng thái server
    private ServerSocket serverSocket; // ServerSocket để lắng nghe

    // Constructor chỉ nhận IP và Port
    public PeerServer(String ipAddress, int port) {
        this.ipAddress = ipAddress;
        this.port = port;
        this.isRunning = false;
    }

    // Hàm để khởi tạo server P2P với IP và Port
    public void startServer() {
        try {
            serverSocket = new ServerSocket(port, 50, InetAddress.getByName(ipAddress));
            isRunning = true;
            System.out.println("Peer listening on IP: " + ipAddress + " Port: " + port);

            while (isRunning) {
                Socket clientSocket = serverSocket.accept();
                new Thread(new PeerHandler(clientSocket)).start();  // Không truyền thông tin file, sẽ lấy từ request
            }

        } catch (IOException e) {
            System.out.println("Error starting peer server: " + e.getMessage());
        } finally {
            stopServer();  // Đảm bảo dừng server khi gặp lỗi hoặc kết thúc
        }
    }

    // Hàm để dừng server
    public void stopServer() {
        if (isRunning) {
            isRunning = false;
            try {
                if (serverSocket != null && !serverSocket.isClosed()) {
                    serverSocket.close(); // Đóng server socket
                    System.out.println("Server stopped.");
                }
            } catch (IOException e) {
                System.out.println("Error stopping server: " + e.getMessage());
            }
        }
    }

    // Xử lý request từ các peer khác
    public static class PeerHandler implements Runnable {
        private Socket clientSocket;

        public PeerHandler(Socket clientSocket) {
            this.clientSocket = clientSocket;
        }

        @Override
        public void run() {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                 PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {

                String request = in.readLine();
                if (request.startsWith("REQUEST_PIECE")) {
                    // Parse thông tin từ request
                    String[] requestParams = request.split(" ");
                    int pieceIndex = Integer.parseInt(requestParams[1]);
                    String fileName = requestParams[2];
                    int pieceLength = Integer.parseInt(requestParams[3]);
                    long fileLength = Long.parseLong(requestParams[4]);

                    // Đọc piece từ file
                    byte[] pieceData = DownloadFile.readPieceFromFile(fileName, pieceIndex, pieceLength, fileLength);

                    // Gửi dữ liệu cho peer khác
                    out.println("PIECE_DATA");
                    out.write(new String(pieceData));
                    out.println("END_PIECE");

                    System.out.println("Sent piece " + pieceIndex + " from file " + fileName + " to peer.");
                }

            } catch (IOException e) {
                System.out.println("Error handling peer request: " + e.getMessage());
            }
        }
    }
}