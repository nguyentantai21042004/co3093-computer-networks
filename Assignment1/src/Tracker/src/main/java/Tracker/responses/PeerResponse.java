package Tracker.responses;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import Tracker.models.Peer;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PeerResponse {
    @JsonProperty("ip_address")
    private String ipAddress; // Địa chỉ IP của peer

    @JsonProperty("port")
    private int port; // Port của peer

    @JsonProperty("status")
    private String status; // "SENDER" hoặc "LECTURE" để biểu thị trạng thái của peer

    public static PeerResponse fromPeer(Peer peer) {
        return PeerResponse.builder()
                .ipAddress(peer.getIpAddress())
                .port(peer.getPort())
                .status(peer.getStatus())
                .build();
    }
}
