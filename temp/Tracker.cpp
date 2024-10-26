#include "Tracker.h"

// Initialize the table to store information of peers
std::unordered_map<std::string, std::vector<PeerInfo>> peer_table;

int main()
{
    // This one is initialize infor for Tracker
    std::string host_ip = get_host_default_interface_ip();
    int port = 22237;

    std::cout << "Tracker IP is: " << host_ip << ", available on port: " << port << std::endl;
    server_program(host_ip, port);
    return 0;
}

void handle_scrape_request(int conn, const std::string &info_hash)
{
    if (peer_table.find(info_hash) != peer_table.end())
    {
        int seeder_count = 0, leecher_count = 0;
        for (const auto &peer : peer_table[info_hash])
        {
            if (peer.is_seeder)
                seeder_count++;
            else
                leecher_count++;
        }

        std::string response = "Scrape result for " + info_hash + ":\n";
        response += "Seeders: " + std::to_string(seeder_count) + "\n";
        response += "Leechers: " + std::to_string(leecher_count) + "\n";
        send(conn, response.c_str(), response.size(), 0);
    }
    else
    {
        std::string response = "No peers found for " + info_hash + "\n";
        send(conn, response.c_str(), response.size(), 0);
    }
}

void handle_stopped_request(const std::string &info_hash, const std::string &peer_id)
{
    auto &peers = peer_table[info_hash];
    peers.erase(std::remove_if(peers.begin(), peers.end(),
                               [&](const PeerInfo &peer)
                               { return peer.peer_id == peer_id; }),
                peers.end());
    std::cout << "Peer " << peer_id << " has stopped sharing file " << info_hash << std::endl;
}

void handle_announce_request(int conn, const std::string &info_hash, const std::string &peer_id, int port)
{
    struct sockaddr_in addr;
    socklen_t addr_len = sizeof(addr);
    getpeername(conn, (struct sockaddr *)&addr, &addr_len);
    std::string ip = inet_ntoa(addr.sin_addr);

    // Check if peer with same IP and port already exists for this info_hash
    bool peer_exists = false;
    for (const auto &peer : peer_table[info_hash])
    {
        if (peer.ip == ip && peer.port == port)
        {
            peer_exists = true;
            break;
        }
    }

    // If the peer already exists, send a response that the information has been saved.
    if (peer_exists)
    {
        std::string response = "Peer with IP " + ip + " and port " + std::to_string(port) + " already exists for info_hash " + info_hash + ".\n";
        send(conn, response.c_str(), response.size(), 0);
    }
    else
    {
        // If peer does not exist, save peer information to table
        PeerInfo new_peer = {peer_id, ip, port, false}; // Initially peer is leecher
        peer_table[info_hash].push_back(new_peer);

        // Generate a list of peers with the same info_hash in response
        std::string response = "Peers sharing " + info_hash + ":\n";
        for (const auto &peer : peer_table[info_hash])
        {
            response += "Peer ID: " + peer.peer_id + " IP: " + peer.ip + " Port: " + std::to_string(peer.port) + "\n";
        }

        send(conn, response.c_str(), response.size(), 0);
    }
}

void handle_completed_request(const std::string &info_hash, const std::string &peer_id, int conn)
{
    // Update peer status to seeder
    for (auto &peer : peer_table[info_hash])
    {
        if (peer.peer_id == peer_id)
        {
            peer.is_seeder = true;
            std::cout << "Peer " << peer_id << " has become a seeder for file " << info_hash << std::endl;

            // Gửi phản hồi cho peer để xác nhận hoàn thành
            std::string response = "Completed request processed successfully for info_hash " + info_hash;
            send(conn, response.c_str(), response.size(), 0);
            break;
        }
    }
}

void handle_peer_list_request(int conn)
{
    std::string response = "Current Peer List:\n";
    for (const auto &entry : peer_table)
    {
        for (const auto &peer : entry.second)
        {
            response += "Peer ID: " + peer.peer_id + " IP: " + peer.ip + " Port: " + std::to_string(peer.port) + "\n";
        }
    }
    send(conn, response.c_str(), response.size(), 0);
}

void handle_file_list_request(int conn)
{
    std::string response = "Current File List:\n";
    for (const auto &entry : peer_table)
    {
        response += "Info_hash: " + entry.first + "\n";
    }
    send(conn, response.c_str(), response.size(), 0);
}

void new_connection(int conn)
{
    struct sockaddr_in addr;
    socklen_t addr_len = sizeof(addr);
    getpeername(conn, (struct sockaddr *)&addr, &addr_len);

    std::cout << "New connection to Tracker, from Peer has IP: " << inet_ntoa(addr.sin_addr) << ", port: " << ntohs(addr.sin_port) << std::endl;

    char buffer[1024];
    while (true)
    {
        ssize_t bytes_received = recv(conn, buffer, sizeof(buffer), 0);
        if (bytes_received <= 0)
        {
            break; // If no data, exit the loop
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
            handle_completed_request(info_hash, peer_id, conn);
        }
        else if (command == "scrape")
        {
            handle_scrape_request(conn, info_hash);
        }
        else if (command == "stopped")
        {
            handle_stopped_request(info_hash, peer_id);
        }
        else if (command == "peer_list")
        {
            handle_peer_list_request(info_hash, peer_id);
        }
        else if (command == "file_list")
        {
            handle_file_list_request(info_hash, peer_id);
        }
    }
    close(conn);
    std::cout << "Connection closed to Tracker, from Peer has IP: " << inet_ntoa(addr.sin_addr) << ", port: " << ntohs(addr.sin_port) << std::endl;
}

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
    std::cout << "This Tracker is listening on IP: " << host_ip << ", port: " << port << std::endl;

    while (true)
    {
        int conn = accept(serversocket, nullptr, nullptr);
        if (conn < 0)
        {
            std::cerr << "Accept failed" << std::endl;
            continue;
        }
        std::thread(new_connection, conn).detach(); // Create new thread for each connection
    }

    close(serversocket);
}

std::string get_host_default_interface_ip()
{
    int sock = socket(AF_INET, SOCK_DGRAM, 0);
    if (sock < 0)
        return "127.0.0.1"; // If socket cannot be created, return localhost

    sockaddr_in addr;
    addr.sin_family = AF_INET;
    addr.sin_port = htons(53);                   // DNS Gateway
    addr.sin_addr.s_addr = inet_addr("8.8.8.8"); // A public IP address to identify

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
    return "127.0.0.1"; // If not successfull, return localhost
}