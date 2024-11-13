package main

import (
	"bufio"
	"fmt"
	"os"
	"path/filepath"
	"strings"
	"time"

	"github.com/nguyentantai21042004/CO3093-Computer-Networks-HK241/pkg/models"
	"github.com/nguyentantai21042004/CO3093-Computer-Networks-HK241/pkg/torrent"
	"github.com/nguyentantai21042004/CO3093-Computer-Networks-HK241/pkg/tracker"
	"github.com/nguyentantai21042004/CO3093-Computer-Networks-HK241/pkg/utils"
)

func main() {
	go torrent.StartServer() // Start server in the background

	trackerURL := "http://172.20.114.189:8088"
	client := tracker.NewTrackerClient(trackerURL, 10*time.Second)

	reader := bufio.NewReader(os.Stdin)
	for {
		fmt.Println("\nOptions:")
		fmt.Println("1. Share a file")
		fmt.Println("2. Download a file")
		fmt.Println("3. Get all files from tracker")
		fmt.Println("4. Get file details from tracker")
		fmt.Println("5. Get all peers from tracker")
		fmt.Println("6. Exit")

		var choice int
		fmt.Print("Choose an option (1-8): ")
		fmt.Scanf("%d\n", &choice)

		switch choice {
		case 1:
			handleShareFile(reader, client)
		case 2:
			handleDownloadFile(client, reader)
		case 3:
			handleGetFiles(client)
		case 4:
			handleGetFileDetails(client)
		case 5:
			handleGetPeers(client)
		case 6:
			fmt.Println("Exiting program...")
			return
		default:
			fmt.Println("Invalid choice, please try again.")
		}
	}
}

func handleShareFile(reader *bufio.Reader, client *tracker.TrackerClient) {
	fmt.Print("Enter the path to the file to share: ")
	filename, _ := reader.ReadString('\n')
	filename = strings.TrimSpace(filename)

	pieceSize := 1024
	torrentFile, err := torrent.CreateTorrentFile(filename, pieceSize)
	if err != nil {
		fmt.Println("Error creating torrent:", err)
		return
	}

	fileHashID, err := torrent.SaveTorrentFile(torrentFile)
	if err != nil {
		fmt.Println("Error saving torrent file:", err)
		return
	}

	status := torrent.CreateFileStatusForPeer1(torrentFile)
	err = torrent.SaveFileStatus(status)
	if err != nil {
		fmt.Println("Error saving file status:", err)
		return
	}

	torrentString := torrent.ToBencode(torrentFile)

	uploadReq := models.UploadRequest{
		FileName:      filename,
		FileSize:      torrentFile.PieceSize * len(torrentFile.Hashes),
		TorrentString: torrentString,
		HashID:        fileHashID,
		IPAddress:     utils.GetLocalIP(),
		ServerPort:    12345,
		Status:        "SENDER",
		TotalPieces:   len(torrentFile.Hashes),
	}

	resp, err := client.UploadFile(uploadReq)
	if err != nil {
		fmt.Println("Error uploading file:", err)
		return
	}
	fmt.Printf("Upload response: %+v\n", resp)

	fmt.Printf("File '%s' is now shared.\n", filename)
}

func handleDownloadFile(client *tracker.TrackerClient, reader *bufio.Reader) {
	// Step 1: Prompt user to enter the hashID
	fmt.Print("Enter the hash ID of the file to fetch details and download: ")
	hashID, _ := reader.ReadString('\n')
	hashID = strings.TrimSpace(hashID)

	// Step 2: Fetch file details for the given hashID
	resp, err := client.GetFileDetails(hashID)
	if err != nil {
		fmt.Println("Error retrieving file details:", err)
		return
	}

	// Print out the fetched file details
	fmt.Printf("File details response for hash ID '%s':\n", hashID)
	fmt.Printf("Status: %s\n", resp.Status)
	fmt.Printf("Message: %s\n", resp.Message)

	// Step 3: Create the data directory if it doesn't exist
	dataDir := "data"
	if _, err := os.Stat(dataDir); os.IsNotExist(err) {
		err := os.Mkdir(dataDir, 0755)
		if err != nil {
			fmt.Println("Error creating data directory:", err)
			return
		}
	}

	// Step 4: Create the .bencode file with only the torrent string
	filePath := filepath.Join(dataDir, hashID+".bencode")
	err = os.WriteFile(filePath, []byte(resp.Data.TorrentString), 0644)
	if err != nil {
		fmt.Println("Error writing torrent string to file:", err)
		return
	}
	fmt.Printf("\nTorrent file '%s' created with content:\n%s\n", filePath, resp.Data.TorrentString)

	// Step 5: Store peers in a list variable with "ip:port" format only
	var peerList []string
	for _, peer := range resp.Data.Peers {
		peerID := fmt.Sprintf("%s:%d", peer.IPAddress, peer.Port)
		peerList = append(peerList, peerID)
	}

	// Print the list of peers
	fmt.Println("\nList of peers associated with this torrent (IP:Port):")
	for _, peer := range peerList {
		fmt.Println("  -", peer)
	}

	// Step 6: Decode the .bencode file to prepare for download
	torrentFile, err := torrent.DecodeTorrentFile(filePath)
	if err != nil {
		fmt.Println("Error decoding torrent file:", err)
		return
	}

	// Step 7: Create file status for downloading and save it
	status := torrent.CreateFileStatusForPeer2(torrentFile)
	err = torrent.SaveFileStatus(status)
	if err != nil {
		fmt.Println("Error saving file status:", err)
		return
	}

	// Step 8: Download the file from the peers
	err = torrent.DownloadFileFromPeers(torrentFile, status, torrentFile.Filename, peerList)
	if err != nil {
		fmt.Println("Error downloading file:", err)
	} else {
		fmt.Printf("File '%s' downloaded successfully.\n", torrentFile.Filename)
	}

	// Step 9: Mark the file as complete on the tracker
	completeReq := models.CompleteRequest{
		HashID:          hashID,
		IPAddress:       utils.GetLocalIP(),
		Port:            12345,
		CompletedPieces: len(torrentFile.Hashes),
		TotalPieces:     len(torrentFile.Hashes),
	}
	completeResp, err := client.SendStatus(completeReq)
	if err != nil {
		fmt.Println("Error marking file as complete on the tracker:", err)
	} else {
		fmt.Printf("File marked as complete on tracker: %s\n", completeResp.Message)
	}
}

func handleGetFiles(client *tracker.TrackerClient) {
	resp, err := client.GetAllFiles()
	if err != nil {
		fmt.Println("Error retrieving files:", err)
		return
	}
	fmt.Printf("Files response: %+v\n", resp)
}

func handleGetPeers(client *tracker.TrackerClient) {
	resp, err := client.GetAllPeers()
	if err != nil {
		fmt.Println("Error retrieving peers:", err)
		return
	}
	fmt.Printf("Peers response: %+v\n", resp)
}

func handleMarkFileComplete(client *tracker.TrackerClient) {
	completeReq := models.CompleteRequest{
		HashID:          "eiogjifwpoajfjPWFJIPJAFJOAJWOFJOPAJOFWJPOS",
		IPAddress:       "192.168.1.10",
		Port:            8080,
		CompletedPieces: 10,
		TotalPieces:     10,
	}

	resp, err := client.SendStatus(completeReq)
	if err != nil {
		fmt.Println("Error marking file complete:", err)
		return
	}
	fmt.Printf("Complete response: %+v\n", resp)
}

func handleGetFileDetails(client *tracker.TrackerClient) {
	// Example hash_id for testing - replace with actual hash_id as needed
	hashID := "eiogjifwpoajfjPWFJIPJAFJOAJWOFJOPAJOFWJPOS"

	// Fetch file details for the given hashID
	resp, err := client.GetFileDetails(hashID)
	if err != nil {
		fmt.Println("Error retrieving file details:", err)
		return
	}

	// Print out the file details response
	fmt.Printf("File details response for hash ID '%s':\n", hashID)
	fmt.Printf("Status: %s\n", resp.Status)
	fmt.Printf("Message: %s\n", resp.Message)
	fmt.Printf("Torrent String: %s\n", resp.Data.TorrentString)

	// Step 1: Create the data directory if it doesn't exist
	dataDir := "data"
	if _, err := os.Stat(dataDir); os.IsNotExist(err) {
		err := os.Mkdir(dataDir, 0755)
		if err != nil {
			fmt.Println("Error creating data directory:", err)
			return
		}
	}

	// Step 2: Create the .bencode file with only the torrent string
	filePath := filepath.Join(dataDir, hashID+".bencode")
	err = os.WriteFile(filePath, []byte(resp.Data.TorrentString), 0644)
	if err != nil {
		fmt.Println("Error writing torrent string to file:", err)
		return
	}

	// Step 2.5: Store peers in a list variable with "ip:port" format only
	var peerList []string
	for _, peer := range resp.Data.Peers {
		peerID := fmt.Sprintf("%s:%d", peer.IPAddress, peer.Port)
		peerList = append(peerList, peerID)
	}

	// Print the list of peers from the variable
	fmt.Println("\nList of peers associated with this torrent (IP:Port):")
	for _, peer := range peerList {
		fmt.Println("  -", peer)
	}

	// Step 3: Confirm the file was created with the expected content
	fmt.Printf("\nFile '%s' created in the 'data' directory with the following content:\n", filePath)
	fmt.Println(resp.Data.TorrentString)
}
