package torrent

import (
	"crypto/sha1"
	"encoding/base64"
	"encoding/binary"
	"fmt"
	"io"
	"net"
	"os"

	"github.com/nguyentantai21042004/CO3093-Computer-Networks-HK241/pkg/models"
)

// Define colors
const (
	Reset  = "\033[0m"
	Red    = "\033[31m"
	Green  = "\033[32m"
	Yellow = "\033[33m"
	Cyan   = "\033[36m"
)

// Log with colors
func logInfo(message string) {
	fmt.Println(Cyan + "[INFO] " + message + Reset)
}

func logSuccess(message string) {
	fmt.Println(Green + "[SUCCESS] " + message + Reset)
}

func logError(message string) {
	fmt.Println(Red + "[ERROR] " + message + Reset)
}

func logWarning(message string) {
	fmt.Println(Yellow + "[WARNING] " + message + Reset)
}

func DownloadFileFromPeers(torrent *models.TorrentFile, status *models.FileStatus, outputFilename string, peerAddresses []string) error {
	logInfo("Starting file download...")
	file, err := os.Create(outputFilename)
	if err != nil {
		logError(fmt.Sprintf("Failed to create output file: %v", err))
		return err
	}
	defer file.Close()

	allPiecesAvailable := true // Track if all pieces were successfully downloaded

	for i := range status.Pieces {
		if status.Pieces[i].Status {
			fmt.Printf("[Client] Skipping already downloaded piece %d\n", i)
			continue // Skip already downloaded pieces
		}

		pieceDownloaded := false // Track if this piece was downloaded successfully

		// Try each peer in turn
		for _, peerAddr := range peerAddresses {
			conn, err := net.Dial("tcp", peerAddr)
			if err != nil {
				logError(fmt.Sprintf("Error connecting to peer %s: %v", peerAddr, err))
				continue // Skip this peer and try the next one
			}
			defer conn.Close()
			logInfo(fmt.Sprintf("Connected to peer %s", peerAddr))

			// Send the file ID length and file ID to the server
			fileID := []byte(status.FileHashID)
			fileIDLength := uint32(len(fileID))
			if err := binary.Write(conn, binary.BigEndian, fileIDLength); err != nil {
				logError(fmt.Sprintf("[Client] Error sending file ID length: %v", err))
				return err
			}
			if _, err := conn.Write(fileID); err != nil {
				logError(fmt.Sprintf("[Client] Error sending file ID: %v", err))
				return err
			}

			// Send request for piece index
			if err := binary.Write(conn, binary.BigEndian, uint32(i)); err != nil {
				logError(fmt.Sprintf("[Client] Error requesting piece %d: %v", i, err))
				return err
			}

			// Read piece size
			var pieceSize uint32
			if err := binary.Read(conn, binary.BigEndian, &pieceSize); err != nil {
				logError(fmt.Sprintf("[Client] Error reading piece size for piece %d: %v", i, err))
				return err
			}

			// Check if the piece is unavailable
			if pieceSize == 0 {
				logWarning(fmt.Sprintf("[Client] Piece %d unavailable from peer %s", i, peerAddr))
				continue // Try the next peer
			}

			// Read the piece data
			piece := make([]byte, pieceSize)
			if _, err := io.ReadFull(conn, piece); err != nil {
				logError(fmt.Sprintf("[Client] Error reading data for piece %d: %v", i, err))
				continue // Skip this peer and try the next one
			}

			// Verify piece hash
			hash := sha1.Sum(piece)
			expectedHash := status.Pieces[i].Hash
			if base64.StdEncoding.EncodeToString(hash[:]) != expectedHash {
				logWarning(fmt.Sprintf("[Client] Hash mismatch for piece %d", i))
				continue // Skip this peer and try the next one
			}
			logInfo(fmt.Sprintf("[Client] Verified hash for piece %d", i))

			// Write piece data to file
			if _, err := file.WriteAt(piece, int64(i)*int64(torrent.PieceSize)); err != nil {
				logError(fmt.Sprintf("[Client] Error writing piece %d to file: %v", i, err))
				return err
			}

			// Update and save status
			status.Pieces[i].Status = true
			if err := SaveFileStatus(status); err != nil {
				logError(fmt.Sprintf("[Client] Error saving file status after downloading piece %d: %v", i, err))
				return err
			}
			logInfo(fmt.Sprintf("[Client] Saved status for downloaded piece %d", i))

			pieceDownloaded = true
			break // Successfully downloaded piece from a peer, move on to the next piece
		}

		// Check if the piece was downloaded successfully
		if !pieceDownloaded {
			logWarning(fmt.Sprintf("[Client] Could not download piece %d from any peer", i))
			allPiecesAvailable = false
		}
	}

	// Log if the file is incomplete
	if !allPiecesAvailable {
		logWarning("[Client] Warning: File download incomplete; some pieces are missing.")
	} else {
		logSuccess("[Client] File download complete.")
	}
	return nil
}
