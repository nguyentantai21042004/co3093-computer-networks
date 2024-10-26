package Tracker.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@AllArgsConstructor // Lombok sẽ tạo constructor có tất cả các tham số
@Document(collection = "peers")
public class PeerInfo {
    @Id
    private String id;

    private String peerId;

    private String infoHash;

    private String ipAddress;

    private int port;

    private long uploaded;

    private long downloaded;

    private long left;

    private String lastUpdate;

    public PeerInfo() {

    }
}
