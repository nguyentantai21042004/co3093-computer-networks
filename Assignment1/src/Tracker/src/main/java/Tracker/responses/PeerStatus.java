package Tracker.responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PeerStatus {
    @JsonProperty("hash_id")
    private String hashID;

    @JsonProperty("sender")
    private int sender;

    @JsonProperty("completed")
    private int completed;

    @JsonProperty("lecturer")
    private int lecturer;

    public static PeerStatus fromMap(Map<String, Integer> peerStatus) {
        return PeerStatus.builder()
                .sender(peerStatus.get("SENDER"))
                .lecturer(peerStatus.get("LECTURE"))
                .completed(peerStatus.get("COMPLETED"))
                .build();
    }
}
