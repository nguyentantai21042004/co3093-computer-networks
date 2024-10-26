package Peer;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.*;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.*;

public class PeerApplication {

    // Lớp mô tả cấu trúc của file_status.json
    public static class FileStatus {
        public String file_name;
        public long file_length;
        public int piece_length;
        public List<PieceStatus> pieces;
    }

    public static class PieceStatus {
        public int index;
        public String hash;
        public String status;

        public PieceStatus(int index, String hash, String status) {
            this.index = index;
            this.hash = hash;
            this.status = status;  // ban đầu có thể là "pending"
        }
    }

    // Kích thước của mỗi phần (piece), mặc định là 512KB
    int pieceLength = 4; // 512KB

    // Hàm này dùng để chia file thành các phần (pieces)
    private static ByteBuffer prepareDataFromBuffer(ByteBuffer buffer) {
        final ByteBuffer data = ByteBuffer.allocate(buffer.remaining());
        buffer.mark();
        data.put(buffer);
        data.clear();
        buffer.reset();
        return data;
    }

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

    public static Map<String, BEValue> buildTorrentInfo(File source, String announce, List<List<URI>> announceList, String creator, int pieceLength) throws IOException {
        Map<String, BEValue> torrent = new HashMap<>();
        torrent.put("announce", new BEValue(announce));

        List<BEValue> tiers = new LinkedList<>();
        for (List<URI> tier : announceList) {
            List<BEValue> tierInfo = new LinkedList<>();
            for (URI trackerURI : tier) {
                tierInfo.add(new BEValue(trackerURI.toString()));
            }
            tiers.add(new BEValue(tierInfo));
        }
        torrent.put("announce-list", new BEValue(tiers));

        Map<String, BEValue> info = new TreeMap<>();
        info.put("name", new BEValue(source.getName()));
        info.put("piece length", new BEValue(pieceLength));  // Kích thước mỗi phần (piece length)

        // Thêm trường "length" cho file đơn lẻ
        info.put("length", new BEValue(source.length())); // Kích thước file

        // Gọi hàm hashFile để tính toán hash cho các phần
        info.put("pieces", new BEValue(hashFile(source, pieceLength).getBytes()));

        torrent.put("info", new BEValue(info));
        return torrent;
    }

    public static byte[] encodeTorrent(Map<String, BEValue> torrentInfo) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        BEncoder.bencode(new BEValue(torrentInfo), baos);
        return baos.toByteArray();
    }

    public static void writeTorrentFile(byte[] torrentData, String fileName) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(fileName)) {
            fos.write(torrentData);
            System.out.println("Torrent file created successfully: " + fileName);
        }
    }

    public static Map<String, BEValue> readTorrentFile(File torrentFile) throws Exception {
        try (FileInputStream fis = new FileInputStream(torrentFile)) {
            // Sử dụng BDecoder để giải mã dữ liệu từ file .torrent
            return BDecoder.bdecode(fis).getMap();
        }
    }

    // Hàm tạo file_status.json
    public static void createFileStatus(String outputPath, String fileName, long fileLength, int pieceLength, List<String> pieceHashes) throws IOException {
        FileStatus fileStatus = new FileStatus();
        fileStatus.file_name = fileName;
        fileStatus.file_length = fileLength;
        fileStatus.piece_length = pieceLength;
        fileStatus.pieces = new ArrayList<>();

        // Tạo trạng thái cho từng piece
        for (int i = 0; i < pieceHashes.size(); i++) {
            fileStatus.pieces.add(new PieceStatus(i, pieceHashes.get(i), "pending"));
        }

        // Ghi file_status.json ra file
        ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(new File(outputPath), fileStatus);
        System.out.println("file_status.json created successfully.");
    }

    public static void main(String[] args) throws Exception {
       // Thiết lập đường dẫn tới file hoặc thư mục
       File source = new File("src/main/java/Peer/src.txt");

       // Kiểm tra file hoặc thư mục có tồn tại và có thể đọc được
       if (!source.exists() || !source.canRead()) {
           throw new IllegalArgumentException("File hoặc thư mục không tồn tại hoặc không thể đọc được: " + source.getName());
       }

       // Thiết lập URI của tracker
       URI announceURI = new URI("http://tracker.example.com/announce");

       // Thiết lập announce list
       List<List<URI>> announceList = new ArrayList<>();
       List<URI> tier = new ArrayList<>();
       tier.add(announceURI);
       announceList.add(tier);

       // Tên người tạo file torrent
       String creator = "User (ttorrent)";
       int pieceLength = 4; // Đặt lại kích thước mỗi phần

       // Xây dựng thông tin torrent
       Map<String, BEValue> torrentInfo = buildTorrentInfo(source, announceURI.toString(), announceList, creator, pieceLength);

       // Mã hóa thông tin torrent thành B-encoded
       byte[] torrentData = encodeTorrent(torrentInfo);

       // Ghi dữ liệu ra file torrent
       writeTorrentFile(torrentData, "src/main/java/Peer/mydatafile.torrent");


        // Đường dẫn tới file .torrent cần đọc
        File torrentFile = new File("src/main/java/Peer/mydatafile.torrent");

        try {
            // Đọc và giải mã file torrent
            Map<String, BEValue> torrentData = readTorrentFile(torrentFile);

            // Trích xuất thông tin từ file torrent
            BEValue announceValue = torrentData.get("announce");
            if (announceValue != null) {
                String announce = announceValue.getString();
                System.out.println("Announce URL: " + announce);
            }

            BEValue infoValue = torrentData.get("info");
            if (infoValue != null) {
                Map<String, BEValue> info = infoValue.getMap();

                BEValue pieceLengthValue = info.get("piece length");
                int pieceLength = 0;
                if (pieceLengthValue != null) {
                    pieceLength = pieceLengthValue.getInt();
                    System.out.println("Piece Length: " + pieceLength);
                }

                BEValue piecesValue = info.get("pieces");
                List<String> pieceHashes = new ArrayList<>();
                if (piecesValue != null) {
                    byte[] pieces = piecesValue.getBytes();
                    int numPieces = pieces.length / 20;
                    System.out.println("Số lượng pieces: " + numPieces);

                    for (int i = 0; i < numPieces; i++) {
                        byte[] pieceHash = Arrays.copyOfRange(pieces, i * 20, (i + 1) * 20);
                        String pieceHashHex = Hex.encodeHexString(pieceHash);
                        pieceHashes.add(pieceHashHex);
                        System.out.println("Piece " + (i + 1) + " Hash (SHA-1): " + pieceHashHex);
                    }
                }

                BEValue nameValue = info.get("name");
                String name = (nameValue != null) ? nameValue.getString() : "unknown";

                BEValue fileLengthValue = info.get("length");
                long fileLength = (fileLengthValue != null) ? fileLengthValue.getLong() : 0;

                // Tạo file_status.json
                String fileStatusPath = "src/main/java/Peer/file_status.json";
                createFileStatus(fileStatusPath, name, fileLength, pieceLength, pieceHashes);
            }
        } catch (Exception e) {
            System.err.println("Lỗi trong quá trình đọc file torrent: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
