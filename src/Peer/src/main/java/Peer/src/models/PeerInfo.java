package Peer.src.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PeerInfo {
    public String peer_id;
    public String ip;
    public int port;
    public String status;

    // Constructor mặc định (Jackson cần cái này để khởi tạo đối tượng)
    public PeerInfo() {
    }

    // Constructor có tham số
    public PeerInfo(String peer_id, String ip, int port, String status) {
        this.peer_id = peer_id;
        this.ip = ip;
        this.port = port;
        this.status = status;
    }

    // Getters và setters (nếu cần)
}
