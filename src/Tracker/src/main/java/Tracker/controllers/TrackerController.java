package Tracker.controllers;

import Tracker.dtos.UploadRequestDTO;
import Tracker.responses.ApiResponse;
import Tracker.services.file.IFileService;
import Tracker.services.peer.IPeerService;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping("/upload")
public class TrackerController {
    private final IFileService fileService;

    private final IPeerService peerService;

    @PostMapping
    public ResponseEntity<?> uploadFile(@RequestBody UploadRequestDTO request) {
        boolean isSave = fileService.saveFile(request);
        if(!isSave)
            return ResponseEntity.status(409)  // HTTP 409 Conflict
                    .body(new ApiResponse("error", "File with the same info hash already exists."));

        peerService.savePeerInfor(request);

        return ResponseEntity.ok(new ApiResponse("success", "File uploaded and peer registered successfully."));
    }
}
