import { Component } from '@angular/core';

@Component({
  selector: 'app-upload',
  templateUrl: './upload.component.html',
  styleUrls: ['./upload.component.scss'],
})
export class UploadComponent {
  fileData = {
    fileName: '',
    fileSize: 0,
    description: '',
  };

  selectedFile: File | null = null;

  // Xử lý khi file được chọn
  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files.length > 0) {
      this.selectedFile = input.files[0];

      // Điền giá trị mặc định dựa trên thông tin file
      this.fileData.fileName = this.getFileName(this.selectedFile.name); // Lấy tên file
      this.fileData.fileSize = this.calculateFileSize(this.selectedFile.size); // Tính kích thước file (MB)
      this.fileData.description = `File ${this.selectedFile.name} được tải lên vào ${new Date().toLocaleString()}`; // Tạo mô tả mặc định
    }
  }

  // Hàm xử lý tên file
  getFileName(fullFileName: string): string {
    return fullFileName.split('.').slice(0, -1).join('.') || fullFileName;
  }

  // Hàm tính kích thước file
  calculateFileSize(sizeInBytes: number): number {
    return Math.round((sizeInBytes / (1024 * 1024)) * 100) / 100; // Chuyển đổi từ bytes sang MB với 2 chữ số thập phân
  }

  // Gửi form
  onSubmit(form: any): void {
    if (!this.selectedFile) {
      alert('Vui lòng chọn một file để upload!');
      return;
    }

    const formData = new FormData();
    formData.append('file', this.selectedFile);
    formData.append('fileName', this.fileData.fileName);
    formData.append('fileSize', this.fileData.fileSize.toString());
    formData.append('description', this.fileData.description);

    // API Call - Giả sử gọi API để upload file
    console.log('Uploading file:', formData);
    alert('Tải lên thành công!');
    form.reset();
  }
}
