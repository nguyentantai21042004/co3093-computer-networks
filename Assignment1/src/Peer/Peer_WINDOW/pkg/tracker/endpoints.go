package tracker

const (
	UploadEndpoint      = "/tracker/api/v1/upload"
	FilesEndpoint       = "/tracker/api/v1/files"
	ScrapeEndpoint      = "/tracker/api/v1/files/%s/peers"
	StatusEndpoint      = "/tracker/api/v1/files/complete"
	FileDetailsEndpoint = "/tracker/api/v1/files/%s"
	// This one is for testing
	PeersEndpoint = "/tracker/api/v1/peers"
)
