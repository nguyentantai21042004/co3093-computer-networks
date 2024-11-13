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
    @JsonProperty("file_name")
    private String fileName; // Tên file

    @JsonProperty("file_size")
    private long fileSize; // Kích thước file

    @JsonProperty("torrent_string")
    private String torrentString; // Unique torrent string

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSX", timezone = "UTC")
    private Instant createdAt;

    public static FileResponse fromFile(File file) {
        return FileResponse.builder()
                .fileName(file.getFileName())
                .fileSize(file.getFileSize())
                .torrentString(file.getTorrentString())
                .createdAt(file.getCreatedAt())
                .build();
    }
}
