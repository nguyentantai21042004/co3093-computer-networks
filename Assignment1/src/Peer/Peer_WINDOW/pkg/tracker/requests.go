package tracker

import (
	"fmt"

	"github.com/nguyentantai21042004/CO3093-Computer-Networks-HK241/pkg/models"
)

// UploadFile sends a file upload request to the tracker.
func (tc *TrackerClient) UploadFile(req models.UploadRequest) (models.UploadResponse, error) {
	var resp models.UploadResponse
	err := tc.SendRequest("POST", UploadEndpoint, req, &resp)
	if err != nil {
		return models.UploadResponse{}, fmt.Errorf("failed to upload file: %v", err)
	}
	return resp, nil
}

// GetFiles retrieves the list of files from the tracker.
func (tc *TrackerClient) GetAllFiles() (models.FilesResponse, error) {
	var resp models.FilesResponse
	err := tc.SendRequest("GET", FilesEndpoint, nil, &resp)
	if err != nil {
		return models.FilesResponse{}, fmt.Errorf("failed to fetch files: %v", err)
	}
	return resp, nil
}

// GetPeers retrieves the list of peers from the tracker.
func (tc *TrackerClient) GetAllPeers() (models.PeersResponse, error) {
	var resp models.PeersResponse
	err := tc.SendRequest("GET", PeersEndpoint, nil, &resp)
	if err != nil {
		return models.PeersResponse{}, fmt.Errorf("failed to fetch peers: %v", err)
	}
	return resp, nil
}

// GetFilePeers retrieves peers for a specific torrent file.
func (tc *TrackerClient) GetFilePeers(req models.FilePeersRequest) (models.PeersResponse, error) {
	var resp models.PeersResponse
	err := tc.SendRequest("GE", ScrapeEndpoint, req, &resp)
	if err != nil {
		return models.PeersResponse{}, fmt.Errorf("failed to fetch file peers: %v", err)
	}
	return resp, nil
}

// MarkFileComplete notifies the tracker that a peer has completed a file.
func (tc *TrackerClient) SendStatus(req models.CompleteRequest) (models.CompleteResponse, error) {
	var resp models.CompleteResponse
	err := tc.SendRequest("POST", StatusEndpoint, req, &resp)
	if err != nil {
		return models.CompleteResponse{}, fmt.Errorf("failed to mark file as complete: %v", err)
	}
	return resp, nil
}

// GetFileDetails retrieves the details of a specific file using its hash_id.
func (tc *TrackerClient) GetFileDetails(hashID string) (models.FileDetailsResponse, error) {
	var resp models.FileDetailsResponse
	// Construct the full URL with hashID injected into the FileDetailsEndpoint
	endpoint := fmt.Sprintf(FileDetailsEndpoint, hashID)

	// Send the GET request to the tracker without a request body
	err := tc.SendRequest("GET", endpoint, nil, &resp)
	if err != nil {
		return models.FileDetailsResponse{}, fmt.Errorf("failed to fetch file details: %v", err)
	}
	return resp, nil
}
