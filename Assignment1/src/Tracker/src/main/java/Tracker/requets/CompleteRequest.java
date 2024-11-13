package Tracker.requets;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.fasterxml.jackson.annotation.JsonProperty;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CompleteRequest {
    @JsonProperty("torrent_string")
    private String torrentString;

    @JsonProperty("ip_address")
    private String ipAddress;

    @JsonProperty("port")
    private int port;

    @JsonProperty("completed_pieces")
    private int completedPieces;

    @JsonProperty("total_pieces")
    private int totalPieces;
}
