package Peer.src.request;

import Peer.src.file_management.PeerFileManager;
import Peer.src.utils.BEValue;
import Peer.src.utils.HashCalculator;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static Peer.src.file_management.FileStatusManager.*;
import static Peer.src.file_management.TorrentFileManager.readTorrentFile;

public class AnnounceRequest {

    private static final String TRACKER_BASE_URL = "http://192.168.88.159:8088"; // URL của Tracker

    private String peerId;

    // Hàm khởi tạo, tạo peer_id duy nhất
    public AnnounceRequest() {
        this.peerId = generatePeerId();
    }

    private String generatePeerId() {
        return UUID.randomUUID().toString().substring(0, 12);
    }

    public void sendAnnounceRequest(String ip, int port, String torrentFilePath) {
        try {
            // Đọc và giải mã file torrent
            File torrentFile = new File(torrentFilePath);
            if (!torrentFile.exists() || !torrentFile.isFile()) {
                System.out.println("File torrent không tồn tại hoặc không hợp lệ: " + torrentFilePath);
                return;
            }

            Map<String, BEValue> torrentDataMap = readTorrentFile(torrentFile);

            // Trích xuất giá trị info_hash
            BEValue infoValue = torrentDataMap.get("info");
            if (infoValue == null) {
                throw new Exception("Không tìm thấy thông tin info_hash trong file torrent.");
            }

            Map<String, BEValue> info = infoValue.getMap();
            HashCalculator hashCalculator = new HashCalculator();
            String infoHash = hashCalculator.computeInfoHash(info);
            System.out.println("Info Hash: " + infoHash);

            // Trích xuất tên file, kích thước file và các piece
            String fileName = extractFileName(info);
            long fileLength = extractFileLength(info);
            int pieceLength = extractPieceLength(info);
            List<String> pieceHashes = extractPieceHashes(info);

            // Cập nhật hoặc tạo file status nếu chưa tồn tại
            String outputPath = "src/main/java/Peer/file_status/file_status.json";
            File statusFile = new File(outputPath);
            if (!statusFile.exists() || statusFile.length() == 0) {
                createFileStatus(outputPath, fileName, fileLength, pieceLength, pieceHashes, false);
            }

            // Tính toán các giá trị uploaded, downloaded và left từ file status
            long uploaded = calculateUploaded(fileName, outputPath);
            long downloaded = calculateDownloaded(fileName, outputPath);
            long left = fileLength - downloaded;

            // Xây dựng URL query parameters
            String url = String.format("%s/announce?infoHash=%s&peerId=%s&ipAddress=%s&uploaded=%d&downloaded=%d&left=%d&port=%d",
                    TRACKER_BASE_URL,
                    infoHash,
                    peerId,
                    ip,
                    uploaded,
                    downloaded,
                    left,
                    port
            );

            // Tạo request HTTP
            HttpClient client = HttpClient.newBuilder()
                    .version(HttpClient.Version.HTTP_1_1)
                    .connectTimeout(Duration.ofSeconds(30))
                    .build();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(url))
                    .timeout(Duration.ofSeconds(20))
                    .header("Content-Type", "application/json")
                    .GET()
                    .build();

            // Gửi request và nhận phản hồi
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            // Xử lý phản hồi từ Tracker
            if (response.statusCode() == 200) {
                System.out.println("Announce thành công: " + response.body());

                // Phân tích phản hồi từ Tracker để lấy danh sách các peer
                ObjectMapper objectMapper = new ObjectMapper();
                List<Map<String, Object>> peersResponse = objectMapper.readValue(response.body(), new TypeReference<List<Map<String, Object>>>() {});

                // Tạo danh sách các peer từ phản hồi
                PeerFileManager peerFileManager = new PeerFileManager();
                List<PeerFileManager.Peer> peers = peerFileManager.getPeerList();

                // Thêm tất cả các peer từ phản hồi vào danh sách
                for (Map<String, Object> peerData : peersResponse) {
                    String peerId = (String) peerData.get("peerId");
                    String peerIp = (String) peerData.get("ipAddress");
                    int peerPort = (int) peerData.get("port");
                    peers = peerFileManager.addPeer(peers, peerId, peerIp, peerPort, "connected");
                }

                // Tạo file peer_info với thông tin về các peer và lưu vào đường dẫn được yêu cầu
                peerFileManager.createPeerInfoFile("src/main/java/Peer/file_status" + "/" + fileName + "_peer_info.json", fileName, fileLength, infoHash, peers);

            } else {
                System.out.println("Lỗi khi gửi announce: " + response.statusCode() + " - " + response.body());
            }
        } catch (URISyntaxException | IOException | InterruptedException e) {
            e.printStackTrace();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
