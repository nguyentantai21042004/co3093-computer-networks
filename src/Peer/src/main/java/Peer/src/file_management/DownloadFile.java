package Peer.src.file_management;

import Peer.src.connection.PeerClient;
import Peer.src.models.FileStatus;
import Peer.src.models.PeerInfo;
import Peer.src.models.PieceStatus;
import Peer.src.models.PeerInfor;

import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.List;

import static Peer.src.file_management.FileStatusManager.readFileStatus;

public class DownloadFile {

    // Đọc một mảnh (piece) từ file gốc
    public static byte[] readPieceFromFile(String fileName, int pieceIndex, int pieceLength, long fileLength) throws IOException {
        String filePath = "src/main/java/Peer/downloaded_files" + "/" + fileName;

        RandomAccessFile file = new RandomAccessFile(filePath, "r");  // Mở file gốc để đọc
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

    public static void downloadFileByHashId(String hashIdOrFileName) {
        try {
            // Đọc file_status.json để lấy thông tin file dựa trên hash_id hoặc file_name
            System.out.println("Đang đọc file_status.json...");
            List<FileStatus> fileStatusList = readFileStatus("src/main/java/Peer/file_status/file_status.json");
            System.out.println("Danh sách file_status đọc được: " + fileStatusList);

            // Lọc file dựa trên hash_id hoặc file_name, kiểm tra null
            FileStatus fileStatus = fileStatusList.stream()
                    .filter(f -> (f.hash_id != null && f.hash_id.equals(hashIdOrFileName)) ||
                            (f.file_name != null && f.file_name.equals(hashIdOrFileName)))
                    .findFirst()
                    .orElse(null);

            if (fileStatus == null) {
                System.out.println("Không tìm thấy thông tin file trong file_status.json.");
                return;
            }

            System.out.println("Thông tin file tìm thấy: " + fileStatus);

            System.out.println("Đang đọc peer_infor.json...");
            ObjectMapper mapper = new ObjectMapper();

            // Đọc toàn bộ JSON thành danh sách các đối tượng PeerInfor
            List<PeerInfor> peerInforList = mapper.readValue(new File("src/main/java/Peer/file_status/peer_info.json"), new TypeReference<List<PeerInfor>>() {});

            PeerInfor matchingPeerInfor = peerInforList.stream()
                    .filter(peerInfor -> hashIdOrFileName.equals(peerInfor.getHash_id()))
                    .findFirst()
                    .orElse(null);

            if (matchingPeerInfor == null) {
                System.out.println("Không tìm thấy thông tin file với hash_id: " + hashIdOrFileName);
                return;
            }

            List<PeerInfo> peerList = matchingPeerInfor.getPeers();  // Truy xuất danh sách PeerInfo từ PeerInfor
            System.out.println("Danh sách peer đọc được: " + peerList);

            // Lấy danh sách các mảnh chưa tải (trạng thái "pending")
            List<PieceStatus> piecesToDownload = fileStatus.pieces.stream()
                    .filter(piece -> "pending".equals(piece.getStatus()))
                    .toList();

            System.out.println("Danh sách các mảnh chưa tải (pending): " + piecesToDownload);

            // Duyệt qua từng mảnh và tải về từ các peer
            for (PieceStatus piece : piecesToDownload) {
                System.out.println("Đang xử lý mảnh: " + piece.getIndex());

                boolean isDownloaded = false;
                for (PeerInfo peer : peerList) {
                    System.out.println("Đang kết nối tới peer: " + peer.ip + ":" + peer.port);

                    // Kết nối tới mỗi peer và yêu cầu tải mảnh
                    PeerClient peerClient = new PeerClient(peer.ip, peer.port);
                    boolean success = peerClient.requestPieceFromPeer(piece.getIndex(), fileStatus.file_name, fileStatus.piece_length, fileStatus.file_length, "src/main/java/Peer/downloaded_files/clone_" + fileStatus.file_name);

                    if (success) {
                        // Cập nhật trạng thái của mảnh thành "downloaded" sau khi tải xong
                        System.out.println("Tải thành công mảnh: " + piece.getIndex());
                        FileStatusManager.updatePieceStatus(fileStatus.file_name, piece.getIndex(), "downloaded");
                        isDownloaded = true;
                        break; // Nếu đã tải thành công, không cần thử tải lại từ peer khác
                    } else {
                        System.out.println("Không thể tải mảnh " + piece.getIndex() + " từ peer: " + peer.ip + ":" + peer.port);
                    }
                }

                if (!isDownloaded) {
                    System.out.println("Không thể tải mảnh " + piece.getIndex());
                }
            }

            System.out.println("Hoàn thành tải các mảnh cho file: " + fileStatus.file_name);

        } catch (IOException e) {
            System.out.println("Lỗi khi tải file: " + e.getMessage());
            e.printStackTrace();
        }
    }


    // Ghi một mảnh (piece) vào file clone
    public static void writePieceToFile(String cloneFileName, byte[] pieceData, int pieceIndex, int pieceLength) throws IOException {
        RandomAccessFile cloneFile = new RandomAccessFile(cloneFileName, "rw");  // Mở file clone để ghi
        cloneFile.seek((long) pieceIndex * pieceLength);  // Đặt con trỏ ghi đúng vị trí của piece
        cloneFile.write(pieceData);
        cloneFile.close();
    }

    // Chuẩn bị dữ liệu từ buffer
    private static ByteBuffer prepareDataFromBuffer(ByteBuffer buffer) {
        final ByteBuffer data = ByteBuffer.allocate(buffer.remaining());
        buffer.mark();
        data.put(buffer);
        data.clear();
        buffer.reset();
        return data;
    }

//    // Hàm tải file từ các peer và ghi vào file clone
//    public static void downloadFile(String fileStatusPath, String originalFileName, String cloneFileName, String peerIP, int peerPort) {
//        try {
//            // Đọc file_status.json
//            List<FileStatus> fileStatusList = readFileStatus(fileStatusPath);
//            FileStatus fileStatus = fileStatusList.get(0); // Giả sử chỉ có một file được quản lý
//
//            // Tạo file rỗng để ghi dữ liệu
//            RandomAccessFile cloneFile = new RandomAccessFile(cloneFileName, "rw");
//            cloneFile.setLength(fileStatus.file_length); // Đặt kích thước file clone bằng với file gốc
//            cloneFile.close();
//
//            // Tạo client để kết nối tới peer khác
//            PeerClient peerClient = new PeerClient(peerIP, peerPort);
//
//            // Lặp qua từng piece và xử lý tải từ peer hoặc từ file gốc
//            for (PieceStatus pieceStatus : fileStatus.pieces) {
//                if ("pending".equals(pieceStatus.getStatus())) {
//                    // Nếu mảnh (piece) đang ở trạng thái "pending", tải từ peer khác
//                    System.out.println("Downloading piece " + pieceStatus.getIndex() + " from peer...");
//
//                    // Sử dụng PeerClient để yêu cầu piece từ peer khác
//                    peerClient.requestPieceFromPeer(pieceStatus.getIndex(), fileStatus.file_name, fileStatus.piece_length, fileStatus.file_length, cloneFileName);
//
//                    // Cập nhật trạng thái của mảnh trong file_status.json
//                    FileStatusManager.updatePieceStatus(pieceStatus.getIndex(), "downloaded");
//                    System.out.println("Piece " + pieceStatus.getIndex() + " đã được ghi và cập nhật trạng thái.");
//                } else {
//                    // Nếu mảnh đã được tải ("downloaded"), đọc từ file gốc và ghi vào file clone
//                    byte[] pieceData = readPieceFromFile(originalFileName, pieceStatus.getIndex(), fileStatus.piece_length, fileStatus.file_length);
//                    writePieceToFile(cloneFileName, pieceData, pieceStatus.getIndex(), fileStatus.piece_length);
//                    System.out.println("Piece " + pieceStatus.getIndex() + " đã được ghi từ file gốc.");
//                }
//            }
//
//            System.out.println("File đã được tái tạo thành công: " + cloneFileName);
//
//        } catch (Exception e) {
//            System.err.println("Đã xảy ra lỗi: " + e.getMessage());
//            e.printStackTrace();
//        }
//    }
}
