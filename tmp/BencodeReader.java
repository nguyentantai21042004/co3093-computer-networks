package Peer;

import java.io.*;
import java.util.*;

public class BencodeReader {

    private final PushbackInputStream input;

    public BencodeReader(PushbackInputStream input) {
        this.input = input;
    }

    // Hàm đọc file và giải mã nội dung
    public Object read() throws IOException {
        int prefix = input.read();
        if (prefix == -1) {
            throw new IOException("End of stream reached unexpectedly.");
        }
        if (prefix == 'd') return readDictionary();
        if (prefix == 'l') return readList();
        if (prefix == 'i') return readInteger();
        if (Character.isDigit(prefix)) return readStringOrBinary(prefix); // Thay đổi ở đây
        throw new IOException("Unknown Bencode prefix: " + (char) prefix);
    }

    private Map<String, Object> readDictionary() throws IOException {
        Map<String, Object> map = new HashMap<>();
        while (true) {
            int prefix = input.read();
            if (prefix == 'e') break; // Kết thúc từ điển
            if (prefix == -1) throw new IOException("Unexpected end of stream while reading dictionary.");
            input.unread(prefix); // Đẩy lại byte vào luồng
            Object key = read();
            Object value = read();
            map.put((String) key, value);
        }
        return map;
    }


    private List<Object> readList() throws IOException {
        List<Object> list = new ArrayList<>();
        while (true) {
            int prefix = input.read();
            if (prefix == 'e') break; // Kết thúc danh sách
            if (prefix == -1) throw new IOException("Unexpected end of stream while reading list.");
            input.unread(prefix); // Đẩy lại byte vào luồng
            list.add(read());
        }
        return list;
    }

    private Long readInteger() throws IOException {
        StringBuilder sb = new StringBuilder();
        while (true) {
            int c = input.read();
            if (c == 'e') break; // Kết thúc số nguyên
            if (c == -1) throw new IOException("Unexpected end of stream while reading integer.");
            sb.append((char) c);
        }
        return Long.parseLong(sb.toString());
    }

    // Phân biệt giữa chuỗi văn bản và chuỗi nhị phân (binary string)
    // Phân biệt giữa chuỗi văn bản và chuỗi nhị phân (binary string)
    private Object readStringOrBinary(int prefix) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append((char) prefix); // Đọc ký tự đầu tiên của độ dài chuỗi
        while (true) {
            int c = input.read();
            if (c == -1) {
                throw new IOException("Unexpected end of stream while reading string length.");
            }
            if (c == ':') break; // Kết thúc phần độ dài chuỗi
            sb.append((char) c);
        }

        String lengthString = sb.toString();
        int length;
        try {
            length = Integer.parseInt(lengthString); // Đọc độ dài chuỗi
        } catch (NumberFormatException e) {
            throw new IOException("Invalid string length in Bencode: " + lengthString);
        }

        // Đọc nội dung chuỗi theo độ dài đã lấy được
        byte[] bytes = new byte[length];
        int bytesRead = input.read(bytes);
        if (bytesRead != length) {
            throw new IOException("Unexpected end of stream while reading string content. Expected " + length + " bytes, but got " + bytesRead + " bytes.");
        }

        // Đảm bảo trả về đúng kiểu dữ liệu cho các phần binary
        return bytes; // Luôn trả về `byte[]` khi đọc xong nội dung
    }

    // Hàm kiểm tra nếu byte[] có thể là chuỗi văn bản
    private boolean isPrintable(byte[] bytes) {
        for (byte b : bytes) {
            if (b < 32 || b > 126) {
                return false; // Nếu có ký tự không in được, coi như chuỗi nhị phân
            }
        }
        return true; // Tất cả ký tự đều in được, coi như chuỗi văn bản
    }

}