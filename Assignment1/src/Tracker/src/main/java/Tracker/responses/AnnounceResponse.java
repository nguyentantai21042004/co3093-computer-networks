package Tracker.responses;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AnnounceResponse {
    @JsonProperty("torrent_string")
    private String torrentString;

    @JsonProperty("peers")
    private List<PeerResponse> peers;
}
