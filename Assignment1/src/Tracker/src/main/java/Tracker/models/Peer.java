package Tracker.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import com.fasterxml.jackson.annotation.JsonProperty;

@Data
@Document(collection = "peers")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Peer {
    @Id
    private String id; // MongoDB-generated ID

    @JsonProperty("hash_id")
    private String hashID; // Hash identifier for the associated file

    @JsonProperty("ip_address")
    private String ipAddress; // Địa chỉ IP của peer

    @JsonProperty("port")
    private int port; // Port của peer

    @JsonProperty("completed_pieces")
    private int completedPieces; // Số mảnh mà peer đã tải xong

    @JsonProperty("total_pieces")
    private int totalPieces; // Tổng số mảnh của file

    @JsonProperty("status")
    private String status; // "SENDER" hoặc "LECTURE" để biểu thị trạng thái của peer
}
