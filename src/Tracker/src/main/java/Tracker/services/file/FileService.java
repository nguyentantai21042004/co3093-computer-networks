package Tracker.services.file;

import Tracker.dtos.UploadRequestDTO;
import Tracker.models.FileInfo;
import Tracker.repositories.FileInfoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class FileService implements IFileService{
    private final FileInfoRepository fileInfoRepository;

    @Override
    public boolean saveFile(UploadRequestDTO request) {
        // Check if the file exists
        FileInfo fileInfo = fileInfoRepository.findByInfoHash(request.getInfoHash());

        if (fileInfo != null) {
            return false; // File with hash_id has been existed
        }

        FileInfo newFileInfo = new FileInfo();
        newFileInfo.setInfoHash(request.getInfoHash());
        newFileInfo.setFileName(request.getFileName());
        newFileInfo.setFileSize(request.getFileSize());

        // Get the current time and format it to ISO 8601
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
        String formattedDate = LocalDateTime.now().format(formatter);

        // Set creation date
        newFileInfo.setCreationDate(formattedDate);

        fileInfoRepository.save(newFileInfo);
        return true;
    }
}