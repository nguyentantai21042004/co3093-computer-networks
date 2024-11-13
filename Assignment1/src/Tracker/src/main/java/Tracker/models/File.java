package Tracker.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.time.Instant;

@Data
@Document(collection = "files")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class File {
    @Id
    private String id; // MongoDB-generated ID

    @JsonProperty("hash_id")
    private String hashID; // Unique hash identifier for the file

    @JsonProperty("file_name")
    private String fileName; // Tên file

    @JsonProperty("file_size")
    private long fileSize; // Kích thước file

    @JsonProperty("torrent_string")
    private String torrentString; // Unique torrent string

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSX", timezone = "UTC")
    @JsonProperty("created_at")
    private Instant createdAt;

}
