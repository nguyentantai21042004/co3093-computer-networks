package main

import (
	"fmt"
	"net"
	"os"
	"time"
)

func main() {
	// Địa chỉ IP của máy chủ trực tuyến và cổng đích không tồn tại
	serverAddr := "8.8.8.8:33434" // Cổng 9999 là một cổng không phổ biến

	// Tạo UDP address
	addr, err := net.ResolveUDPAddr("udp", serverAddr)
	if err != nil {
		fmt.Println("Error resolving address:", err)
		os.Exit(1)
	}

	// Kết nối tới địa chỉ UDP
	conn, err := net.DialUDP("udp", nil, addr)
	if err != nil {
		fmt.Println("Error connecting:", err)
		os.Exit(1)
	}
	defer conn.Close()

	// Gửi một gói UDP rỗng
	message := []byte("ping")
	_, err = conn.Write(message)
	if err != nil {
		fmt.Println("Error sending message:", err)
		os.Exit(1)
	}
	fmt.Println("UDP packet sent to", serverAddr)

	// Chờ một thời gian để thu lại phản hồi ICMP trong Wireshark
	time.Sleep(10 * time.Second) // Chờ 10 giây
}
