#include <iostream>
#include <thread>
#include <cstring>
#include <unistd.h>
#include <arpa/inet.h>
#include <unordered_map>
#include <vector>
#include <sstream>

// Cấu trúc lưu trữ thông tin về các peer
struct PeerInfo
{
    std::string peer_id;
    std::string ip;
    int port;
    bool is_seeder; // Đánh dấu peer là seeder hay leecher
};

// Bảng lưu trữ thông tin các peer đang chia sẻ file theo info_hash
std::unordered_map<std::string, std::vector<PeerInfo>> peer_table;

// Hàm xử lý yêu cầu announce từ peer
void handle_announce_request(int conn, const std::string &info_hash, const std::string &peer_id, int port)
{
    struct sockaddr_in addr;
    socklen_t addr_len = sizeof(addr);
    getpeername(conn, (struct sockaddr *)&addr, &addr_len);
    std::string ip = inet_ntoa(addr.sin_addr);

    // Lưu thông tin peer vào bảng
    PeerInfo new_peer = {peer_id, ip, port, false}; // Ban đầu peer là leecher
    peer_table[info_hash].push_back(new_peer);

    // Tạo phản hồi danh sách các peer có cùng info_hash
    std::string response = "Peers sharing " + info_hash + ":\n";
    for (const auto &peer : peer_table[info_hash])
    {
        response += "Peer ID: " + peer.peer_id + " IP: " + peer.ip + " Port: " + std::to_string(peer.port) + "\n";
    }

    // Gửi phản hồi lại cho peer
    send(conn, response.c_str(), response.size(), 0);
}

// Hàm xử lý yêu cầu completed từ peer
void handle_completed_request(const std::string &info_hash, const std::string &peer_id)
{
    // Cập nhật trạng thái của peer thành seeder
    for (auto &peer : peer_table[info_hash])
    {
        if (peer.peer_id == peer_id)
        {
            peer.is_seeder = true;
            std::cout << "Peer " << peer_id << " đã trở thành seeder cho file " << info_hash << std::endl;
            break;
        }
    }
}

void handle_list_files_request(int conn, const std::string &peer_id)
{
    std::string response = "Files held by peer " + peer_id + ":\n";
    for (const auto &pair : peer_table)
    {
        for (const auto &peer : pair.second)
        {
            if (peer.peer_id == peer_id)
            {
                response += "File (info_hash): " + pair.first + (peer.is_seeder ? " [Seeder]\n" : " [Leecher]\n");
            }
        }
    }
    send(conn, response.c_str(), response.size(), 0);
}

void handle_list_peers_request(int conn)
{
    std::string response = "Peers in the system:\n";
    for (const auto &pair : peer_table)
    {
        response += "File (info_hash): " + pair.first + "\n";
        for (const auto &peer : pair.second)
        {
            response += "Peer ID: " + peer.peer_id + " IP: " + peer.ip + " Port: " + std::to_string(peer.port) + (peer.is_seeder ? " [Seeder]\n" : " [Leecher]\n");
        }
    }
    send(conn, response.c_str(), response.size(), 0);
}

// Hàm xử lý từng kết nối đến từ peer
void new_connection(int conn)
{
    struct sockaddr_in addr;
    socklen_t addr_len = sizeof(addr);
    getpeername(conn, (struct sockaddr *)&addr, &addr_len);

    std::cout << "New connection from: " << inet_ntoa(addr.sin_addr) << ":" << ntohs(addr.sin_port) << std::endl;

    char buffer[1024];
    while (true)
    {
        ssize_t bytes_received = recv(conn, buffer, sizeof(buffer), 0);
        if (bytes_received <= 0)
        {
            break; // Nếu không có dữ liệu, thoát vòng lặp
        }

        std::string request(buffer, bytes_received);
        std::stringstream ss(request);
        std::string command, info_hash, peer_id;
        int port;

        ss >> command >> info_hash >> peer_id >> port;

        if (command == "announce")
        {
            handle_announce_request(conn, info_hash, peer_id, port);
        }
        else if (command == "completed")
        {
            handle_completed_request(info_hash, peer_id);
        }
        else if (command == "list_peers")
        {
            handle_list_peers_request(conn);
        }
        else if (command == "list_files")
        {
            handle_list_files_request(conn, peer_id);
        }
    }

    close(conn);
    std::cout << "Connection closed from: " << inet_ntoa(addr.sin_addr) << std::endl;
}

// Hàm thiết lập server TCP cho Tracker
void server_program(const std::string &host_ip, int port)
{
    int serversocket = socket(AF_INET, SOCK_STREAM, 0);
    if (serversocket < 0)
    {
        std::cerr << "Could not create socket" << std::endl;
        return;
    }

    sockaddr_in server_addr;
    server_addr.sin_family = AF_INET;
    server_addr.sin_addr.s_addr = inet_addr(host_ip.c_str());
    server_addr.sin_port = htons(port);

    if (bind(serversocket, (struct sockaddr *)&server_addr, sizeof(server_addr)) < 0)
    {
        std::cerr << "Bind failed" << std::endl;
        return;
    }

    listen(serversocket, 10);
    std::cout << "Listening on: " << host_ip << ":" << port << std::endl;

    while (true)
    {
        int conn = accept(serversocket, nullptr, nullptr);
        if (conn < 0)
        {
            std::cerr << "Accept failed" << std::endl;
            continue;
        }
        std::thread(new_connection, conn).detach(); // Tạo thread mới cho mỗi kết nối
    }

    close(serversocket);
}

// Hàm lấy địa chỉ IP của máy chủ
std::string get_host_default_interface_ip()
{
    int sock = socket(AF_INET, SOCK_DGRAM, 0);
    if (sock < 0)
        return "127.0.0.1"; // Nếu không thể tạo socket, trả về localhost

    sockaddr_in addr;
    addr.sin_family = AF_INET;
    addr.sin_port = htons(53);                   // Cổng DNS
    addr.sin_addr.s_addr = inet_addr("8.8.8.8"); // Một địa chỉ IP public để xác định

    if (connect(sock, (struct sockaddr *)&addr, sizeof(addr)) == 0)
    {
        // Connect successfully
        sockaddr_in local_addr;
        socklen_t addr_len = sizeof(local_addr);
        getsockname(sock, (struct sockaddr *)&local_addr, &addr_len);
        close(sock);
        return inet_ntoa(local_addr.sin_addr);
    }

    close(sock);
    return "127.0.0.1"; // Nếu không thành công, trả về localhost
}

int main()
{
    std::string host_ip = get_host_default_interface_ip();
    int port = 22237;

    std::cout << "Tracker IP is: " << host_ip << ", available on port: " << port << std::endl;

    server_program(host_ip, port);
    return 0;
}