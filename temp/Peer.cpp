#include "Peer.h"

void show_menu()
{
    std::cout << "================= Peer Menu =================" << std::endl;
    std::cout << "1. Announce (Thông báo peer tham gia mạng)" << std::endl;
    std::cout << "2. Scrape (Yêu cầu thông tin file từ tracker)" << std::endl;
    std::cout << "3. Completed (Thông báo peer đã hoàn thành tải file)" << std::endl;
    std::cout << "4. Stopped (Thông báo peer dừng chia sẻ file)" << std::endl;
    std::cout << "5. Peers List" << std::endl;
    std::cout << "6. Files List" << std::endl;
    std::cout << "7. Exit (Thoát chương trình)" << std::endl;
    std::cout << "=============================================" << std::endl;
    std::cout << "Lựa chọn: ";
}

int main()
{
    std::string tracker_ip = "192.168.253.131"; // Địa chỉ IP của Tracker
    int tracker_port = 22237;                   // Cổng Tracker đang lắng nghe
    std::string peer_id = "peer4";              // ID của peer này
    int peer_port = 6881;                       // Cổng mà peer này sử dụng

    while (true)
    {
        show_menu(); // Hiển thị menu cho người dùng

        int choice;
        std::cin >> choice;

        if (choice == 5)
        {
            std::cout << "Thoát chương trình." << std::endl;
            break;
        }

        std::string info_hash;
        std::cout << "Nhập info_hash: ";
        std::cin >> info_hash;

        switch (choice)
        {
        case 1:
            std::cout << "Sending Announce request..." << std::endl;
            send_announce_request(tracker_ip, tracker_port, info_hash, peer_id, peer_port);
            break;
        case 2:
            std::cout << "Sending Scrape request..." << std::endl;
            send_scrape_request(tracker_ip, tracker_port, info_hash);
            break;
        case 3:
            std::cout << "Sending Completed request..." << std::endl;
            send_completed_request(tracker_ip, tracker_port, info_hash, peer_id);
            break;
        case 4:
            std::cout << "Sending Stopped request..." << std::endl;
            send_stopped_request(tracker_ip, tracker_port, info_hash, peer_id);
            break;
        case 5:
            std::cout << "Get all peer in system" < std::endl;
            send_peer_list_request(tracker_ip, tracker_port);
            break;
        case 6:
            std::cout << "Get all files infor in system" < std::endl;
            send_file_list_request(tracker_ip, tracker_port);
            break;
        default:
            std::cout << "Lựa chọn không hợp lệ. Vui lòng chọn lại." << std::endl;
            break;
        }
    }

    return 0;
}

void send_announce_request(const std::string &tracker_ip, int tracker_port, const std::string &info_hash, const std::string &peer_id, int peer_port)
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

void send_scrape_request(const std::string &tracker_ip, int tracker_port, const std::string &info_hash)
{
    int sock = socket(AF_INET, SOCK_STREAM, 0);
    if (sock < 0)
    {
        std::cerr << "Could not create socket" << std::endl;
        return;
    }

    sockaddr_in tracker_addr;
    tracker_addr.sin_family = AF_INET;
    tracker_addr.sin_port = htons(tracker_port);
    inet_pton(AF_INET, tracker_ip.c_str(), &tracker_addr.sin_addr);

    // Kết nối tới tracker
    if (connect(sock, (struct sockaddr *)&tracker_addr, sizeof(tracker_addr)) < 0)
    {
        std::cerr << "Could not connect to tracker" << std::endl;
        close(sock);
        return;
    }

    // Gửi Scrape request tới tracker
    std::string scrape_request = "scrape " + info_hash;
    send(sock, scrape_request.c_str(), scrape_request.size(), 0);

    // Nhận phản hồi từ tracker
    char buffer[1024];
    ssize_t bytes_received = recv(sock, buffer, sizeof(buffer), 0);
    if (bytes_received > 0)
    {
        std::string response(buffer, bytes_received);
        std::cout << "Tracker response (scrape): " << response << std::endl;
    }

    close(sock);
}

void send_completed_request(const std::string &tracker_ip, int tracker_port, const std::string &info_hash, const std::string &peer_id)
{
    int sock = socket(AF_INET, SOCK_STREAM, 0);
    if (sock < 0)
    {
        std::cerr << "Could not create socket" << std::endl;
        return;
    }

    sockaddr_in tracker_addr;
    tracker_addr.sin_family = AF_INET;
    tracker_addr.sin_port = htons(tracker_port);
    inet_pton(AF_INET, tracker_ip.c_str(), &tracker_addr.sin_addr);

    // Kết nối tới tracker
    if (connect(sock, (struct sockaddr *)&tracker_addr, sizeof(tracker_addr)) < 0)
    {
        std::cerr << "Could not connect to tracker" << std::endl;
        close(sock);
        return;
    }

    // Gửi Completed request tới tracker
    std::string completed_request = "completed " + info_hash + " " + peer_id;
    send(sock, completed_request.c_str(), completed_request.size(), 0);

    // Nhận phản hồi từ tracker
    char buffer[1024];
    ssize_t bytes_received = recv(sock, buffer, sizeof(buffer), 0);
    if (bytes_received > 0)
    {
        std::string response(buffer, bytes_received);
        std::cout << "Tracker response (completed): " << response << std::endl;
    }

    close(sock);
}

void send_stopped_request(const std::string &tracker_ip, int tracker_port, const std::string &info_hash, const std::string &peer_id)
{
    int sock = socket(AF_INET, SOCK_STREAM, 0);
    if (sock < 0)
    {
        std::cerr << "Could not create socket" << std::endl;
        return;
    }

    sockaddr_in tracker_addr;
    tracker_addr.sin_family = AF_INET;
    tracker_addr.sin_port = htons(tracker_port);
    inet_pton(AF_INET, tracker_ip.c_str(), &tracker_addr.sin_addr);

    // Kết nối tới tracker
    if (connect(sock, (struct sockaddr *)&tracker_addr, sizeof(tracker_addr)) < 0)
    {
        std::cerr << "Could not connect to tracker" << std::endl;
        close(sock);
        return;
    }

    // Gửi Stopped request tới tracker
    std::string stopped_request = "stopped " + info_hash + " " + peer_id;
    send(sock, stopped_request.c_str(), stopped_request.size(), 0);

    // Nhận phản hồi từ tracker
    char buffer[1024];
    ssize_t bytes_received = recv(sock, buffer, sizeof(buffer), 0);
    if (bytes_received > 0)
    {
        std::string response(buffer, bytes_received);
        std::cout << "Tracker response (stopped): " << response << std::endl;
    }

    close(sock);
}

void send_peer_list_request(const std::string &tracker_ip, int tracker_port)
{
    int sock = socket(AF_INET, SOCK_STREAM, 0);
    if (sock < 0)
    {
        std::cerr << "Could not create socket" << std::endl;
        return;
    }

    sockaddr_in tracker_addr;
    tracker_addr.sin_family = AF_INET;
    tracker_addr.sin_port = htons(tracker_port);
    inet_pton(AF_INET, tracker_ip.c_str(), &tracker_addr.sin_addr);

    // Kết nối tới tracker
    if (connect(sock, (struct sockaddr *)&tracker_addr, sizeof(tracker_addr)) < 0)
    {
        std::cerr << "Could not connect to tracker" << std::endl;
        close(sock);
        return;
    }

    // Gửi Peer List request
    std::string request = "peer_list";
    send(sock, request.c_str(), request.size(), 0);

    // Nhận phản hồi từ tracker
    char buffer[1024];
    ssize_t bytes_received = recv(sock, buffer, sizeof(buffer), 0);
    if (bytes_received > 0)
    {
        std::string response(buffer, bytes_received);
        std::cout << "Tracker response (peer list): " << response << std::endl;
    }

    close(sock);
}

void send_file_list_request(const std::string &tracker_ip, int tracker_port)
{
    int sock = socket(AF_INET, SOCK_STREAM, 0);
    if (sock < 0)
    {
        std::cerr << "Could not create socket" << std::endl;
        return;
    }

    sockaddr_in tracker_addr;
    tracker_addr.sin_family = AF_INET;
    tracker_addr.sin_port = htons(tracker_port);
    inet_pton(AF_INET, tracker_ip.c_str(), &tracker_addr.sin_addr);

    // Kết nối tới tracker
    if (connect(sock, (struct sockaddr *)&tracker_addr, sizeof(tracker_addr)) < 0)
    {
        std::cerr << "Could not connect to tracker" << std::endl;
        close(sock);
        return;
    }

    // Gửi File List request
    std::string request = "file_list";
    send(sock, request.c_str(), request.size(), 0);

    // Nhận phản hồi từ tracker
    char buffer[1024];
    ssize_t bytes_received = recv(sock, buffer, sizeof(buffer), 0);
    if (bytes_received > 0)
    {
        std::string response(buffer, bytes_received);
        std::cout << "Tracker response (file list): " << response << std::endl;
    }

    close(sock);
}