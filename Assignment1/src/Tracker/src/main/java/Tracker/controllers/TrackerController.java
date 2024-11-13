package Tracker.controllers;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import Tracker.models.File;
import Tracker.models.Peer;
import Tracker.requets.AnnouceRequest;
import Tracker.requets.CompleteRequest;
import Tracker.requets.StatusRequest;
import Tracker.requets.UploadRequet;
import Tracker.responses.ApiResponse;
import lombok.AllArgsConstructor;
import Tracker.services.IFileService;
import Tracker.services.IPeerService;
import Tracker.responses.FileResponse;
import Tracker.responses.PeerResponse;
import Tracker.responses.PeerStatus;
import Tracker.responses.TorrentResponse;
import Tracker.utils.TorrentString;

@RestController
@RequestMapping("/tracker/api/v1")
@AllArgsConstructor
public class TrackerController {

        private final IPeerService peerService;

        private final IFileService fileService;

        // Endpoint: POST /upload
        @PostMapping("/upload")
        public ResponseEntity<ApiResponse> uploadFile(
                        @RequestBody UploadRequet uploadRequet,
                        BindingResult result) {
                try {
                        if (result.hasErrors()) {
                                List<String> errorMessages = result.getFieldErrors()
                                                .stream()
                                                .map(FieldError::getDefaultMessage)
                                                .toList();
                                return ResponseEntity.badRequest().body(ApiResponse.builder()
                                                .message("Some thing wrong in your input")
                                                .data(errorMessages)
                                                .status(HttpStatus.BAD_REQUEST)
                                                .build());
                        }

                        File file = fileService.saveFile(uploadRequet);

                        Peer peer = peerService.savePeer(uploadRequet);

                        return ResponseEntity.ok(ApiResponse.builder()
                                        .message("File uploaded successfully.")
                                        .data(TorrentResponse.builder()
                                                        .file(FileResponse.fromFile(file))
                                                        .peers(List.of(PeerResponse.fromPeer(peer)))
                                                        .build())
                                        .status(HttpStatus.OK)
                                        .build());
                } catch (Exception e) {
                        return ResponseEntity.badRequest().body(ApiResponse.builder()
                                        .message("Some thing wrong in your input")
                                        .data(e.getMessage())
                                        .status(HttpStatus.BAD_REQUEST)
                                        .build());
                }
        }

        // Endpoint: GET /files
        @GetMapping("/files")
        public ResponseEntity<ApiResponse> getFiles() {
                List<File> files = fileService.getFiles();
                return ResponseEntity.ok(ApiResponse.builder()
                                .message("Files fetched successfully.")
                                .data(files.stream().map(FileResponse::fromFile).toList())
                                .status(HttpStatus.OK)
                                .build());
        }

        // Endpoint: GET /peers
        @GetMapping("/peers")
        public ResponseEntity<ApiResponse> getPeers() {
                List<Peer> peers = peerService.getPeers();
                return ResponseEntity.ok(ApiResponse.builder()
                                .message("Peers fetched successfully.")
                                .data(peers.stream().map(PeerResponse::fromPeer).toList())
                                .status(HttpStatus.OK)
                                .build());
        }

        // Endpoint: GET /files/peers
        @GetMapping("/files/peers")
        public ResponseEntity<ApiResponse> annouceRequest(
                        @RequestBody AnnouceRequest annouceRequest,
                        BindingResult result) {
                try {
                        if (result.hasErrors()) {
                                List<String> errorMessages = result.getFieldErrors()
                                                .stream()
                                                .map(FieldError::getDefaultMessage)
                                                .toList();
                                return ResponseEntity.badRequest().body(ApiResponse.builder()
                                                .message("Some thing wrong in your input")
                                                .data(errorMessages)
                                                .status(HttpStatus.BAD_REQUEST)
                                                .build());
                        }

                        String hashID = TorrentString.encode(annouceRequest.getTorrentString());
                        List<Peer> peers = peerService.getPeersByHashID(hashID);

                        return ResponseEntity.ok(ApiResponse.builder()
                                        .message("Peers fetched successfully.")
                                        .data(peers.stream().map(PeerResponse::fromPeer).toList())
                                        .status(HttpStatus.OK)
                                        .build());
                } catch (Exception e) {
                        return ResponseEntity.badRequest().body(ApiResponse.builder()
                                        .message("Some thing wrong in your input")
                                        .data(e.getMessage())
                                        .status(HttpStatus.BAD_REQUEST)
                                        .build());
                }
        }

        // Endpoint: GET /files/status
        @GetMapping("/files/status")
        public ResponseEntity<ApiResponse> getFilesStatus(
                        @RequestBody StatusRequest statusRequest,
                        BindingResult result) {
                try {
                        if (result.hasErrors()) {
                                List<String> errorMessages = result.getFieldErrors()
                                                .stream()
                                                .map(FieldError::getDefaultMessage)
                                                .toList();
                                return ResponseEntity.badRequest().body(ApiResponse.builder()
                                                .message("Some thing wrong in your input")
                                                .data(errorMessages)
                                                .status(HttpStatus.BAD_REQUEST)
                                                .build());
                        }

                        String hashID = TorrentString.encode(statusRequest.getTorrentString());
                        Map<String, Integer> peerStatus = peerService.getPeerStatusMap(hashID);

                        PeerStatus peerStatusResponse = PeerStatus.builder()
                                        .hashID(hashID)
                                        .sender(peerStatus.get("SENDER"))
                                        .lecturer(peerStatus.get("LECTURE"))
                                        .completed(peerStatus.get("COMPLETED"))
                                        .build();

                        return ResponseEntity.ok(ApiResponse.builder()
                                        .message("Peers fetched successfully.")
                                        .data(peerStatusResponse)
                                        .status(HttpStatus.OK)
                                        .build());
                } catch (Exception e) {
                        return ResponseEntity.badRequest().body(ApiResponse.builder()
                                        .message("Some thing wrong in your input")
                                        .data(e.getMessage())
                                        .status(HttpStatus.BAD_REQUEST)
                                        .build());
                }
        }

        // Endpoint: POST /files/complete
        @PostMapping("/files/complete")
        public ResponseEntity<ApiResponse> completeRequest(
                        @RequestBody CompleteRequest completeRequest,
                        BindingResult result) {
                try {
                        if (result.hasErrors()) {
                                List<String> errorMessages = result.getFieldErrors()
                                                .stream()
                                                .map(FieldError::getDefaultMessage)
                                                .toList();
                                return ResponseEntity.badRequest().body(ApiResponse.builder()
                                                .message("Some thing wrong in your input")
                                                .data(errorMessages)
                                                .status(HttpStatus.BAD_REQUEST)
                                                .build());
                        }

                        String hashID = TorrentString.encode(completeRequest.getTorrentString());
                        peerService.completeRequest(hashID, completeRequest.getIpAddress(), completeRequest.getPort(),
                                        completeRequest.getCompletedPieces(), completeRequest.getTotalPieces());

                        return ResponseEntity.ok(ApiResponse.builder()
                                        .message("Peer completed successfully.")
                                        .data(null)
                                        .status(HttpStatus.OK)
                                        .build());
                } catch (Exception e) {
                        return ResponseEntity.badRequest().body(ApiResponse.builder()
                                        .message("Some thing wrong in your input")
                                        .data(e.getMessage())
                                        .status(HttpStatus.BAD_REQUEST)
                                        .build());
                }
        }
}