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
		fmt.Printf("Server listening on %s\n", addr)
	} else {
		fmt.Println("Failed to start server:", err)
		return
	}

	defer listener.Close()

	for {
		conn, err := listener.Accept()
		if err != nil {
			fmt.Println("Error accepting connection:", err)
			continue
		}
		go handleConnection(conn)
	}
}

func handleConnection(conn net.Conn) {
	defer conn.Close()

	fmt.Println("[Server] Connection accepted from", conn.RemoteAddr())

	// Read file ID length
	var fileIDLength uint32
	if err := binary.Read(conn, binary.BigEndian, &fileIDLength); err != nil {
		// fmt.Println("[Server] Error reading file ID length:", err)
		return
	}
	fmt.Printf("[Server] File ID length received: %d\n", fileIDLength)

	// Read file ID
	fileID := make([]byte, fileIDLength)
	if _, err := conn.Read(fileID); err != nil {
		// fmt.Println("[Server] Error reading file ID:", err)
		return
	}
	fileIDStr := string(fileID)
	fmt.Printf("[Server] File ID received: %s\n", fileIDStr)

	// Sanitize the fileHashID to ensure it's a valid filename
	sanitizedFileID := utils.SanitizeFileName(fileIDStr) // Sanitize the file ID

	// Locate torrent file and status based on the sanitized file ID
	torrentFilePath := filepath.Join("data", sanitizedFileID+".bencode")
	// fmt.Printf("[Server] Looking for torrent file at %s\n", torrentFilePath)
	torrent, err := DecodeTorrentFile(torrentFilePath)
	if err != nil {
		fmt.Println("[Server] Failed to decode torrent file:", err)
		return
	}

	statusFilePath := filepath.Join("data", sanitizedFileID+"_status.json")
	status, err := LoadFileStatus(statusFilePath)
	if err != nil {
		fmt.Println("[Server] Failed to load file status:", err)
		return
	}

	// Request specific piece index from client
	var pieceIndex uint32
	if err = binary.Read(conn, binary.BigEndian, &pieceIndex); err != nil {
		fmt.Println("[Server] Error reading piece index:", err)
		return
	}
	fmt.Printf("[Server] Piece index requested: %d\n", pieceIndex)

	// Validate piece index and check if the piece is available
	if int(pieceIndex) >= len(status.Pieces) || !status.Pieces[pieceIndex].Status {
		fmt.Printf("[Server] Piece %d is unavailable, notifying client.\n", pieceIndex)
		if err = binary.Write(conn, binary.BigEndian, uint32(0)); err != nil {
			fmt.Println("[Server] Error sending unavailable piece notice:", err)
		}
		return
	}

	// Open file and serve the requested piece
	file, err := os.Open(torrent.Filename)
	if err != nil {
		fmt.Println("[Server] Failed to open file:", err)
		return
	}
	defer file.Close()

	file.Seek(int64(pieceIndex)*int64(torrent.PieceSize), io.SeekStart)
	buffer := make([]byte, torrent.PieceSize)
	n, err := file.Read(buffer)
	if err != nil && err != io.EOF {
		fmt.Println("[Server] Error reading file:", err)
		return
	}

	// Send piece size and data
	if err = binary.Write(conn, binary.BigEndian, uint32(n)); err != nil {
		fmt.Println("[Server] Error sending piece size:", err)
		return
	}
	if _, err = conn.Write(buffer[:n]); err != nil {
		fmt.Println("[Server] Error sending piece data:", err)
		return
	}

	fmt.Printf("[Server] Sent piece %d of size %d bytes\n", pieceIndex, n)
}
