package Peer.src.models;

import java.util.List;

import lombok.Data;

@Data
public class FileStatus {
    public String file_name;
    public long file_length;
    public int piece_length;
    public List<PieceStatus> pieces;
    public String hash_id;

    // Constructor mặc định
    public FileStatus() {
    }

    // Constructor có tham số
    public FileStatus(String file_name, long file_length, int piece_length, String hash_id, List<PieceStatus> pieces) {
        this.file_name = file_name;
        this.file_length = file_length;
        this.hash_id = hash_id;
        this.pieces = pieces;
        this.piece_length = piece_length;
    }
}
