package Peer;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public class FileStatusCreator {

    public static class PieceStatus {
        public int piece_index;
        public String hash;
        public String status;

        public PieceStatus() {}

        @JsonCreator
        public PieceStatus(@JsonProperty("piece_index") int piece_index,
                           @JsonProperty("hash") String hash,
                           @JsonProperty("status") String status) {
            this.piece_index = piece_index;
            this.hash = hash;
            this.status = status;
        }

        public int getPiece_index() {
            return piece_index;
        }

        public void setPiece_index(int piece_index) {
            this.piece_index = piece_index;
        }

        public String getHash() {
            return hash;
        }

        public void setHash(String hash) {
            this.hash = hash;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }

    public static class FileStatus {
        public String file_name;
        public long file_size;
        public int piece_length;
        public List<PieceStatus> pieces;

        public FileStatus() {}

        @JsonCreator
        public FileStatus(@JsonProperty("file_name") String file_name,
                          @JsonProperty("file_size") long file_size,
                          @JsonProperty("piece_length") int piece_length,
                          @JsonProperty("pieces") List<PieceStatus> pieces) {
            this.file_name = file_name;
            this.file_size = file_size;
            this.piece_length = piece_length;
            this.pieces = pieces;
        }

        public String getFile_name() {
            return file_name;
        }

        public void setFile_name(String file_name) {
            this.file_name = file_name;
        }

        public long getFile_size() {
            return file_size;
        }

        public void setFile_size(long file_size) {
            this.file_size = file_size;
        }

        public int getPiece_length() {
            return piece_length;
        }

        public void setPiece_length(int piece_length) {
            this.piece_length = piece_length;
        }

        public List<PieceStatus> getPieces() {
            return pieces;
        }

        public void setPieces(List<PieceStatus> pieces) {
            this.pieces = pieces;
        }
    }

    // Hàm để thêm trạng thái của một file vào file_status.json
    public static void addFileStatus(String torrentFilePath) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        File statusFile = new File("src/main/java/Peer/file_status.json");

        // In nội dung của file .torrent để kiểm tra ký tự không hợp lệ
        File torrentFile = new File(torrentFilePath);
        if (!torrentFile.exists()) {
            System.out.println("File torrent không tồn tại.");
            return;
        }

        System.out.println("File torrent path: " + torrentFilePath);
        System.out.println("File torrent size: " + torrentFile.length() + " bytes");

        // Đọc file và in nội dung
        BufferedReader reader = new BufferedReader(new FileReader(torrentFile));
        String line;
        while ((line = reader.readLine()) != null) {
            System.out.println(line);
        }
        reader.close();

        // Kiểm tra nếu file_status.json không tồn tại
        List<FileStatus> fileStatuses;
        if (!statusFile.exists()) {
            System.out.println("File status.json không tồn tại, tạo mới...");
            fileStatuses = new ArrayList<>();
        } else {
            // Đọc danh sách các file đã lưu từ file_status.json
            try {
                fileStatuses = mapper.readValue(statusFile, new TypeReference<List<FileStatus>>() {});
            } catch (IOException e) {
                System.out.println("Không thể đọc file_status.json, tạo mới...");
                fileStatuses = new ArrayList<>();
            }
        }


            Map<String, Object> torrentData = TorrentFile.readTorrentFile(torrentFilePath);  // Đọc file .torrent
            String fileName = (String) torrentData.get("name");   // Tên file từ file .torrent
            long fileSize = (Long) torrentData.get("length");     // Kích thước file từ file .torrent

            // Kiểm tra và chuyển đổi piece length về Integer nếu cần
            Object pieceLengthObj = torrentData.get("piece length");
            int pieceLength;
            if (pieceLengthObj instanceof Long) {
                pieceLength = ((Long) pieceLengthObj).intValue();  // Chuyển từ Long sang Integer
            } else {
                pieceLength = (Integer) pieceLengthObj;
            }

            // Thay vì ép kiểu thành List<String>, ta sẽ lấy nó như một mảng byte
            byte[] piecesBytes = (byte[]) torrentData.get("pieces");
            List<String> pieceHashes = new ArrayList<>();

            // Xử lý từng mảnh (20 byte cho mỗi hash SHA-1)
            for (int i = 0; i < piecesBytes.length; i += 20) {
                StringBuilder hash = new StringBuilder();
                for (int j = 0; j < 20; j++) {
                    hash.append(String.format("%02x", piecesBytes[i + j] & 0xff));  // Chuyển đổi byte sang hex
                }
                pieceHashes.add(hash.toString());
            }

            // In ra thông tin từ file .torrent
            System.out.println("File name: " + fileName);
            System.out.println("File size: " + fileSize);
            System.out.println("Piece length: " + pieceLength);
            System.out.println("Piece hashes:");
            for (int i = 0; i < pieceHashes.size(); i++) {
                System.out.println("  Piece " + i + " hash: " + pieceHashes.get(i));
            }

            // Tạo danh sách trạng thái của các mảnh
            List<PieceStatus> pieces = new ArrayList<>();
            for (int i = 0; i < pieceHashes.size(); i++) {
                pieces.add(new PieceStatus(i, pieceHashes.get(i), "incomplete"));
            }

            // Thêm thông tin file mới vào danh sách
            FileStatus newFileStatus = new FileStatus(fileName, fileSize, pieceLength, pieces);
            fileStatuses.add(newFileStatus);

            // Ghi lại file_status.json với danh sách file đã được cập nhật
            mapper.writeValue(statusFile, fileStatuses);
            System.out.println("File status.json đã được cập nhật với thông tin của file mới!");

    }

    // Hàm để trích xuất thông tin trạng thái của một file cụ thể từ file_status.json
    public static FileStatus getFileStatus(String fileName) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        File statusFile = new File("src/main/java/Peer/file_status.json");

        if (!statusFile.exists()) {
            throw new IOException("File status.json không tồn tại!");
        }

        // Đọc danh sách các trạng thái file
        List<FileStatus> fileStatuses = mapper.readValue(statusFile, new TypeReference<List<FileStatus>>() {});

        // Tìm file cần trích xuất
        for (FileStatus fileStatus : fileStatuses) {
            if (fileStatus.file_name.equals(fileName)) {
                return fileStatus;
            }
        }

        throw new IOException("Không tìm thấy thông tin trạng thái cho file: " + fileName);
    }
}