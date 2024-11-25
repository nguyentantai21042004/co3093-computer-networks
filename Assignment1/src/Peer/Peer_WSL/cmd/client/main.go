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

const (
	Reset   = "\033[0m"
	Red     = "\033[31m"
	Green   = "\033[32m"
	Yellow  = "\033[33m"
	Blue    = "\033[34m"
	Magenta = "\033[35m"
	Cyan    = "\033[36m"
	White   = "\033[37m"
)

// Display menu with colors and separation
func showMenu() {
	fmt.Println(Cyan + "\n================== Torrent CLI ==================" + Reset)
	fmt.Println(Green + "Options:" + Reset)
	fmt.Println("[1] 📤 Share a file - Upload a file to share with peers.")
	fmt.Println("[2] 📥 Download a file - Fetch details and download from peers.")
	fmt.Println("[3] 📄 Get all files - View all files available on the tracker.")
	fmt.Println("[4] 🔍 Get all peers - List all peers associated with a torrent.")
	fmt.Println("[5] ❌ Exit - Close the application.")
	fmt.Println(Cyan + "=================================================" + Reset)
}

// Loading spinner effect for tasks
func loadingSpinner(message string) {
	spinner := []string{"|", "/", "-", "\\"}
	fmt.Print(Yellow + message + Reset)
	for i := 0; i < 10; i++ {
		fmt.Printf("\r%s %s", message, spinner[i%len(spinner)])
		time.Sleep(100 * time.Millisecond)
	}
	fmt.Print("\r" + Green + "Done!" + Reset + "\n")
}

func main() {
	// Assuming torrent.StartServer is your server initialization function
	go torrent.StartServer()

	trackerURL := "https://co3093-computer-networks-tracker-backend.onrender.com"
	client := tracker.NewTrackerClient(trackerURL, 10*time.Second)

	reader := bufio.NewReader(os.Stdin)

	// Main loop
	for {
		// Display menu
		showMenu()
		fmt.Print("Choose an option (1-6): ")
		input, _ := reader.ReadString('\n')
		input = strings.TrimSpace(input)

		// Process choice
		switch input {
		case "1":
			fmt.Println(Green + "Sharing a file..." + Reset)
			handleShareFile(reader, client)
		case "2":
			fmt.Println(Green + "Downloading a file..." + Reset)
			handleDownloadFile(client, reader)
		case "3":
			fmt.Println(Green + "Getting all files from tracker..." + Reset)
			handleGetFiles(client)
		case "4":
			fmt.Println(Green + "Getting all peers from tracker..." + Reset)
			handleGetPeers(client)
		case "5":
			fmt.Println(Green + "Exiting program..." + Reset)
			return
		default:
			fmt.Println(Red + "Invalid choice! Please enter a number between 1 and 5." + Reset)
		}
	}
}

func handleShareFile(reader *bufio.Reader, client *tracker.TrackerClient) {
	fmt.Print(Cyan + "Enter the path to the file to share: " + Reset)
	filename, _ := reader.ReadString('\n')
	filename = strings.TrimSpace(filename)

	pieceSize := 1024
	fmt.Println(Yellow + "Creating torrent file..." + Reset)
	loadingSpinner("Processing")
	torrentFile, err := torrent.CreateTorrentFile(filename, pieceSize)
	if err != nil {
		fmt.Println(Red + "Error creating torrent: " + err.Error() + Reset)
		return
	}

	fmt.Println(Yellow + "Saving torrent file..." + Reset)
	fileHashID, err := torrent.SaveTorrentFile(torrentFile)
	if err != nil {
		fmt.Println(Red + "Error saving torrent file: " + err.Error() + Reset)
		return
	}

	status := torrent.CreateFileStatusForPeer1(torrentFile)
	err = torrent.SaveFileStatus(status)
	if err != nil {
		fmt.Println(Red + "Error saving file status: " + err.Error() + Reset)
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

	fmt.Println(Yellow + "Uploading file to tracker..." + Reset)
	resp, err := client.UploadFile(uploadReq)
	if err != nil {
		fmt.Println(Red + "Error uploading file: " + err.Error() + Reset)
		return
	}
	fmt.Printf(Green+"File '%s' is now shared successfully!\n"+Reset, filename)
	fmt.Printf("Upload response: %+v\n", resp.Data)
}

func handleDownloadFile(client *tracker.TrackerClient, reader *bufio.Reader) {
	fmt.Print(Cyan + "Enter the hash ID of the file to fetch details and download: " + Reset)
	hashID, _ := reader.ReadString('\n')
	hashID = strings.TrimSpace(hashID)

	fmt.Println(Yellow + "Fetching file details from tracker..." + Reset)
	loadingSpinner("Fetching details")
	resp, err := client.GetFileDetails(hashID)
	if err != nil {
		fmt.Println(Red + "Error retrieving file details: " + err.Error() + Reset)
		return
	}

	fmt.Println(Green + "File details retrieved successfully!" + Reset)
	fmt.Printf("Status: %s\nMessage: %s\n", resp.Status, resp.Message)

	dataDir := "data"
	if _, err := os.Stat(dataDir); os.IsNotExist(err) {
		fmt.Println(Yellow + "Creating data directory..." + Reset)
		err := os.Mkdir(dataDir, 0755)
		if err != nil {
			fmt.Println(Red + "Error creating data directory: " + err.Error() + Reset)
			return
		}
	}

	filePath := filepath.Join(dataDir, hashID+".bencode")
	fmt.Println(Yellow + "Creating torrent file..." + Reset)
	err = os.WriteFile(filePath, []byte(resp.Data.TorrentString), 0644)
	if err != nil {
		fmt.Println(Red + "Error writing torrent string to file: " + err.Error() + Reset)
		return
	}
	fmt.Printf(Green+"\nTorrent file '%s' created successfully.\n"+Reset, filePath)

	var peerList []string
	for _, peer := range resp.Data.Peers {
		peerID := fmt.Sprintf("%s:%d", peer.IPAddress, peer.Port)
		peerList = append(peerList, peerID)
	}

	fmt.Println("\n" + Cyan + "List of peers associated with this torrent (IP:Port):" + Reset)
	for _, peer := range peerList {
		fmt.Println("  -", peer)
	}

	fmt.Println(Yellow + "Decoding torrent file for download..." + Reset)
	torrentFile, err := torrent.DecodeTorrentFile(filePath)
	if err != nil {
		fmt.Println(Red + "Error decoding torrent file: " + err.Error() + Reset)
		return
	}

	status := torrent.CreateFileStatusForPeer2(torrentFile)
	err = torrent.SaveFileStatus(status)
	if err != nil {
		fmt.Println(Red + "Error saving file status: " + err.Error() + Reset)
		return
	}

	fmt.Println(Yellow + "Downloading file from peers..." + Reset)
	err = torrent.DownloadFileFromPeers(torrentFile, status, torrentFile.Filename, peerList)
	if err != nil {
		fmt.Println(Red + "Error downloading file: " + err.Error() + Reset)
		return
	}
	fmt.Printf(Green+"File '%s' downloaded successfully!\n"+Reset, torrentFile.Filename)

	completeReq := models.CompleteRequest{
		HashID:          hashID,
		IPAddress:       utils.GetLocalIP(),
		Port:            12345,
		CompletedPieces: len(torrentFile.Hashes),
		TotalPieces:     len(torrentFile.Hashes),
	}

	fmt.Println(Yellow + "Marking file as complete on tracker..." + Reset)
	completeResp, err := client.SendStatus(completeReq)
	if err != nil {
		fmt.Println(Red + "Error marking file as complete: " + err.Error() + Reset)
		return
	}
	fmt.Printf(Green+"File marked as complete on tracker: %s\n"+Reset, completeResp.Message)
}

func handleGetFiles(client *tracker.TrackerClient) {
	fmt.Println(Yellow + "Fetching all files from tracker..." + Reset)
	loadingSpinner("Processing")

	resp, err := client.GetAllFiles()
	if err != nil {
		fmt.Println(Red + "Error retrieving files: " + err.Error() + Reset)
		return
	}

	if len(resp.Data) == 0 {
		fmt.Println(Yellow + "No files available on the tracker." + Reset)
		return
	}

	fmt.Println(Green + "\nList of Files:" + Reset)
	fmt.Printf("%-30s %-10s %-10s\n", "File Name", "Size (MB)", "Hash ID")
	fmt.Println(strings.Repeat("-", 50))
	for _, file := range resp.Data {
		fmt.Printf("%-30s %-10.2f %-10s\n", file.FileName, float64(file.FileSize)/1024/1024, file.HashID)
	}
	fmt.Println(Cyan + "\nFiles fetched successfully!" + Reset)
}

func handleGetPeers(client *tracker.TrackerClient) {
	fmt.Println(Yellow + "Fetching all peers from tracker..." + Reset)
	loadingSpinner("Processing")

	resp, err := client.GetAllPeers()
	if err != nil {
		fmt.Println(Red + "Error retrieving peers: " + err.Error() + Reset)
		return
	}

	if len(resp.Data) == 0 {
		fmt.Println(Yellow + "No peers available on the tracker." + Reset)
		return
	}

	fmt.Println(Green + "\nList of Peers:" + Reset)
	fmt.Printf("%-15s %-10s\n", "IP Address", "Port")
	fmt.Println(strings.Repeat("-", 30))
	for _, peer := range resp.Data {
		fmt.Printf("%-15s %-10d\n", peer.IPAddress, peer.Port)
	}
	fmt.Println(Cyan + "\nPeers fetched successfully!" + Reset)
}

func handleMarkFileComplete(client *tracker.TrackerClient) {
	fmt.Println(Yellow + "Marking file as complete on tracker..." + Reset)
	loadingSpinner("Processing")

	completeReq := models.CompleteRequest{
		HashID:          "eiogjifwpoajfjPWFJIPJAFJOAJWOFJOPAJOFWJPOS",
		IPAddress:       "192.168.1.10",
		Port:            8080,
		CompletedPieces: 10,
		TotalPieces:     10,
	}

	resp, err := client.SendStatus(completeReq)
	if err != nil {
		fmt.Println(Red + "Error marking file complete: " + err.Error() + Reset)
		return
	}
	fmt.Println(Green + "File marked as complete successfully!" + Reset)
	fmt.Printf("Response: %+v\n", resp)
}
