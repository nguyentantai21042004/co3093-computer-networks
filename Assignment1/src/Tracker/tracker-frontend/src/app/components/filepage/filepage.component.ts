import { Component, OnInit } from '@angular/core';
import { ApiResponse } from '../../response/api.response';
import { FileService } from '../../service/file.service';
import { File } from '../../models/file';
@Component({
  selector: 'app-files',
  templateUrl: './filepage.component.html',
  styleUrls: ['./filepage.component.scss'],
})
export class FilepageComponent implements OnInit {
  files: File[] = [];

  constructor(private fileService: FileService) {

  }

  ngOnInit(): void {
    debugger
    this.getFiles("");
  }

  // Xử lý tìm kiếm
  onSearch(event: Event): void {
    const searchTerm = (event.target as HTMLInputElement).value;
    this.files = [];
    this.getFiles(searchTerm);
  }

  getFiles(hashID: string) {
    this.fileService.getFiles(hashID).subscribe({
      next: (response: ApiResponse) => {
        this.files = response.data;
      },
      error: (error: any) => {
        console.log(error);
      }
    })

    console.log(this.files);
  }
}
