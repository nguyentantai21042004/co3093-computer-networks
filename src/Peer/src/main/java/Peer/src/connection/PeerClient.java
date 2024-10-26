package Peer.src.connection;

import Peer.src.file_management.DownloadFile;

import java.io.*;
import java.net.Socket;

public class PeerClient {
    private String peerIP;
    private int peerPort;

    // Constructor chỉ nhận vào IP và Port
    public PeerClient(String peerIP, int peerPort) {
        this.peerIP = peerIP;
        this.peerPort = peerPort;
    }

    // Hàm gửi yêu cầu request một piece từ peer khác
    public boolean requestPieceFromPeer(int pieceIndex, String fileName, int pieceLength, long fileLength, String cloneFileName) {
        try (Socket socket = new Socket(peerIP, peerPort);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            // Gửi yêu cầu tải piece, kèm theo thông tin file
            out.println("REQUEST_PIECE " + pieceIndex + " " + fileName + " " + pieceLength + " " + fileLength);
            System.out.println("Sent piece request for piece " + pieceIndex + " to peer: " + peerIP);

            // Nhận phản hồi và lưu phần dữ liệu vào file clone
            String response = in.readLine();
            if (response != null && response.startsWith("PIECE_DATA")) {
                byte[] pieceData = receivePieceData(in, pieceLength);
                DownloadFile.writePieceToFile(cloneFileName, pieceData, pieceIndex, pieceLength);
                System.out.println("Downloaded and wrote piece " + pieceIndex + " to " + cloneFileName);
                return true; // Tải thành công
            } else {
                System.out.println("No data received for piece " + pieceIndex);
                return false; // Tải không thành công
            }
        } catch (IOException e) {
            System.out.println("Error requesting piece from peer: " + e.getMessage());
            return false; // Xử lý lỗi khi yêu cầu tải
        }
    }

    private byte[] receivePieceData(BufferedReader in, int pieceLength) throws IOException {
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        char[] buffer = new char[pieceLength];
        int numBytes;
        while ((numBytes = in.read(buffer, 0, pieceLength)) != -1) {
            byteStream.write(new String(buffer, 0, numBytes).getBytes());
            if (numBytes < pieceLength) break; // Dừng khi đọc hết dữ liệu
        }
        return byteStream.toByteArray();
    }
}