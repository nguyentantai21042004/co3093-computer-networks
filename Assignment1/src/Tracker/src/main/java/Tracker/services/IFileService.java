package Tracker.services;

import java.util.List;

import Tracker.models.File;
import Tracker.requets.UploadRequet;

public interface IFileService {
    File saveFile(UploadRequet uploadRequet) throws Exception;

    List<File> getFiles();
}
