package utils

import (
	"fmt"
	"net"
	"runtime"
)

// GetLocalIP finds the preferred local IP based on OS and address ranges.
func GetLocalIP() string {
	addrs, err := net.InterfaceAddrs()
	if err != nil {
		fmt.Println("Error retrieving local IP:", err)
		return ""
	}

	preferredIP := ""
	for _, addr := range addrs {
		if ipNet, ok := addr.(*net.IPNet); ok && !ipNet.IP.IsLoopback() {
			ip := ipNet.IP.To4()
			if ip == nil {
				continue
			}

			// Check for WSL IP preference if running on Linux
			if runtime.GOOS == "linux" && ip[0] == 172 && ip[1] == 20 {
				return ip.String() // WSL IP preference for 172.20.x.x range
			}

			// Check for Windows IP preference in 192.168.88.x range
			if runtime.GOOS == "windows" && ip[0] == 192 && ip[1] == 168 && ip[2] == 88 {
				return ip.String() // Windows IP preference for 192.168.88.x range
			}

			// Fallback: prioritize other private IP ranges if no preference is set
			if preferredIP == "" && (ip[0] == 10 || ip[0] == 172 && ip[1] >= 16 && ip[1] <= 31 || ip[0] == 192 && ip[1] == 168) {
				preferredIP = ip.String()
			}
		}
	}

	return preferredIP // Return the selected IP or an empty string if none found
}
