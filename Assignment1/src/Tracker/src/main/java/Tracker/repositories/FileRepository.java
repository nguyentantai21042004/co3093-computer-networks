package Tracker.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;

import Tracker.models.File;

public interface FileRepository extends MongoRepository<File, String> {
    File findByHashID(String hashID);
}