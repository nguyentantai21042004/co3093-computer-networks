#ifndef TRACKER_H
#define TRACKER_H

#include <iostream>
#include <thread>
#include <cstring>
#include <unistd.h>
#include <arpa/inet.h>
#include <unordered_map>
#include <vector>
#include <algorithm>
#include <sstream>

// Structure for storing information about peers
struct PeerInfo
{
    std::string peer_id;
    std::string ip;
    int port;
    bool is_seeder; // Mark peer as seeder or leecher
};

// Table to store information about peers sharing files by info_hash
extern std::unordered_map<std::string, std::vector<PeerInfo>> peer_table;

void handle_scrape_request(int conn, const std::string &info_hash);

void handle_stopped_request(const std::string &info_hash, const std::string &peer_id);

// Function to handle announce request from peer
void handle_announce_request(int conn, const std::string &info_hash, const std::string &peer_id, int port);

// The function handles the completed request from the peer.
void handle_completed_request(const std::string &info_hash, const std::string &peer_id);

void handle_peer_list_request(int conn);
void handle_file_list_request(int conn);
// Function to handle each connection coming from peer
void new_connection(int conn);

// TCP server setup function for Tracker
void server_program(const std::string &host_ip, int port);

// Function to get the server IP address
std::string get_host_default_interface_ip();

#endif // TRACKER_H
