package main

import (
	"bufio"
	"fmt"
	"os"
	"strings"

	"github.com/nguyentantai21042004/CO3093-Computer-Networks-HK241/pkg/torrent"
)

func main() {
	go torrent.StartServer() // Start server in the background

	reader := bufio.NewReader(os.Stdin)
	for {
		fmt.Println("\nOptions:")
		fmt.Println("1. Share a file")
		fmt.Println("2. Download a file")
		fmt.Println("3. Exit")
		fmt.Print("Choose an option (1, 2, or 3): ")
		choice, _ := reader.ReadString('\n')
		choice = strings.TrimSpace(choice)

		switch choice {
		case "1":
			handleShareFile(reader)
		case "2":
			handleDownloadFile(reader)
		case "3":
			fmt.Println("Exiting program...")
			return
		default:
			fmt.Println("Invalid choice, please try again.")
		}
	}
}

func handleShareFile(reader *bufio.Reader) {
	fmt.Print("Enter the path to the file to share: ")
	filename, _ := reader.ReadString('\n')
	filename = strings.TrimSpace(filename)

	pieceSize := 1024
	torrentFile, err := torrent.CreateTorrentFile(filename, pieceSize)
	if err != nil {
		fmt.Println("Error creating torrent:", err)
		return
	}

	err = torrent.SaveTorrentFile(torrentFile)
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
	fmt.Printf("File '%s' is now shared.\n", filename)
}

func handleDownloadFile(reader *bufio.Reader) {
	fmt.Print("Enter the path to the torrent file (.bencode) to download: ")
	torrentPath, _ := reader.ReadString('\n')
	torrentPath = strings.TrimSpace(torrentPath)

	torrentFile, err := torrent.DecodeTorrentFile(torrentPath)
	if err != nil {
		fmt.Println("Error decoding torrent:", err)
		return
	}

	status := torrent.CreateFileStatusForPeer2(torrentFile)
	err = torrent.SaveFileStatus(status)
	if err != nil {
		fmt.Println("Error saving file status:", err)
		return
	}

	fmt.Print("Enter a comma-separated list of peer IP addresses and ports (e.g., 192.168.1.10:12345, 192.168.1.11:12345): ")
	peersInput, _ := reader.ReadString('\n')
	peerAddresses := strings.Split(strings.TrimSpace(peersInput), ",")

	err = torrent.DownloadFileFromPeers(torrentFile, status, torrentFile.Filename, peerAddresses)
	if err != nil {
		fmt.Println("Error downloading file:", err)
	} else {
		fmt.Printf("File '%s' downloaded successfully.\n", torrentFile.Filename)
	}
}
