package Peer.src.file_management;

import Peer.src.utils.BDecoder;
import Peer.src.utils.BEValue;
import Peer.src.utils.BEncoder;

import java.io.*;
import java.net.URI;
import java.util.*;

import static Peer.src.utils.HashCalculator.hashFile;

public class TorrentFileManager {
    public static Map<String, BEValue> readTorrentFile(File torrentFile) throws Exception {
        try (FileInputStream fis = new FileInputStream(torrentFile)) {
            // Sử dụng BDecoder để giải mã dữ liệu từ file .torrent
            return BDecoder.bdecode(fis).getMap();
        }
    }

    public static void writeTorrentFile(byte[] torrentData, String fileName) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(fileName)) {
            fos.write(torrentData);
            System.out.println("Torrent file created successfully: " + fileName);
        }
    }

    public static byte[] encodeTorrent(Map<String, BEValue> torrentInfo) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        BEncoder.bencode(new BEValue(torrentInfo), baos);
        return baos.toByteArray();
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
}
