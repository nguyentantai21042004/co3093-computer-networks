package Peer;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.*;
import java.nio.file.*;
import java.util.*;

public class TorrentRebuilder {

    public static class FileStatus {
        public String file_name;
        public long file_length;
        public int piece_length;
        public List<PieceStatus> pieces;
    }

    public static class PieceStatus {
        public int index;
        public String hash;
        public String status;
    }

    // Đọc thông tin từ file_status.json
    public static FileStatus readFileStatus(String fileStatusPath) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(new File(fileStatusPath), FileStatus.class);
    }

    public static byte[] readPieceFromFile(String fileName, int pieceIndex, int pieceLength, long fileLength) throws IOException {
        RandomAccessFile file = new RandomAccessFile(fileName, "r");  // Mở file gốc để đọc
        file.seek((long) pieceIndex * pieceLength);  // Chuyển con trỏ đến vị trí của mảnh cần đọc

        // Tính toán kích thước thực sự của mảnh (piece)
        long remainingBytes = fileLength - (long) pieceIndex * pieceLength;
        int actualPieceLength = (int) Math.max(0, Math.min(pieceLength, remainingBytes));  // Đảm bảo không có giá trị âm

        // Nếu không còn dữ liệu để đọc, trả về mảng rỗng
        if (actualPieceLength <= 0) {
            return new byte[0];
        }

        byte[] pieceData = new byte[actualPieceLength];
        int bytesRead = file.read(pieceData);
        file.close();

        // Nếu bytesRead là -1, có nghĩa là không còn dữ liệu để đọc
        if (bytesRead == -1) {
            throw new EOFException("Reached the end of the file unexpectedly.");
        }

        return pieceData;
    }

    public static void writePieceToFile(String cloneFileName, byte[] pieceData, int pieceIndex, int pieceLength) throws IOException {
        RandomAccessFile cloneFile = new RandomAccessFile(cloneFileName, "rw");  // Mở file clone để ghi
        cloneFile.seek((long) pieceIndex * pieceLength);  // Đặt con trỏ ghi đúng vị trí của piece
        cloneFile.write(pieceData);
        cloneFile.close();
    }

    public static void main(String[] args) {
        String fileStatusPath = "src/main/java/Peer/file_status.json";  // Đường dẫn tới file_status.json
        String originalFileName = "src/main/java/Peer/src.txt";  // File gốc
        String cloneFileName = "src/main/java/Peer/clone.txt";   // File tái tạo

        try {
            // Đọc file_status.json
            FileStatus fileStatus = readFileStatus(fileStatusPath);

            // Tạo file rỗng để ghi dữ liệu
            RandomAccessFile cloneFile = new RandomAccessFile(cloneFileName, "rw");
            cloneFile.setLength(fileStatus.file_length);
            cloneFile.close();

            // Lặp qua từng piece và đọc từ file gốc
            for (PieceStatus pieceStatus : fileStatus.pieces) {
                if ("downloaded".equals(pieceStatus.status)) {
                    // Đọc trực tiếp phần (piece) từ file gốc
                    byte[] pieceData = readPieceFromFile(originalFileName, pieceStatus.index, fileStatus.piece_length, fileStatus.file_length);

                    // Ghi phần đã đọc vào file tái tạo
                    writePieceToFile(cloneFileName, pieceData, pieceStatus.index, fileStatus.piece_length);

                    // In ra thông tin về phần đã ghi
                    System.out.println("Đã ghi mảnh " + pieceStatus.index + " vào file tái tạo, kích thước: " + pieceData.length + " bytes.");
                }
            }

            System.out.println("File đã được tái tạo thành công: " + cloneFileName);

        } catch (Exception e) {
            System.err.println("Đã xảy ra lỗi: " + e.getMessage());
            e.printStackTrace();
        }
    }
}