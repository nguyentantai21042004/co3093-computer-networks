package Tracker.services.peer;

import Tracker.dtos.UploadRequestDTO;
import Tracker.models.PeerInfo;
import Tracker.repositories.PeerInfoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PeerService implements IPeerService{
    private final PeerInfoRepository peerInfoRepository;

    @Override
    public void savePeerInfor(UploadRequestDTO request) {
        // Save peer information
        PeerInfo peerInfo = new PeerInfo();
        peerInfo.setPeerId(request.getPeerId());
        peerInfo.setInfoHash(request.getInfoHash());
        peerInfo.setIpAddress(request.getIpAddress());
        peerInfo.setPort(request.getPort());
        peerInfo.setUploaded(request.getUploaded());
        peerInfo.setDownloaded(request.getDownloaded());
        peerInfo.setLeft(request.getLeft());
        peerInfo.setLastUpdate(String.valueOf(request.getLastUpdate()));

        peerInfoRepository.save(peerInfo);
    }
}
