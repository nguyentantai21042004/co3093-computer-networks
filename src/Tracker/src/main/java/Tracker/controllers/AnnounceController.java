package Tracker.controllers;

import Tracker.models.PeerInfo;
import Tracker.repositories.PeerInfoRepository;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@AllArgsConstructor
@RequestMapping("/announce")
public class AnnounceController {
    private final PeerInfoRepository peerInfoRepository;

    @GetMapping
    public ResponseEntity<List<PeerInfo>> announce(
            @RequestParam String infoHash,
            @RequestParam String peerId,
            @RequestParam String ipAddress,
            @RequestParam long uploaded,
            @RequestParam long downloaded,
            @RequestParam long left,
            @RequestParam int port) {

        // Tìm peer dựa trên infoHash và peerId
        Optional<PeerInfo> optionalPeer = peerInfoRepository.findByInfoHashAndPeerId(infoHash, peerId);

        PeerInfo peer;
        if (optionalPeer.isPresent()) {
            // Peer đã tồn tại, cập nhật thông tin
            peer = optionalPeer.get();
            peer.setUploaded(uploaded);
            peer.setDownloaded(downloaded);
            peer.setLeft(left);
            peer.setPort(port);
            peer.setIpAddress(ipAddress);  // Cập nhật IP nếu thay đổi
        } else {
            // Tạo một peer mới nếu không tìm thấy trong hệ thống
            peer = new PeerInfo(null, peerId, infoHash, ipAddress, port, uploaded, downloaded, left, null);
        }

        // Lưu hoặc cập nhật peer vào database
        peerInfoRepository.save(peer);

        // Lấy danh sách các peer khác đang chia sẻ cùng file
        List<PeerInfo> peers = peerInfoRepository.findByInfoHash(infoHash);

        // Trả về danh sách các peer hoặc No Content nếu không có peer nào khác
        if (peers.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(peers);
    }
}
