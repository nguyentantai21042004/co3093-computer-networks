package models

import "time"

type TorrentFile struct {
	Filename  string   `bencode:"filename" json:"filename"`
	PieceSize int      `bencode:"piece_size" json:"piece_size"`
	Hashes    []string `bencode:"hashes" json:"hashes"`
}

type PieceInfo struct {
	Hash   string `json:"hash"`
	Status bool   `json:"status"`
}

type FileStatus struct {
	Filename   string      `json:"filename"`
	PieceSize  int         `json:"piece_size"`
	FileHashID string      `json:"file_hash_id"`
	Pieces     []PieceInfo `json:"pieces"`
}

// UploadRequest represents the JSON payload for the upload endpoint.
type UploadRequest struct {
	FileName      string `json:"file_name"`
	FileSize      int    `json:"file_size"`
	TorrentString string `json:"torrent_string"`
	HashID        string `json:"hash_id"`
	IPAddress     string `json:"ip_address"`
	ServerPort    int    `json:"server_port"`
	Status        string `json:"status"`
	TotalPieces   int    `json:"total_pieces"`
}

// UploadResponse represents the response from the upload endpoint.
type UploadResponse struct {
	Status  string `json:"status"`
	Message string `json:"message"`
	Data    struct {
		File struct {
			CreatedAt     string `json:"createdAt"`
			FileName      string `json:"file_name"`
			FileSize      int    `json:"file_size"`
			TorrentString string `json:"torrent_string"`
		} `json:"file"`
		Peers []PeerInfo `json:"peers"`
	} `json:"data"`
}

// FilesResponse represents the response from the /files endpoint.
type FilesResponse struct {
	Status  string `json:"status"`
	Message string `json:"message"`
	Data    []struct {
		HashID    string    `json:"hash_id"`
		FileName  string    `json:"file_name"`
		FileSize  int       `json:"file_size"`
		CreatedAt time.Time `json:"created_at"`
	} `json:"data"`
}

// PeerInfo represents peer details.
type PeerInfo struct {
	IPAddress string `json:"ip_address"`
	Port      int    `json:"port"`
	Status    string `json:"status"`
}

// PeersResponse represents the response from the /peers endpoint.
type PeersResponse struct {
	Status  string     `json:"status"`
	Message string     `json:"message"`
	Data    []PeerInfo `json:"data"`
}

// FilePeersRequest represents the JSON payload for the /files/peers endpoint.
type FilePeersRequest struct {
	TorrentString string `json:"torrent_string"`
}

// FilePeersResponse represents the response from the /files/peers endpoint.
type FilePeersResponse struct {
	Status  string     `json:"status"`
	Message string     `json:"message"`
	Data    []PeerInfo `json:"data"`
}

// CompleteRequest represents the JSON payload for the /files/complete endpoint.
type CompleteRequest struct {
	HashID          string `json:"hash_id"`
	IPAddress       string `json:"ip_address"`
	Port            int    `json:"port"`
	CompletedPieces int    `json:"completed_pieces"`
	TotalPieces     int    `json:"total_pieces"`
}

// CompleteResponse represents the response from the /files/complete endpoint.
type CompleteResponse struct {
	Status  string    `json:"status"`
	Message string    `json:"message"`
	Data    *struct{} `json:"data"` // Use pointer to struct for null response
}

// FileDetailsResponse represents the response from the /files/{hash_id} endpoint.
type FileDetailsResponse struct {
	Status  string `json:"status"`
	Message string `json:"message"`
	Data    struct {
		TorrentString string     `json:"torrent_string"`
		Peers         []PeerInfo `json:"peers"`
	} `json:"data"`
}
