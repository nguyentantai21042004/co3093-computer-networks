package Tracker.services;

import Tracker.models.File;
import Tracker.repositories.FileRepository;
import Tracker.requets.UploadRequet;

import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Service;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class FileService implements IFileService {

    private final FileRepository fileRepository;

    @Override
    public File saveFile(UploadRequet uploadRequet) throws Exception {
        File existingFile = fileRepository.findByHashID(uploadRequet.getHashID());
        if (existingFile != null) {
            throw new Exception("File already exists");
        }

        File newFile = File.builder()
                .hashID(uploadRequet.getHashID())
                .fileName(uploadRequet.getFileName())
                .fileSize(uploadRequet.getFileSize())
                .torrentString(uploadRequet.getTorrentString())
                .createdAt(Instant.now())
                .build();

        return fileRepository.save(newFile);
    }

    @Override
    public List<File> getFiles() {
        return fileRepository.findAll();
    }

    @Override
    public File getFileByHashID(String hashID) {
        return fileRepository.findByHashID(hashID);
    }

    @Override
    public File getFileByID(String id) {
        return fileRepository.findById(id).orElse(null);
    }
}
