#ifndef PEER_H
#define PEER_H

#include <iostream>
#include <vector>
#include <thread>
#include <arpa/inet.h>
#include <unistd.h>
#include <sstream>
#include <cstring>
#include <sys/socket.h>

// Peer information storage structure
struct Peer
{
    std::string peer_id;
    std::string ip;
    int port;
};

// Function to start peer, send request to tracker
void send_announce_request(const std::string &tracker_ip, int tracker_port, const std::string &info_hash, const std::string &peer_id, int peer_port);

void send_scrape_request(const std::string &tracker_ip, int tracker_port, const std::string &info_hash);

void send_completed_request(const std::string &tracker_ip, int tracker_port, const std::string &info_hash, const std::string &peer_id);

void send_stopped_request(const std::string &tracker_ip, int tracker_port, const std::string &info_hash, const std::string &peer_id);

void send_peer_list_request(const std::string &tracker_ip, int tracker_port);

void send_file_list_request(const std::string &tracker_ip, int tracker_port);

#endif // PEER_H