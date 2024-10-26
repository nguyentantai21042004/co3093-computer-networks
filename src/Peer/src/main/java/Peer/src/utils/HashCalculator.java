package Peer.src.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

import org.apache.commons.codec.digest.DigestUtils;

public class HashCalculator {
    public static String hashFile(File file, int pieceLength) throws IOException {
        try (FileInputStream fis = new FileInputStream(file)) {
            FileChannel channel = fis.getChannel();
            ByteBuffer buffer = ByteBuffer.allocate(pieceLength);
            StringBuilder hashes = new StringBuilder();

            // Đọc file từng phần (pieceLength)
            while (channel.read(buffer) > 0 || buffer.position() > 0) {
                buffer.flip();  // Prepare buffer for reading
                byte[] piece = new byte[buffer.limit()];
                buffer.get(piece);
                hashes.append(DigestUtils.sha1Hex(piece));  // Tính toán hash SHA-1
                buffer.clear();  // Clear buffer for next read
            }

            return hashes.toString();
        }
    }

    public String computeInfoHash(Map<String, BEValue> info) throws NoSuchAlgorithmException, IOException {
        // Mã hóa phần "info" dưới dạng B-encoded
        byte[] encodedInfo = BEncoder.bencode(info).array();

        if (encodedInfo.length == 0) {
            throw new IOException("Failed to B-encode the 'info' section.");
        }

        // Sử dụng thuật toán SHA-1 để băm phần thông tin "info"
        MessageDigest sha1Digest = MessageDigest.getInstance("SHA-1");
        byte[] infoHashBytes = sha1Digest.digest(encodedInfo);

        // Chuyển đổi giá trị băm thành chuỗi hex
        StringBuilder infoHashHex = new StringBuilder();
        for (byte b : infoHashBytes) {
            infoHashHex.append(String.format("%02x", b));
        }

        return infoHashHex.toString();
    }

}
