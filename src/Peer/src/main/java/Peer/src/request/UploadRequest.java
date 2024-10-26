package Peer.src.request;
import Peer.src.models.UploadRequestDTO;


import Peer.src.utils.BEValue;
import Peer.src.utils.HashCalculator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.commons.codec.binary.Hex;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static Peer.src.file_management.FileStatusManager.createFileStatus;
import static Peer.src.file_management.TorrentFileManager.*;

public class UploadRequest {
    private static final String TRACKER_BASE_URL = "http://192.168.88.159:8088"; // hoặc "http://192.168.x.x:8088"

    public void uploadFile(String ip, int port, String filePath) {
        try {
            File file = new File(filePath);
            if (!file.exists() || !file.isFile()) {
                System.out.println("File không tồn tại hoặc không phải là file hợp lệ: " + filePath);
                return;
            }

            URI announceURI = new URI("http://192.168.88.159:8088");
            List<List<URI>> announceList = new ArrayList<>();
            List<URI> tier = new ArrayList<>();
            tier.add(announceURI);
            announceList.add(tier);

            String creator = "User (ttorrent)";
            int pieceLength = 4;

            // Xây dựng thông tin torrent
            Map<String, BEValue> torrentInfo = buildTorrentInfo(file, announceURI.toString(), announceList, creator, pieceLength);

            // Mã hóa thông tin torrent thành B-encoded
            byte[] torrentDataBytes = encodeTorrent(torrentInfo);

            // Ghi dữ liệu ra file torrent
            writeTorrentFile(torrentDataBytes, "src/main/java/Peer/torrent_files/mydatafile.torrent");

            // Đường dẫn tới file .torrent cần đọc
            File torrentFile = new File("src/main/java/Peer/torrent_files/mydatafile.torrent");

            try {
                // Đọc và giải mã file torrent
                Map<String, BEValue> torrentDataMap = readTorrentFile(torrentFile);

                // Trích xuất thông tin từ file torrent
                BEValue announceValue = torrentDataMap.get("announce");
                if (announceValue != null) {
                    String announce = announceValue.getString();
                    System.out.println("Announce URL: " + announce);
                }

                BEValue infoValue = torrentDataMap.get("info");
                if (infoValue != null) {
                    Map<String, BEValue> info = infoValue.getMap();

                    BEValue pieceLengthValue = info.get("piece length");
                    int pieceLen = 0;
                    if (pieceLengthValue != null) {
                        pieceLen = pieceLengthValue.getInt();
                        System.out.println("Piece Length: " + pieceLen);
                    }

                    BEValue piecesValue = info.get("pieces");
                    List<String> pieceHashes = new ArrayList<>();
                    if (piecesValue != null) {
                        byte[] pieces = piecesValue.getBytes();
                        int numPieces = pieces.length / 20;
                        System.out.println("Số lượng pieces: " + numPieces);

                        for (int i = 0; i < numPieces; i++) {
                            byte[] pieceHash = Arrays.copyOfRange(pieces, i * 20, (i + 1) * 20);
                            String pieceHashHex = Hex.encodeHexString(pieceHash);
                            pieceHashes.add(pieceHashHex);
                            System.out.println("Piece " + (i + 1) + " Hash (SHA-1): " + pieceHashHex);
                        }
                    }

                    BEValue nameValue = info.get("name");
                    String name = (nameValue != null) ? nameValue.getString() : "unknown";

                    BEValue fileLengthValue = info.get("length");
                    long fileLength = (fileLengthValue != null) ? fileLengthValue.getLong() : 0;

                    // Tạo file_status.json
                    String fileStatusPath = "src/main/java/Peer/file_status/file_status.json";
                    createFileStatus(fileStatusPath, name, fileLength, pieceLen, pieceHashes, true);

                    HashCalculator hashCalculator = new HashCalculator();

                    // Tính toán giá trị info_hash
                    String infoHash = hashCalculator.computeInfoHash(info);
                    System.out.println("Info Hash: " + infoHash);

                    // Tạo đối tượng UploadRequestDTO để gửi yêu cầu
                    UploadRequestDTO requestDTO = new UploadRequestDTO();
                    requestDTO.setInfoHash(infoHash); // Thay thế giá trị hash thực tế
                    requestDTO.setFileName(file.getName());
                    requestDTO.setFileSize(file.length());
                    requestDTO.setPeerId("peer_12345");
                    requestDTO.setIpAddress(ip);
                    requestDTO.setPort(port);
                    requestDTO.setUploaded(0L);
                    requestDTO.setDownloaded(0L);
                    requestDTO.setLeft(file.length());
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
                    requestDTO.setCreationDate(LocalDateTime.now());
                    requestDTO.setLastUpdate(LocalDateTime.now());

                    // Chuyển đổi requestDTO thành JSON
                    ObjectMapper objectMapper = new ObjectMapper();
                    objectMapper.registerModule(new JavaTimeModule());
                    String requestBody = objectMapper.writeValueAsString(requestDTO);

                    HttpClient client = HttpClient.newBuilder()
                            .version(HttpClient.Version.HTTP_1_1)
                            .connectTimeout(Duration.ofSeconds(30))
                            .build();

                    HttpRequest request = HttpRequest.newBuilder()
                            .uri(URI.create(TRACKER_BASE_URL + "/upload"))
                            .timeout(Duration.ofSeconds(20))
                            .header("Content-Type", "application/json")
                            .POST(HttpRequest.BodyPublishers.ofString(requestBody, StandardCharsets.UTF_8))
                            .build();

                    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                    // Xử lý response từ Tracker
                    if (response.statusCode() == 200) {
                        System.out.println("Upload thành công: " + response.body());
                    } else if (response.statusCode() == 409) {
                        System.out.println("Conflict: File đã tồn tại - " + response.body());
                    } else {
                        System.out.println("Lỗi khi upload file: " + response.statusCode() + " - " + response.body());
                    }
                }
            } catch (Exception e) {
                System.err.println("Lỗi trong quá trình đọc file torrent: " + e.getMessage());
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

}