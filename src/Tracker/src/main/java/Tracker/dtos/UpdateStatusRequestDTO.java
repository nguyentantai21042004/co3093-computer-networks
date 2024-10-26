package Tracker.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UpdateStatusRequestDTO {
    private String peerId;

    private long uploaded;

    private long downloaded;

    private long left;

    private String lastUpdate;
}
