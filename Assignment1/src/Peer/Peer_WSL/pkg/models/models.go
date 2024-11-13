package models

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
