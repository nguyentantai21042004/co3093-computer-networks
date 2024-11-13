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

func DownloadFileFromPeers(torrent *models.TorrentFile, status *models.FileStatus, outputFilename string, peerAddresses []string) error {
	file, err := os.Create(outputFilename)
	if err != nil {
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
				fmt.Printf("[Client] Error connecting to peer %s: %v\n", peerAddr, err)
				continue // Skip this peer and try the next one
			}
			defer conn.Close()
			fmt.Printf("[Client] Connected to peer %s\n", peerAddr)

			// Send the file ID length and file ID to the server
			fileID := []byte(status.FileHashID)
			fileIDLength := uint32(len(fileID))
			if err := binary.Write(conn, binary.BigEndian, fileIDLength); err != nil {
				fmt.Printf("[Client] Error sending file ID length: %v\n", err)
				return err
			}
			if _, err := conn.Write(fileID); err != nil {
				fmt.Printf("[Client] Error sending file ID: %v\n", err)
				return err
			}

			// Send request for piece index
			if err := binary.Write(conn, binary.BigEndian, uint32(i)); err != nil {
				fmt.Printf("[Client] Error requesting piece %d: %v\n", i, err)
				return err
			}

			// Read piece size
			var pieceSize uint32
			if err := binary.Read(conn, binary.BigEndian, &pieceSize); err != nil {
				fmt.Printf("[Client] Error reading piece size for piece %d: %v\n", i, err)
				return err
			}

			// Check if the piece is unavailable
			if pieceSize == 0 {
				fmt.Printf("[Client] Piece %d unavailable from peer %s\n", i, peerAddr)
				continue // Try the next peer
			}

			// Read the piece data
			piece := make([]byte, pieceSize)
			if _, err := io.ReadFull(conn, piece); err != nil {
				fmt.Printf("[Client] Error reading data for piece %d: %v\n", i, err)
				continue // Skip this peer and try the next one
			}

			// Verify piece hash
			hash := sha1.Sum(piece)
			expectedHash := status.Pieces[i].Hash
			if base64.StdEncoding.EncodeToString(hash[:]) != expectedHash {
				fmt.Printf("[Client] Hash mismatch for piece %d\n", i)
				continue // Skip this peer and try the next one
			}
			fmt.Printf("[Client] Verified hash for piece %d\n", i)

			// Write piece data to file
			if _, err := file.WriteAt(piece, int64(i)*int64(torrent.PieceSize)); err != nil {
				fmt.Printf("[Client] Error writing piece %d to file: %v\n", i, err)
				return err
			}

			// Update and save status
			status.Pieces[i].Status = true
			if err := SaveFileStatus(status); err != nil {
				fmt.Printf("[Client] Error saving file status after downloading piece %d: %v\n", i, err)
				return err
			}
			fmt.Printf("[Client] Saved status for downloaded piece %d\n", i)

			pieceDownloaded = true
			break // Successfully downloaded piece from a peer, move on to the next piece
		}

		// Check if the piece was downloaded successfully
		if !pieceDownloaded {
			fmt.Printf("[Client] Could not download piece %d from any peer\n", i)
			allPiecesAvailable = false
		}
	}

	// Log if the file is incomplete
	if !allPiecesAvailable {
		fmt.Println("[Client] Warning: File download incomplete; some pieces are missing.")
	} else {
		fmt.Println("[Client] File download complete.")
	}
	return nil
}
