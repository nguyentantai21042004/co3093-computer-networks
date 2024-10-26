package Tracker.repositories;

import Tracker.models.PeerInfo;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PeerInfoRepository extends MongoRepository<PeerInfo, String> {
    List<PeerInfo> findByInfoHash(String infoHash);
    PeerInfo findByPeerId(String peerId);
    Optional<PeerInfo> findByInfoHashAndPeerId(String infoHash, String peerId);
}
