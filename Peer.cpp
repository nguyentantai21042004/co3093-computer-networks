#include <iostream>
#include <vector>
#include <thread>
#include <arpa/inet.h>
#include <unistd.h>
#include <sstream>
#include <cstring>
#include <sys/socket.h>

// Cấu trúc lưu trữ thông tin của các peer
struct Peer
{
    std::string peer_id;
    std::string ip;
    int port;
};

// Hàm kết nối với Tracker và nhận danh sách peer
std::vector<Peer> connect_to_tracker(const std::string &tracker_ip, int tracker_port, const std::string &info_hash, const std::string &peer_id, int peer_port)
{
    int sock = socket(AF_INET, SOCK_STREAM, 0);
    if (sock < 0)
    {
        std::cerr << "Could not create socket" << std::endl;
        exit(EXIT_FAILURE);
    }

    sockaddr_in tracker_addr;
    tracker_addr.sin_family = AF_INET;
    tracker_addr.sin_port = htons(tracker_port);
    inet_pton(AF_INET, tracker_ip.c_str(), &tracker_addr.sin_addr);

    // Kết nối tới tracker
    if (connect(sock, (struct sockaddr *)&tracker_addr, sizeof(tracker_addr)) < 0)
    {
        std::cerr << "Could not connect to tracker" << std::endl;
        exit(EXIT_FAILURE);
    }

    // Gửi yêu cầu announce tới tracker
    std::string announce_request = "announce " + info_hash + " " + peer_id + " " + std::to_string(peer_port);
    send(sock, announce_request.c_str(), announce_request.size(), 0);

    // Nhận phản hồi từ tracker
    char buffer[1024];
    ssize_t bytes_received = recv(sock, buffer, sizeof(buffer), 0);
    std::string response(buffer, bytes_received);
    std::cout << "Tracker response: " << response << std::endl;

    // Phân tích phản hồi để lấy danh sách peer
    std::vector<Peer> peers;
    std::stringstream ss(response);
    std::string line;
    while (std::getline(ss, line))
    {
        if (line.find("Peer ID") != std::string::npos)
        {
            Peer peer;
            std::stringstream peer_info(line);
            std::string temp;
            peer_info >> temp >> temp;                                         // Bỏ qua "Peer ID: "
            peer_info >> peer.peer_id >> temp >> peer.ip >> temp >> peer.port; // Phân tích IP và Port
            peers.push_back(peer);
        }
    }

    close(sock);
    return peers;
}

// Hàm tải dữ liệu từ peer khác
void download_from_peer(const Peer &peer)
{
    int sock = socket(AF_INET, SOCK_STREAM, 0);
    if (sock < 0)
    {
        std::cerr << "Could not create socket" << std::endl;
        return;
    }

    sockaddr_in peer_addr;
    peer_addr.sin_family = AF_INET;
    peer_addr.sin_port = htons(peer.port);
    inet_pton(AF_INET, peer.ip.c_str(), &peer_addr.sin_addr);

    // Kết nối tới peer
    if (connect(sock, (struct sockaddr *)&peer_addr, sizeof(peer_addr)) < 0)
    {
        std::cerr << "Could not connect to peer " << peer.peer_id << std::endl;
        return;
    }

    // Yêu cầu tải dữ liệu (tệp hoặc phần của tệp)
    std::string request = "GET file_part";
    send(sock, request.c_str(), request.size(), 0);

    // Nhận dữ liệu từ peer
    char buffer[1024];
    ssize_t bytes_received = recv(sock, buffer, sizeof(buffer), 0);
    std::string file_data(buffer, bytes_received);
    std::cout << "Received data from peer " << peer.peer_id << ": " << file_data << std::endl;

    close(sock);
}

// Hàm khởi động peer, gửi yêu cầu đến tracker và tải dữ liệu từ các peer khác
void send_announce_request(const std::string &tracker_ip, int tracker_port, const std::string &info_hash, const std::string &peer_id, int peer_port)
{
    // Kết nối với Tracker và nhận danh sách các peer
    std::vector<Peer> peers = connect_to_tracker(tracker_ip, tracker_port, info_hash, peer_id, peer_port);

    // Tải dữ liệu từ các peer khác (ở đây chỉ tải từ peer đầu tiên trong danh sách)
    for (const auto &peer : peers)
    {
        std::cout << "Connecting to peer: " << peer.peer_id << " (" << peer.ip << ":" << peer.port << ")" << std::endl;
        std::thread(download_from_peer, peer).detach(); // Tạo thread tải dữ liệu từ peer
    }
}

int main()
{
    std::string tracker_ip = "192.168.253.131"; // Địa chỉ IP của Tracker
    int tracker_port = 22237;                   // Cổng Tracker đang lắng nghe

    std::string info_hash = "abc23"; // Hash của tệp
    std::string peer_id = "peer4";    // ID của peer này
    int peer_port = 6881;             // Cổng mà peer này sử dụng

    send_announce_request(tracker_ip, tracker_port, info_hash, peer_id, peer_port);

    // Giữ chương trình chạy để các kết nối đồng thời có thể hoàn thành
    while (true)
    {
        sleep(1);
    }

    return 0;
}