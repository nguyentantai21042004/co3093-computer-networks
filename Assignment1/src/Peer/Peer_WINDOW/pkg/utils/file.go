package utils

import (
	"crypto/sha1"
	"encoding/base64"
	"strings"
)

// Compute the entire file's hash ID based on piece hashes
func CalculateFileHashID(hashes []string) string {
	concatenatedHashes := strings.Join(hashes, "")
	fileHash := sha1.Sum([]byte(concatenatedHashes))
	return base64.StdEncoding.EncodeToString(fileHash[:])
}

// Sanitize fileHashID to be a valid filename
func SanitizeFileName(fileName string) string {
	// Replace or remove invalid characters for filenames (e.g., +, /, etc.)
	fileName = strings.ReplaceAll(fileName, "+", "_")
	fileName = strings.ReplaceAll(fileName, "/", "_")
	// Add more replacements as necessary
	return fileName
}
