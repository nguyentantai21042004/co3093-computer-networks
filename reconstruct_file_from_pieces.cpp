#include <iostream>
#include <fstream>
#include <vector>
#include <string>

// Hàm ghép các khối lại thành file hoàn chỉnh
void reconstruct_file_from_pieces(const std::string &output_file, const std::vector<std::string> &pieces)
{
    std::ofstream file(output_file, std::ios::binary);
    if (!file.is_open())
    {
        std::cerr << "Không thể mở file đầu ra!" << std::endl;
        return;
    }

    // Ghép tất cả các khối lại thành file hoàn chỉnh
    for (const auto &piece : pieces)
    {
        file.write(piece.c_str(), piece.size());
    }

    file.close();
    std::cout << "File hoàn chỉnh đã được tạo: " << output_file << std::endl;
}

int main()
{
    std::vector<std::string> pieces = {
        "7110eda4d09e062aa5e4a390b0a572ac0d2c0220", // Giả sử đây là các khối dữ liệu bạn nhận được hoặc lưu trữ đâu đó
        "fea7f657f56a2a448da7d4b535ee5e279caf3d9a", // Trong thực tế, khối này sẽ được tải từ mạng P2P
        "1c6637a8f2e1f75e06ff9984894d6bd16a3a36a9"};

    std::string output_file = "reconstructed_file.txt"; // Tên file hoàn chỉnh

    // Tạo file hoàn chỉnh từ các khối dữ liệu
    reconstruct_file_from_pieces(output_file, pieces);

    return 0;
}
