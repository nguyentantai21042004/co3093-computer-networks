package Tracker.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;
import Tracker.models.Peer;

public interface PeerRepository extends MongoRepository<Peer, String> {
    Peer findByIpAddressAndFileHash(String ipAddress, String fileHash);

    Peer findByFileHashAndIpAddressAndPort(String fileHash, String ipAddress, int port);

    List<Peer> findByFileHash(String fileHash);
}