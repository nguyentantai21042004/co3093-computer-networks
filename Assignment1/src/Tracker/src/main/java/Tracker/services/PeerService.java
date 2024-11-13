package Tracker.services;

import java.util.UUID;

import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import lombok.AllArgsConstructor;
import Tracker.models.Peer;
import Tracker.repositories.PeerRepository;
import Tracker.requets.UploadRequet;
import Tracker.utils.TorrentString;

import java.util.List;
import java.util.Map;

@Service
@AllArgsConstructor
public class PeerService implements IPeerService {

    private final PeerRepository peerRepository;

    @Override
    public Peer savePeer(UploadRequet uploadRequet) throws Exception {
        Peer existingPeer = peerRepository.findByIpAddressAndFileHash(uploadRequet.getIpAddress(),
                uploadRequet.getTorrentString());

        if (existingPeer != null) {
            throw new Exception("Peer already exists in this file");
        }

        String hashID = TorrentString.encode(uploadRequet.getTorrentString());

        Peer newPeer = Peer.builder()
                .peerId(UUID.randomUUID().toString())
                .fileHash(hashID)
                .ipAddress(uploadRequet.getIpAddress())
                .port(uploadRequet.getServerPort())
                .completedPieces(uploadRequet.getTotalPieces())
                .totalPieces(uploadRequet.getTotalPieces())
                .status(uploadRequet.getStatus())
                .build();

        return peerRepository.save(newPeer);
    }

    @Override
    public List<Peer> getPeers() {
        return peerRepository.findAll();
    }

    @Override
    public List<Peer> getPeersByHashID(String hashID) {
        return peerRepository.findByFileHash(hashID);
    }

    @Override
    public Pair<Integer, Integer> getPeerStatus(String hashID) {
        List<Peer> allPeers = peerRepository.findAll();
        Pair<Integer, Integer> peerStatus = Pair.of(0, 0);
        for (Peer peer : allPeers) {
            if (peer.getStatus().equals("SENDER")) {
                peerStatus = Pair.of(peerStatus.getFirst() + 1, peerStatus.getSecond());
            } else if (peer.getStatus().equals("LECTURE")) {
                peerStatus = Pair.of(peerStatus.getFirst(), peerStatus.getSecond() + 1);
            }
        }
        return peerStatus;
    }

    @Override
    public Map<String, Integer> getPeerStatusMap(String hashID) {
        List<Peer> allPeers = peerRepository.findAll();
        int SENDER = 0;
        int LECTURE = 0;
        int COMPLETED = 0;

        for (Peer peer : allPeers) {
            if (peer.getStatus().equals("SENDER")) {
                SENDER++;
            } else if (peer.getStatus().equals("LECTURE")) {
                LECTURE++;
            } else if (peer.getStatus().equals("COMPLETED")) {
                COMPLETED++;
            }
        }
        return Map.of("SENDER", SENDER, "LECTURE", LECTURE, "COMPLETED", COMPLETED);
    }

    @Override
    public void completeRequest(String hashID, String ipAddress, int port, int completedPieces, int totalPieces)
            throws Exception {
        Peer existingPeer = peerRepository.findByFileHashAndIpAddressAndPort(hashID, ipAddress, port);
        if (existingPeer == null) {
            throw new Exception("Peer not found");
        }

        existingPeer.setCompletedPieces(completedPieces);
        existingPeer.setTotalPieces(totalPieces);

        if (existingPeer.getCompletedPieces() == existingPeer.getTotalPieces()) {
            existingPeer.setStatus("COMPLETED");
        }

        peerRepository.save(existingPeer);
    }
}
