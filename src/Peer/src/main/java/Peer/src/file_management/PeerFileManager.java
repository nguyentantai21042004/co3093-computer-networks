package Peer.src.file_management;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PeerFileManager {

    // Class Peer để lưu thông tin từng peer
    public static class Peer {
        public String peer_id;
        public String ip;
        public int port;
        public String status;

        public Peer() {
            // Default constructor cho Jackson
        }

        public Peer(String peer_id, String ip, int port, String status) {
            this.peer_id = peer_id;
            this.ip = ip;
            this.port = port;
            this.status = status;
        }
    }

    // Class FileInfo để lưu thông tin file và danh sách các peer, bao gồm cả hash_id
    public static class FileInfo {
        public String file_name;
        public long file_length;
        public String hash_id; // Thêm hash_id của file
        public List<Peer> peers;

        public FileInfo() {
            // Default constructor cho Jackson
        }

        public FileInfo(String file_name, long file_length, String hash_id, List<Peer> peers) {
            this.file_name = file_name;
            this.file_length = file_length;
            this.hash_id = hash_id;
            this.peers = peers;
        }
    }

    // Hàm tạo file JSON để lưu thông tin file, hash_id và danh sách các peer
    public void createPeerInfoFile(String filePath, String fileName, long fileLength, String hashId, List<Peer> peers) throws IOException {
        // Tạo đối tượng FileInfo chứa thông tin file và danh sách các peer
        FileInfo fileInfo = new FileInfo(fileName, fileLength, hashId, peers);

        // Tạo thư mục nếu chưa tồn tại
        File file = new File(filePath);
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }

        // Ghi thông tin ra file JSON
        ObjectMapper mapper = new ObjectMapper();
        ObjectWriter writer = mapper.writerWithDefaultPrettyPrinter(); // Để định dạng JSON đẹp hơn
        writer.writeValue(file, fileInfo);

        System.out.println("Peer info file created successfully at " + filePath);
    }

    // Hàm để thêm một peer vào danh sách
    public List<Peer> addPeer(List<Peer> peers, String peerId, String ip, int port, String status) {
        peers.add(new Peer(peerId, ip, port, status));
        return peers;
    }

    // Hàm để lấy danh sách peer hiện tại (khởi tạo trống)
    public List<Peer> getPeerList() {
        return new ArrayList<>();
    }
}
