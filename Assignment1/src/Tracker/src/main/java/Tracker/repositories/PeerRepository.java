package Tracker.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;
import Tracker.models.Peer;

public interface PeerRepository extends MongoRepository<Peer, String> {
    Peer findByIpAddressAndHashID(String ipAddress, String hashID);

    Peer findByHashIDAndIpAddressAndPort(String hashID, String ipAddress, int port);

    List<Peer> findByHashID(String hashID);
}