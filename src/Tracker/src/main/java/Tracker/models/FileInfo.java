package Tracker.models;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "files")
public class FileInfo {
    @Id
    private String id;

    private String infoHash;

    private String fileName;

    private long fileSize;

    private String creationDate;
}