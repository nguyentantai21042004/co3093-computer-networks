package Peer.src.models;
import lombok.Data;

@Data
public class PieceStatus {
    private int index;
    private String hash;
    private String status;

    // Constructor mặc định không tham số (Jackson yêu cầu để khởi tạo)
    public PieceStatus() {
    }

    // Constructor có tham số
    public PieceStatus(int index, String hash, String status) {
        this.index = index;
        this.hash = hash;
        this.status = status;
    }

    // Getters và Setters
    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
