package Tracker.controllers;

import Tracker.dtos.UpdateStatusRequestDTO;
import Tracker.models.PeerInfo;
import Tracker.repositories.PeerInfoRepository;
import Tracker.responses.ApiResponse;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping("/update-status")
public class StatusController {
    private final PeerInfoRepository peerInfoRepository;

    @PostMapping
    public ResponseEntity<?> updateStatus(@RequestBody UpdateStatusRequestDTO request) {
        PeerInfo peer = peerInfoRepository.findByPeerId(request.getPeerId());
        if (peer != null) {
            peer.setUploaded(request.getUploaded());
            peer.setDownloaded(request.getDownloaded());
            peer.setLeft(request.getLeft());
            peer.setLastUpdate(request.getLastUpdate());
            peerInfoRepository.save(peer);
            return ResponseEntity.ok(new ApiResponse("success", "Peer status updated successfully."));
        } else {
            return ResponseEntity.badRequest().body(new ApiResponse("error", "Peer not found."));
        }
    }
}