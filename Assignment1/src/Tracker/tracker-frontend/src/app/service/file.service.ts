import { Injectable } from "@angular/core";
import { environment } from "../environments/environment";
import { HttpClient } from "@angular/common/http";
import { HttpUtilService } from "./http.util.service";
import { ApiResponse } from "../response/api.response";

@Injectable({
    providedIn: 'root',
})
export class FileService {
    constructor(private http: HttpClient, private httpUtilService: HttpUtilService) { }

    getFiles(hashID: string) {
        return this.http.get<ApiResponse>(`${environment.apiBaseUrl}/files`, {
            headers: this.httpUtilService.createHeaders(),
            params: { hash_id: hashID },
        });
    }
}