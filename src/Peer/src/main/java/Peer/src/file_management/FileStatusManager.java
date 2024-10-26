package Peer.src.file_management;

import Peer.src.models.FileStatus;
import Peer.src.models.PieceStatus;
import Peer.src.utils.BEValue;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import Peer.src.utils.*;

public class FileStatusManager {
    // Hàm đọc danh sách FileStatus từ file JSON
    public static List<FileStatus> readFileStatus(String fileStatusPath) throws IOException {
        File file = new File(fileStatusPath);
        if (!file.exists() || file.length() == 0) {
            throw new FileNotFoundException("file_status.json không tồn tại hoặc rỗng.");
        }

        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(file, new TypeReference<List<FileStatus>>() {});
    }

    // Hàm cập nhật trạng thái của một mảnh (piece) trong file_status.json
    public static void updatePieceStatus(String fileName, int pieceIndex, String newStatus) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        File fileStatusFile = new File("src/main/java/Peer/file_status/file_status.json");

        // Đọc file_status.json hiện tại
        List<FileStatus> fileStatusList = mapper.readValue(fileStatusFile, new TypeReference<List<FileStatus>>() {});

        // Duyệt qua danh sách FileStatus và cập nhật trạng thái của mảnh có pieceIndex tương ứng
        for (FileStatus fileStatus : fileStatusList) {
            if (fileStatus.file_name.equals(fileName)) {
                for (PieceStatus piece : fileStatus.pieces) {
                    if (piece.getIndex() == pieceIndex) {
                        piece.setStatus(newStatus);
                        System.out.println("Cập nhật trạng thái của piece " + pieceIndex + " thành " + newStatus);
                        break;
                    }
                }
            }
        }

        // Ghi lại file_status.json sau khi cập nhật
        mapper.writeValue(fileStatusFile, fileStatusList);
    }


    // Hàm trích xuất danh sách các piece hashes từ thông tin torrent
    public static List<String> extractPieceHashes(Map<String, BEValue> info) throws Exception {
        // Giả sử rằng thông tin về hash của các pieces nằm trong key "pieces" dưới dạng chuỗi băm
        BEValue piecesValue = info.get("pieces");
        List<String> pieceHashes = new ArrayList<>();
        if (piecesValue != null) {
            // Chuỗi băm của các piece được nối liền nhau trong một chuỗi, cần chia nhỏ thành từng phần băm
            String pieces = piecesValue.getString();
            for (int i = 0; i < pieces.length(); i += 20) {
                pieceHashes.add(pieces.substring(i, Math.min(i + 20, pieces.length())));
            }
        }
        return pieceHashes;
    }


    // Hàm trích xuất tổng kích thước file từ thông tin torrent
    public static long extractFileLength(Map<String, BEValue> info) throws Exception {
        // Giả sử rằng thông tin về chiều dài file nằm trong key "length"
        BEValue lengthValue = info.get("length");
        if (lengthValue != null) {
            return lengthValue.getLong();
        }
        return 0;
    }

    // Hàm trích xuất kích thước của mỗi piece
    public static int extractPieceLength(Map<String, BEValue> info) throws Exception {
        // Giả sử rằng thông tin về độ dài mỗi piece nằm trong key "piece length"
        BEValue pieceLengthValue = info.get("piece length");
        if (pieceLengthValue != null) {
            return pieceLengthValue.getInt();
        }
        return 0;
    }


    // Hàm trích xuất tên file từ thông tin torrent
    public static String extractFileName(Map<String, BEValue> info) throws Exception {
        // Giả sử rằng thông tin về tên file nằm trong key "name"
        BEValue nameValue = info.get("name");
        if (nameValue != null) {
            return nameValue.getString();
        }
        return "unknown_file";
    }

    // Hàm tính toán số byte đã tải
    public static long calculateDownloaded(String fileName, String fileStatusPath) throws IOException {
        List<FileStatus> fileStatusList = readFileStatus(fileStatusPath);
        long downloaded = 0;

        // Tìm FileStatus dựa trên fileName và tính toán số byte đã tải
        for (FileStatus fileStatus : fileStatusList) {
            if (fileStatus.file_name.equals(fileName)) {
                for (PieceStatus piece : fileStatus.pieces) {
                    if (piece.getStatus().equals("downloaded")) {
                        downloaded += fileStatus.piece_length;
                    }
                }
                break;
            }
        }
        return downloaded;
    }

    // Hàm tính toán số byte đã upload
    public static long calculateUploaded(String fileName, String fileStatusPath) throws IOException {
        // Giả định rằng bạn sẽ tính toán dựa trên trạng thái upload của các pieces
        return 0L;
    }
    public static String computeInfoHash(Map<String, BEValue> info) throws NoSuchAlgorithmException, IOException {
        // Mã hóa phần "info" dưới dạng B-encoded
        byte[] encodedInfo = BEncoder.bencode(info).array();

        if (encodedInfo.length == 0) {
            throw new IOException("Failed to B-encode the 'info' section.");
        }

        // Sử dụng thuật toán SHA-1 để băm phần thông tin "info"
        MessageDigest sha1Digest = MessageDigest.getInstance("SHA-1");
        byte[] infoHashBytes = sha1Digest.digest(encodedInfo);

        // Chuyển đổi giá trị băm thành chuỗi hex
        StringBuilder infoHashHex = new StringBuilder();
        for (byte b : infoHashBytes) {
            infoHashHex.append(String.format("%02x", b));
        }

        return infoHashHex.toString();
    }

    public static void createFileStatus(String outputPath, String fileName, long fileLength, int pieceLength, List<String> pieceHashes, boolean isUploader) throws IOException, NoSuchAlgorithmException {
        File fileStatusFile = new File(outputPath);
        ObjectMapper mapper = new ObjectMapper();
        List<FileStatus> fileStatusList;

        // Kiểm tra nếu file_status.json đã tồn tại, nếu không thì khởi tạo danh sách mới
        if (fileStatusFile.exists() && fileStatusFile.length() > 0) {
            fileStatusList = mapper.readValue(fileStatusFile, new TypeReference<List<FileStatus>>() {});
        } else {
            fileStatusList = new ArrayList<>();
        }

        // Tạo Map<String, BEValue> để lưu thông tin "info" cần thiết
        Map<String, BEValue> info = new HashMap<>();
        info.put("name", new BEValue(fileName));
        info.put("length", new BEValue(fileLength));
        info.put("piece length", new BEValue(pieceLength));

        // Chuyển đổi danh sách pieceHashes thành một chuỗi nối
        StringBuilder piecesConcatenated = new StringBuilder();
        for (String hash : pieceHashes) {
            piecesConcatenated.append(hash);
        }
        info.put("pieces", new BEValue(piecesConcatenated.toString()));

        // Tính toán giá trị hash_id từ phần "info" sử dụng hàm computeInfoHash
        String hashId = computeInfoHash(info);
        System.out.println("hash_id tính toán được: " + hashId);

        // Kiểm tra nếu FileStatus đã tồn tại, nếu có thì cập nhật trạng thái các piece
        boolean fileExists = false;
        for (FileStatus fileStatus : fileStatusList) {
            if (fileStatus.file_name.equals(fileName)) {
                fileExists = true;
                fileStatus.hash_id = hashId; // Cập nhật hash_id
                for (PieceStatus piece : fileStatus.pieces) {
                    piece.setStatus(isUploader ? "downloaded" : "pending");
                }
                break;
            }
        }

        // Nếu file chưa tồn tại, thêm file mới vào danh sách
        if (!fileExists) {
            FileStatus newFileStatus = new FileStatus();
            newFileStatus.file_name = fileName;
            newFileStatus.file_length = fileLength;
            newFileStatus.piece_length = pieceLength;
            newFileStatus.pieces = new ArrayList<>();
            newFileStatus.hash_id = hashId; // Gán hash_id mới

            String initialStatus = isUploader ? "downloaded" : "pending";
            for (int i = 0; i < pieceHashes.size(); i++) {
                newFileStatus.pieces.add(new PieceStatus(i, pieceHashes.get(i), initialStatus));
            }

            fileStatusList.add(newFileStatus);
        }

        // Ghi file_status.json với danh sách đã cập nhật
        mapper.writeValue(fileStatusFile, fileStatusList);
        System.out.println("file_status.json created/updated successfully.");
    }

    public static String getBitfieldFromStatus(String fileName, String outputPath) throws IOException {
        File fileStatusFile = new File(outputPath);
        ObjectMapper mapper = new ObjectMapper();
        List<FileStatus> fileStatusList;

        // Đọc nội dung file_status.json
        if (fileStatusFile.exists()) {
            fileStatusList = mapper.readValue(fileStatusFile, new TypeReference<List<FileStatus>>() {});
        } else {
            throw new FileNotFoundException("file_status.json not found.");
        }

        // Tìm file trong danh sách file_status
        FileStatus targetFileStatus = null;
        for (FileStatus fileStatus : fileStatusList) {
            if (fileStatus.file_name.equals(fileName)) {
                targetFileStatus = fileStatus;
                break;
            }
        }

        if (targetFileStatus == null) {
            throw new IllegalArgumentException("File not found in file_status.json.");
        }

        // Tạo bitfield từ trạng thái các piece
        StringBuilder bitfield = new StringBuilder();
        for (PieceStatus piece : targetFileStatus.pieces) {
            if (piece.getStatus().equals("downloaded")) {
                bitfield.append("1");
            } else {
                bitfield.append("0");
            }
        }

        return bitfield.toString();
    }

}