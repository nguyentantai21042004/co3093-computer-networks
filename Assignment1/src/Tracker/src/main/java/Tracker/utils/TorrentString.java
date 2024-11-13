package Tracker.utils;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class TorrentString {
    /**
     * Mã hóa chuỗi torrent_string thành định dạng gọn hơn.
     * 
     * @param torrentString Chuỗi cần mã hóa
     * @return Chuỗi đã được mã hóa bằng Base64
     */
    public static String encode(String torrentString) {
        return Base64.getEncoder()
                .encodeToString(torrentString.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Giải mã chuỗi torrent_string từ định dạng Base64 về chuỗi gốc.
     * 
     * @param encodedString Chuỗi mã hóa cần giải mã
     * @return Chuỗi torrent_string gốc
     */
    public static String decode(String encodedString) {
        byte[] decodedBytes = Base64.getDecoder().decode(encodedString);
        return new String(decodedBytes, StandardCharsets.UTF_8);
    }
}
