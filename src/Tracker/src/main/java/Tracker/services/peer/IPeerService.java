package Tracker.services.peer;

import Tracker.dtos.UploadRequestDTO;

public interface IPeerService {
    void savePeerInfor(UploadRequestDTO request);
}
