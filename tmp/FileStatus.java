package Peer;

import java.util.List;

class FileStatus {
    public String file_name;
    public long file_size;
    public int piece_length;
    public List<FileStatusCreator.PieceStatus> pieces;

    public FileStatus(String file_name, long file_size, int piece_length, List<FileStatusCreator.PieceStatus> pieces) {
        this.file_name = file_name;
        this.file_size = file_size;
        this.piece_length = piece_length;
        this.pieces = pieces;
    }
}