package Tracker.services;

import Tracker.models.Peer;
import Tracker.requets.UploadRequet;
import java.util.List;
import java.util.Map;

import org.springframework.data.util.Pair;

public interface IPeerService {
    Peer savePeer(UploadRequet uploadRequet) throws Exception;

    List<Peer> getPeers();

    List<Peer> getPeersByHashID(String hashID);

    Pair<Integer, Integer> getPeerStatus(String hashID);

    Map<String, Integer> getPeerStatusMap(String hashID);

    void completeRequest(String hashID, String ipAddress, int port, int completedPieces, int totalPieces)
            throws Exception;
}
