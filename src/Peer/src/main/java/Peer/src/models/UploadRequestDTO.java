package Peer.src.models;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class UploadRequestDTO {
    private String infoHash;

    private String fileName;

    private long fileSize;

    private String peerId;

    private String ipAddress;

    private int port;

    private long uploaded;

    private long downloaded;

    private long left;

    private LocalDateTime creationDate; // Changed to LocalDateTime

    private LocalDateTime lastUpdate; // Changed to LocalDateTime

    public UploadRequestDTO() {

    }
}