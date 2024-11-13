package Tracker.responses;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import Tracker.models.File;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FileResponse {

    @JsonProperty("hash_id")
    private String hashID;

    @JsonProperty("file_name")
    private String fileName; // Tên file

    @JsonProperty("file_size")
    private long fileSize; // Kích thước file

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSX", timezone = "UTC")
    @JsonProperty("created_at")
    private Instant createdAt;

    public static FileResponse fromFile(File file) {
        return FileResponse.builder()
                .hashID(file.getHashID())
                .fileName(file.getFileName())
                .fileSize(file.getFileSize())
                .createdAt(file.getCreatedAt())
                .build();
    }
}
