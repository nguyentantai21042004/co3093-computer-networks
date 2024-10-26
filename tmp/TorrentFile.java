package Peer;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.nio.charset.StandardCharsets;

public class TorrentFile {
    private static final int PIECE_SIZE = 4;

    // Hàm tính toán SHA-1 cho một chuỗi dữ liệu
    public static String sha1(String data) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        byte[] hashInBytes = md.digest(data.getBytes());

        // Chuyển đổi byte array thành chuỗi hex
        StringBuilder sb = new StringBuilder();
        for (byte b : hashInBytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    // Hàm chia file thành các khối và tính toán băm SHA-1 cho từng khối
    public static List<byte[]> splitFileIntoPieces(String filePath) throws IOException, NoSuchAlgorithmException {
        List<byte[]> pieceHashes = new ArrayList<>();
        MessageDigest sha1 = MessageDigest.getInstance("SHA-1");

        // Lấy kích thước file để tính toán số lượng mảnh
        long fileSize = Files.size(Paths.get(filePath));

        // Đọc file theo từng khối (piece)
        try (InputStream is = Files.newInputStream(Paths.get(filePath))) {
            byte[] buffer = new byte[PIECE_SIZE];
            int bytesRead;
            int totalBytesRead = 0;

            while ((bytesRead = is.read(buffer)) != -1) {
                totalBytesRead += bytesRead;
                if (totalBytesRead > fileSize) {
                    break; // Dừng lại nếu đã đọc quá kích thước file
                }

                if (bytesRead < PIECE_SIZE) {
                    // Nếu là khối cuối và nhỏ hơn PIECE_SIZE, chỉ tính băm cho phần dữ liệu thực
                    sha1.update(Arrays.copyOf(buffer, bytesRead));
                } else {
                    sha1.update(buffer);
                }
                pieceHashes.add(sha1.digest());
                sha1.reset(); // Reset lại đối tượng MessageDigest sau mỗi lần tính băm
            }
        }

        return pieceHashes;
    }


    // Hàm đọc file .torrent và giải mã nội dung Bencode
    // Hàm đọc file .torrent và giải mã nội dung Bencode
    public static Map<String, Object> readTorrentFile(String torrentFilePath) throws IOException {
        FileInputStream fis = new FileInputStream(torrentFilePath);
        PushbackInputStream pbis = new PushbackInputStream(fis, 1);
        BencodeReader bencodeReader = new BencodeReader(pbis);

        System.out.println("Bắt đầu đọc file .torrent");

        Object data = bencodeReader.read();

        if (data instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> torrentData = (Map<String, Object>) data;

            Map<String, Object> info = (Map<String, Object>) torrentData.get("info");
            String fileName = (String) info.get("name");
            long fileSize = (Long) info.get("length");
            long pieceLength = (Long) info.get("piece length");

            // Lấy chuỗi các giá trị hash của từng piece
            byte[] pieces = (byte[]) info.get("pieces");
            List<String> pieceHashes = new ArrayList<>();

            // Mỗi giá trị hash có độ dài 20 byte
            for (int i = 0; i < pieces.length; i += 20) {
                byte[] pieceHash = Arrays.copyOfRange(pieces, i, i + 20);
                pieceHashes.add(bytesToHex(pieceHash));  // Chuyển byte[] thành chuỗi hexa
            }

            System.out.println("File name: " + fileName);
            System.out.println("File size: " + fileSize);
            System.out.println("Piece length: " + pieceLength);
            System.out.println("Piece hashes:");
            for (int i = 0; i < pieceHashes.size(); i++) {
                System.out.println("  Piece " + i + " hash: " + pieceHashes.get(i));
            }

            Map<String, Object> result = new HashMap<>();
            result.put("name", fileName);
            result.put("length", fileSize);
            result.put("piece length", pieceLength);
            result.put("pieces", pieceHashes);

            return result;
        } else {
            throw new IOException("Lỗi: Dữ liệu trả về không phải là Map!");
        }
    }




    // Create file torrent
    public static String createTorrentFile(String filePath, String trackerUrl) throws IOException, NoSuchAlgorithmException {
        List<byte[]> pieceHashes = splitFileIntoPieces(filePath); // Lấy các giá trị hash của từng mảnh
        long fileSize = Files.size(Paths.get(filePath)); // Kích thước file

        // Tạo nội dung file .torrent theo định dạng Bencode
        StringBuilder torrentContent = new StringBuilder();
        torrentContent.append("d"); // Bắt đầu từ điển Bencode
        torrentContent.append("8:announce").append(trackerUrl.length()).append(":").append(trackerUrl); // Tracker URL

        // Phần từ điển info
        torrentContent.append("4:info");
        torrentContent.append("d");
        torrentContent.append("6:lengthi").append(fileSize).append("e"); // Độ dài file
        torrentContent.append("4:name").append(filePath.length()).append(":").append(filePath); // Tên file
        torrentContent.append("12:piece lengthi").append(PIECE_SIZE).append("e"); // Kích thước khối

        // Chuỗi nối của tất cả các giá trị băm SHA-1
        torrentContent.append("6:pieces").append(pieceHashes.size() * 20).append(":");  // Mỗi SHA-1 hash có 40 ký tự hexa
        for (byte[] hash : pieceHashes) {
            torrentContent.append(bytesToHex(hash));  // Chuyển byte[] thành chuỗi hexa và ghi vào nội dung file .torrent
        }

        torrentContent.append("e"); // Kết thúc từ điển info
        torrentContent.append("e"); // Kết thúc từ điển Bencode

        // Ghi nội dung vào file .torrent
        // Ghi nội dung vào file .torrent
        try (FileWriter writer = new FileWriter(filePath + ".torrent")) {
            writer.write(torrentContent.toString());
            System.out.println("Torrent content: " + torrentContent.toString()); // In ra nội dung để kiểm tra
        } catch (IOException e) {
            System.out.println("Error writing torrent file: " + e.getMessage());
        }

        return filePath + ".torrent";
    }


    // Chuyển đổi byte array thành chuỗi hexa (hexadecimal)
    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));  // Chuyển byte thành 2 ký tự hexa
        }
        return sb.toString();
    }
}
