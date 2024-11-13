package Tracker.requets;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UploadRequet {
    @JsonProperty("file_name")
    private String fileName;

    @JsonProperty("file_size")
    private long fileSize;

    @JsonProperty("hash_id")
    private String hashID;

    @JsonProperty("torrent_string")
    private String torrentString;

    @JsonProperty("ip_address")
    private String ipAddress;

    @JsonProperty("server_port")
    private int serverPort;

    @JsonProperty("status")
    private String status;

    @JsonProperty("total_pieces")
    private int totalPieces;
}