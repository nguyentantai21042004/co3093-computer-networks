package Peer;

import Peer.src.connection.PeerClient;
import Peer.src.connection.PeerServer;
import Peer.src.file_management.DownloadFile;
import Peer.src.file_management.FileStatusManager;
import Peer.src.models.FileStatus;
import Peer.src.models.PieceStatus;
import Peer.src.request.AnnounceRequest;
import Peer.src.request.UploadRequest;

import java.io.IOException;
import java.util.Scanner;
import java.util.UUID;

import static Peer.src.connection.NetworkUtils.getHostDefaultInterfaceIp;

public class PeerApplication {
	private static final int SERVER_PORT = 2337; // Cổng mà peer sẽ lắng nghe
	private static final String TRACKER_URL = "http://192.168.88.159:8088";
	private static final String PEER_ID = generateUniquePeerID(); // Peer ID duy nhất
	private static final String PEER_IP = getHostDefaultInterfaceIp(); // Địa chỉ IP của peer này
	private static final int PEER_PORT = 6881; // Cổng mà peer này sử dụng
	private static final UploadRequest uploadRequest = new UploadRequest(); // Tạo đối tượng UploadRequest

	private static final AnnounceRequest announceRequest = new AnnounceRequest();

	private static String generateUniquePeerID() {
		return UUID.randomUUID().toString(); // Sinh ra một chuỗi UUID duy nhất
	}

	public static void main(String[] args) {
		// Tạo server P2P
		PeerServer peerServer = new PeerServer(PEER_IP, SERVER_PORT);
		Thread serverThread = new Thread(() -> peerServer.startServer());
		serverThread.start();



		// CLI - Giao diện để phân phối các nhiệm vụ
		Scanner scanner = new Scanner(System.in);
		boolean running = true;

		System.out.println("Peer đang chạy và sẵn sàng nhận yêu cầu...");

		// Vòng lặp CLI
		while (running) {
			System.out.println("\nChọn nhiệm vụ:");
			System.out.println("1. Gửi announce tới Tracker");
			System.out.println("2. Kết nối tới Peer khác và tải dữ liệu");
			System.out.println("3. Xem trạng thái file (file_status.json)");
			System.out.println("4. Upload file lên Tracker");  // Thêm lựa chọn cho việc upload file
			System.out.println("5. Thoát Peer");

			int choice = scanner.nextInt();

			switch (choice) {
				case 1:
					// Gửi announce tới Tracker
					System.out.println("Gửi announce tới Tracker...");

					System.out.println("Nhập đường dẫn tới file .torrent để gửi announce:");
					scanner.nextLine(); // Đọc phần dòng mới còn lại sau khi nhập số
					String torrentFilePath = scanner.nextLine();

					// Gửi announce request
					announceRequest.sendAnnounceRequest(PEER_IP, SERVER_PORT, torrentFilePath);
					break;

				case 2:
					// Kết nối tới Peer khác và tải dữ liệu
					System.out.println("Kết nối tới peer khác để tải dữ liệu...");

					// Nhập tên file hoặc hash_id
					System.out.println("Nhập tên file hoặc hash_id của file:");
					scanner.nextLine(); // Đọc phần dòng mới còn lại sau khi nhập số
					String hashIdOrFileName = scanner.nextLine();

					// Gọi hàm để tải file dựa trên hash_id hoặc tên file
					DownloadFile.downloadFileByHashId(hashIdOrFileName);
					break;

				case 3:
					// Xem trạng thái của file (file_status.json)
					System.out.println("Nhập đường dẫn tới file file_status.json:");
					scanner.nextLine(); // Đọc dòng mới
					String fileStatusPath = scanner.nextLine();

					try {
						FileStatus fileStatus = (FileStatus) FileStatusManager.readFileStatus(fileStatusPath);
						displayFileStatus(fileStatus);
					} catch (IOException e) {
						System.out.println("Không thể đọc file trạng thái: " + e.getMessage());
					}
					break;

				case 4:
					// Upload file lên Tracker
					System.out.println("Nhập đường dẫn tới file để upload:");
					scanner.nextLine(); // Đọc phần dòng mới còn lại sau khi nhập số
					String filePath = scanner.nextLine();
					uploadRequest.uploadFile(PEER_IP, SERVER_PORT, filePath);  // Gọi hàm upload file
					break;

				case 5:
					// Thoát chương trình
					System.out.println("Đang dừng Peer...");
					running = false;
					break;

				default:
					System.out.println("Lựa chọn không hợp lệ. Hãy chọn lại.");
					break;
			}
		}

		// Kết thúc server khi peer dừng
		peerServer.stopServer();
		scanner.close();
	}

	// Hàm hiển thị trạng thái của file
	private static void displayFileStatus(FileStatus fileStatus) {
		System.out.println("\nThông tin file:");
		System.out.println("Tên file: " + fileStatus.file_name);
		System.out.println("Kích thước file: " + fileStatus.file_length + " bytes");
		System.out.println("Kích thước mỗi piece: " + fileStatus.piece_length + " bytes");
		System.out.println("\nTrạng thái các pieces:");

		for (PieceStatus piece : fileStatus.pieces) {
			System.out.println("Piece #" + piece.getIndex() + " | Hash: " + piece.getHash() + " | Trạng thái: " + piece.getStatus());
		}
	}
}
