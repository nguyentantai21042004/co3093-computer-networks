package tracker

import (
	"bytes"
	"encoding/json"
	"fmt"
	"net/http"
	"time"
)

// TrackerClient requests to the client for interacting with the tracker
type TrackerClient struct {
	BaseURL string
	Timeout time.Duration
}

// NewTrackerClient initializes a new TrackerClient with a base URL and timeout.
func NewTrackerClient(baseURL string, timeout time.Duration) *TrackerClient {
	return &TrackerClient{
		BaseURL: baseURL,
		Timeout: timeout,
	}
}

// SendRequest is a generic method to send requests to the tracker API.
func (tc *TrackerClient) SendRequest(method, endpoint string, reqBody interface{}, respBody interface{}) error {
	data, err := json.Marshal(reqBody)
	if err != nil {
		return fmt.Errorf("failed to encode request body: %v", err)
	}

	url := fmt.Sprintf("%s%s", tc.BaseURL, endpoint)
	request, err := http.NewRequest(method, url, bytes.NewBuffer(data))
	if err != nil {
		return fmt.Errorf("failed to create request: %v", err)
	}
	request.Header.Set("Content-Type", "application/json")

	client := &http.Client{Timeout: tc.Timeout}
	response, err := client.Do(request)
	if err != nil {
		return fmt.Errorf("request failed: %v", err)
	}
	defer response.Body.Close()

	if response.StatusCode != http.StatusOK {
		if response.StatusCode == 400 {
			return fmt.Errorf("This file has been existed")
		}
		return fmt.Errorf("unexpected status code: %d", response.StatusCode)
	}

	if respBody != nil {
		if err := json.NewDecoder(response.Body).Decode(respBody); err != nil {
			return fmt.Errorf("failed to decode response body: %v", err)
		}
	}

	return nil
}
