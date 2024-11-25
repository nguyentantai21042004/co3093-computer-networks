package torrent

import (
	"encoding/binary"
	"fmt"
	"io"
	"net"
	"os"
	"path/filepath"

	"github.com/nguyentantai21042004/CO3093-Computer-Networks-HK241/pkg/utils"
)

// Start a server that listens for incoming connections
func StartServer() {
	localIP := utils.GetLocalIP()
	var listener net.Listener
	var err error
	var port int

	port = 12345
	addr := fmt.Sprintf("%s:%d", localIP, port)
	listener, err = net.Listen("tcp", addr)
	if err == nil {
		logInfo(fmt.Sprintf("Server listening on %s", addr))
	} else {
		logError(fmt.Sprintf("Failed to start server: %v", err))
		return
	}

	defer listener.Close()

	for {
		conn, err := listener.Accept()
		if err != nil {
			logError(fmt.Sprintf("Error accepting connection: %v", err))
			continue
		}
		go handleConnection(conn)
	}
}

func handleConnection(conn net.Conn) {
	defer conn.Close()

	logInfo(fmt.Sprintf("[Server] Connection accepted from %s", conn.RemoteAddr()))

	// Read file ID length
	var fileIDLength uint32
	if err := binary.Read(conn, binary.BigEndian, &fileIDLength); err != nil {
		// fmt.Println("[Server] Error reading file ID length:", err)
		return
	}
	logInfo(fmt.Sprintf("[Server] File ID length received: %d", fileIDLength))

	// Read file ID
	fileID := make([]byte, fileIDLength)
	if _, err := conn.Read(fileID); err != nil {
		// fmt.Println("[Server] Error reading file ID:", err)
		return
	}
	fileIDStr := string(fileID)
	logInfo(fmt.Sprintf("[Server] File ID received: %s", fileIDStr))

	// Sanitize the fileHashID to ensure it's a valid filename
	sanitizedFileID := utils.SanitizeFileName(fileIDStr) // Sanitize the file ID

	// Locate torrent file and status based on the sanitized file ID
	torrentFilePath := filepath.Join("data", sanitizedFileID+".bencode")
	// fmt.Printf("[Server] Looking for torrent file at %s\n", torrentFilePath)
	torrent, err := DecodeTorrentFile(torrentFilePath)
	if err != nil {
		logError(fmt.Sprintf("[Server] Failed to decode torrent file: %v", err))
		return
	}

	statusFilePath := filepath.Join("data", sanitizedFileID+"_status.json")
	status, err := LoadFileStatus(statusFilePath)
	if err != nil {
		logError(fmt.Sprintf("[Server] Failed to load file status: %v", err))
		return
	}

	// Request specific piece index from client
	var pieceIndex uint32
	if err = binary.Read(conn, binary.BigEndian, &pieceIndex); err != nil {
		logError(fmt.Sprintf("[Server] Error reading piece index: %v", err))
		return
	}
	logInfo(fmt.Sprintf("[Server] Piece index requested: %d", pieceIndex))

	// Validate piece index and check if the piece is available
	if int(pieceIndex) >= len(status.Pieces) || !status.Pieces[pieceIndex].Status {
		fmt.Printf("[Server] Piece %d is unavailable, notifying client.\n", pieceIndex)
		if err = binary.Write(conn, binary.BigEndian, uint32(0)); err != nil {
			logError(fmt.Sprintf("[Server] Error sending unavailable piece notice: %v", err))
		}
		return
	}

	// Open file and serve the requested piece
	file, err := os.Open(torrent.Filename)
	if err != nil {
		logError(fmt.Sprintf("[Server] Failed to open file: %v", err))
		return
	}
	defer file.Close()

	file.Seek(int64(pieceIndex)*int64(torrent.PieceSize), io.SeekStart)
	buffer := make([]byte, torrent.PieceSize)
	n, err := file.Read(buffer)
	if err != nil && err != io.EOF {
		logError(fmt.Sprintf("[Server] Error reading file: %v", err))
		return
	}

	// Send piece size and data
	if err = binary.Write(conn, binary.BigEndian, uint32(n)); err != nil {
		logError(fmt.Sprintf("[Server] Error sending piece size: %v", err))
		return
	}
	if _, err = conn.Write(buffer[:n]); err != nil {
		logError(fmt.Sprintf("[Server] Error sending piece data: %v", err))
		return
	}

	logInfo(fmt.Sprintf("[Server] Sent piece %d of size %d bytes", pieceIndex, n))
}
