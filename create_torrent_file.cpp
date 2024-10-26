#include <iostream>
#include <fstream>
#include <sstream>
#include <vector>
#include <openssl/sha.h>
#include <iomanip>
#include <cmath>
#include <filesystem>
#include "json.hpp"

using json = nlohmann::json; // Áp dụng cho json

constexpr size_t PIECE_SIZE = 4; // Kích thước mỗi khối là 4 bytes

// Hàm tính SHA-1 cho một chuỗi dữ liệu
std::string sha1(const std::string &data)
{
    unsigned char hash[SHA_DIGEST_LENGTH];
    SHA1(reinterpret_cast<const unsigned char *>(data.c_str()), data.size(), hash);

    std::stringstream ss;
    for (int i = 0; i < SHA_DIGEST_LENGTH; ++i)
    {
        ss << std::hex << std::setw(2) << std::setfill('0') << (int)hash[i];
    }
    return ss.str();
}

// Hàm chia file thành các khối (pieces) và tính băm SHA-1 cho từng khối
std::vector<std::string> split_file_into_pieces(const std::string &file_path)
{
    std::ifstream file(file_path, std::ios::binary);
    std::vector<std::string> piece_hashes;

    if (!file.is_open())
    {
        std::cerr << "Không thể mở file: " << file_path << std::endl;
        return piece_hashes;
    }

    std::string piece;
    piece.resize(PIECE_SIZE); // Khởi tạo khối với kích thước PIECE_SIZE
    while (file.read(&piece[0], PIECE_SIZE) || file.gcount() > 0)
    {
        piece.resize(file.gcount());         // Cắt bỏ các phần thừa nếu khối cuối nhỏ hơn PIECE_SIZE
        piece_hashes.push_back(sha1(piece)); // Tính băm SHA-1 cho khối
        piece.resize(PIECE_SIZE);            // Đặt lại kích thước khối cho lần đọc tiếp theo
    }

    return piece_hashes;
}

void create_torrent_file(const std::string &file_path, const std::string &tracker_url)
{
    std::string torrent_file_name = file_path + ".torrent";
    std::ofstream torrent_file(torrent_file_name);

    if (!torrent_file.is_open())
    {
        std::cerr << "Could not create torrent file: " << torrent_file_name << std::endl;
        return;
    }

    // Tính toán các giá trị băm SHA-1 cho từng khối của file
    std::vector<std::string> piece_hashes = split_file_into_pieces(file_path);

    // Ghi nội dung file .torrent theo định dạng Bencode
    torrent_file << "d";                                                      // Bắt đầu từ điển Bencode
    torrent_file << "8:announce" << tracker_url.size() << ":" << tracker_url; // Tracker URL

    // Phần từ điển info
    torrent_file << "4:info";
    torrent_file << "d";                                                               // Bắt đầu từ điển info
    torrent_file << "6:length" << "i" << std::filesystem::file_size(file_path) << "e"; // Độ dài file
    torrent_file << "4:name" << file_path.size() << ":" << file_path;                  // Tên file
    torrent_file << "12:piece length" << "i" << PIECE_SIZE << "e";                     // Kích thước khối

    // Chuỗi nối của tất cả các giá trị băm SHA-1
    torrent_file << "6:pieces" << piece_hashes.size() * 20 << ":";
    for (const auto &hash : piece_hashes)
    {
        torrent_file << hash;
    }

    torrent_file << "e"; // Kết thúc từ điển info
    torrent_file << "e"; // Kết thúc từ điển Bencode

    torrent_file.close();

    std::cout << "Torrent file created: " << torrent_file_name << std::endl;
}

// Hàm tạo file "file status.json"
void create_file_status(const std::string &torrent_file, const std::string &file_path)
{
    // Sử dụng lại các giá trị từ quá trình tạo torrent
    size_t file_size = std::filesystem::file_size(file_path);                  // Kích thước file
    std::vector<std::string> piece_hashes = split_file_into_pieces(file_path); // Hash của các khối từ file

    // Tạo đối tượng JSON
    json status_json;
    status_json["file_name"] = file_path;
    status_json["file_size"] = file_size;
    status_json["piece_length"] = PIECE_SIZE;

    // Tạo danh sách các khối với trạng thái ban đầu là "incomplete"
    for (size_t i = 0; i < piece_hashes.size(); ++i)
    {
        json piece;
        piece["piece_index"] = i;
        piece["hash"] = piece_hashes[i];
        piece["status"] = "incomplete"; // Khởi tạo trạng thái "incomplete" cho từng khối
        status_json["pieces"].push_back(piece);
    }

    // Ghi đối tượng JSON vào file status
    std::ofstream status_file_stream("file_status.json");
    if (status_file_stream.is_open())
    {
        status_file_stream << status_json.dump(4); // Ghi file với định dạng đẹp (indent 4 spaces)
        status_file_stream.close();
        std::cout << "File status.json đã được tạo thành công!" << std::endl;
    }
    else
    {
        std::cerr << "Không thể tạo file status.json!" << std::endl;
    }
}

int main()
{
    std::string file_path = "src.txt";                               // Đường dẫn tới file gốc
    std::string tracker_url = "http://example-tracker.com/announce"; // URL tracker

    // Tạo file torrent
    create_torrent_file(file_path, tracker_url);
    // Tạo file status dựa trên thông tin file gốc và file torrent
    create_file_status("src.txt.torrent", file_path);
    return 0;
}