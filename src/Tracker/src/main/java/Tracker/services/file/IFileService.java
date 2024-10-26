package Tracker.services.file;

import Tracker.dtos.UploadRequestDTO;

public interface IFileService {
    boolean saveFile(UploadRequestDTO request);
}
