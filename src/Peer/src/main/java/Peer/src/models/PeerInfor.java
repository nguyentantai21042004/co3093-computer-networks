package Peer.src.models;

import lombok.Data;
import lombok.Getter;

import java.util.List;

@Data
public class PeerInfor {
    public String file_name;
    public long file_length;
    public String hash_id;
    // Getter cho danh sách các peer
    @Getter
    public List<PeerInfo> peers;  // Danh sách các peer

    // Constructor mặc định
    public PeerInfor() {
    }

    // Constructor có tham số
    public PeerInfor(String file_name, long file_length, String hash_id, List<PeerInfo> peers) {
        this.file_name = file_name;
        this.file_length = file_length;
        this.hash_id = hash_id;
        this.peers = peers;
    }

}
