package torrent

import (
	"encoding/json"
	"os"
	"path/filepath"

	"github.com/nguyentantai21042004/CO3093-Computer-Networks-HK241/pkg/models"
	"github.com/nguyentantai21042004/CO3093-Computer-Networks-HK241/pkg/utils"
)

// Tạo file trạng thái với thông tin chi tiết cho từng mảnh
func CreateFileStatusForPeer1(torrent *models.TorrentFile) *models.FileStatus {
	var pieces []models.PieceInfo
	for _, hash := range torrent.Hashes {
		pieces = append(pieces, models.PieceInfo{
			Hash:   hash,
			Status: true, // Peer1 đã tải hết tất cả các mảnh, nên đặt true
		})
	}

	fileHashID := utils.CalculateFileHashID(torrent.Hashes) // Tính mã hash ID của file

	return &models.FileStatus{
		Filename:   torrent.Filename,
		PieceSize:  torrent.PieceSize,
		FileHashID: fileHashID, // Lưu mã hash ID của file
		Pieces:     pieces,
	}
}

// Create FileStatus for Peer2 with piece status set to false
func CreateFileStatusForPeer2(torrent *models.TorrentFile) *models.FileStatus {
	var pieces []models.PieceInfo
	for _, hash := range torrent.Hashes {
		pieces = append(pieces, models.PieceInfo{
			Hash:   hash,
			Status: false,
		})
	}

	fileHashID := utils.CalculateFileHashID(torrent.Hashes)

	return &models.FileStatus{
		Filename:   torrent.Filename,
		PieceSize:  torrent.PieceSize,
		FileHashID: fileHashID,
		Pieces:     pieces,
	}
}

// Save the file status to a JSON file
func SaveFileStatus(status *models.FileStatus) error {
	// Create the output directory if it doesn't exist
	outputDir := "data"
	if _, err := os.Stat(outputDir); os.IsNotExist(err) {
		err := os.Mkdir(outputDir, 0755)
		if err != nil {
			return err
		}
	}

	// Sanitize the fileHashID to ensure it's a valid filename
	statusFileID := utils.SanitizeFileName(status.FileHashID)

	// Save the file status with a sanitized fileHashID
	statusFilePath := filepath.Join(outputDir, statusFileID+"_status.json")
	file, err := os.Create(statusFilePath)
	if err != nil {
		return err
	}
	defer file.Close()

	encoder := json.NewEncoder(file)
	return encoder.Encode(status)
}

// Load file status from a JSON file
func LoadFileStatus(path string) (*models.FileStatus, error) {
	file, err := os.Open(path)
	if err != nil {
		return nil, err
	}
	defer file.Close()

	var status models.FileStatus
	err = json.NewDecoder(file).Decode(&status)
	if err != nil {
		return nil, err
	}
	return &status, nil
}
