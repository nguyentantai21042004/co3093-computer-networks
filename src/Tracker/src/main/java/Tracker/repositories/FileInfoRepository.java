package Tracker.repositories;

import Tracker.models.FileInfo;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface  FileInfoRepository extends MongoRepository<FileInfo, String> {
    FileInfo findByInfoHash(String infoHash);
}
