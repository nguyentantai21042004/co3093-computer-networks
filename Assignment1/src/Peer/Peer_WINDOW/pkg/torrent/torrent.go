package torrent

import (
	"bytes"
	"crypto/sha1"
	"encoding/base64"
	"fmt"
	"io"
	"os"
	"path/filepath"

	"github.com/jackpal/bencode-go"
	"github.com/nguyentantai21042004/CO3093-Computer-Networks-HK241/pkg/models"
	"github.com/nguyentantai21042004/CO3093-Computer-Networks-HK241/pkg/utils"
)

// Convert the TorrentFile to Bencode format
func ToBencode(torrent *models.TorrentFile) string {
	bencodeStr := "d8:filename" + fmt.Sprintf("%d:%s", len(torrent.Filename), torrent.Filename)
	bencodeStr += fmt.Sprintf("10:piece_sizei%de", torrent.PieceSize)
	bencodeStr += "6:hashes" + "l"
	for _, hash := range torrent.Hashes {
		bencodeStr += fmt.Sprintf("%d:%s", len(hash), hash)
	}
	bencodeStr += "e" + "e"
	return bencodeStr
}

// Create the torrent file and split it into pieces
func CreateTorrentFile(filename string, pieceSize int) (*models.TorrentFile, error) {
	file, err := os.Open(filename)
	if err != nil {
		return nil, err
	}
	defer file.Close()

	var hashes []string
	buffer := make([]byte, pieceSize)

	for {
		n, err := file.Read(buffer)
		if err != nil && err != io.EOF {
			return nil, err
		}
		if n == 0 {
			break
		}

		hash := sha1.Sum(buffer[:n])
		hashes = append(hashes, base64.StdEncoding.EncodeToString(hash[:]))
	}

	return &models.TorrentFile{
		Filename:  filename,
		PieceSize: pieceSize,
		Hashes:    hashes,
	}, nil
}

// Save the torrent file in .bencode format
func SaveTorrentFile(torrent *models.TorrentFile) (string, error) {
	outputDir := "data"
	if _, err := os.Stat(outputDir); os.IsNotExist(err) {
		err := os.Mkdir(outputDir, 0755)
		if err != nil {
			return "", err
		}
	}

	fileHashID := utils.CalculateFileHashID(torrent.Hashes)
	// Sanitize the fileHashID to ensure it's a valid filename
	fileHashID = utils.SanitizeFileName(fileHashID)

	torrentFilePath := filepath.Join(outputDir, fileHashID+".bencode")
	err := os.WriteFile(torrentFilePath, []byte(ToBencode(torrent)), 0644)
	if err != nil {
		return "", fmt.Errorf("Error saving Bencode file: %v", err)
	}
	return fileHashID, nil
}

// Decode a Bencode file
func DecodeTorrentFile(filePath string) (*models.TorrentFile, error) {
	file, err := os.Open(filePath)
	if err != nil {
		return nil, fmt.Errorf("Error opening Bencode file: %v", err)
	}
	defer file.Close()

	// Print the raw content of the file for debugging
	content, err := io.ReadAll(file)
	if err != nil {
		return nil, fmt.Errorf("Error reading file content: %v", err)
	}
	fmt.Printf("Raw Torrent File Content: %s\n", string(content))

	// Now decode the Bencode data
	torrent := &models.TorrentFile{}
	err = bencode.Unmarshal(bytes.NewReader(content), torrent)
	if err != nil {
		return nil, fmt.Errorf("Error decoding torrent: %v", err)
	}
	return torrent, nil
}
