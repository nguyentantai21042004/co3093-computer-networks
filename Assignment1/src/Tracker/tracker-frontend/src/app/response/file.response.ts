export interface FileListResponse {
    files: FileResponse[];
    totalPages: number;
}

export interface FileResponse {
    id: string;
    name: string;
    size: number;
}

