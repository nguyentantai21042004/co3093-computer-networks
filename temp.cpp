#include <iostream>
#include <fstream>
#include <vector>
#include <string>

constexpr size_t PIECE_SIZE = 4; // Kích thước mỗi khối là 4 bytes

// Hàm chia file thành các khối dữ liệu
std::vector<std::string> split_file_into_pieces(const std::string &file_path)
{
    std::ifstream file(file_path, std::ios::binary); // Mở file gốc ở chế độ nhị phân
    std::vector<std::string> pieces;

    if (!file.is_open())
    {
        std::cerr << "Không thể mở file gốc: " << file_path << std::endl;
        return pieces;
    }

    std::string piece;
    piece.resize(PIECE_SIZE); // Khởi tạo khối với kích thước PIECE_SIZE

    // Đọc từng khối dữ liệu từ file gốc
    while (file.read(&piece[0], PIECE_SIZE) || file.gcount() > 0)
    {
        piece.resize(file.gcount()); // Điều chỉnh kích thước cho khối cuối cùng nếu nó nhỏ hơn PIECE_SIZE
        pieces.push_back(piece);     // Lưu trữ khối vào vector
        piece.resize(PIECE_SIZE);    // Đặt lại kích thước khối cho lần đọc tiếp theo
    }

    file.close();
    return pieces;
}

// Hàm hiển thị các khối dữ liệu đã tạo ra
void print_pieces(const std::vector<std::string> &pieces)
{
    for (size_t i = 0; i < pieces.size(); ++i)
    {
        std::cout << "Khối " << i << ": " << pieces[i] << std::endl;
    }
}

int main()
{
    std::string file_path = "src.txt"; // Đường dẫn tới file gốc

    // Chia file gốc thành các khối dữ liệu
    std::vector<std::string> pieces = split_file_into_pieces(file_path);

    // Hiển thị các khối dữ liệu đã tạo ra
    print_pieces(pieces);

    return 0;
}
