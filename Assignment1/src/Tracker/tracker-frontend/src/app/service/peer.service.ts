import { HttpClient } from "@angular/common/http";
import { Injectable } from "@angular/core";
import { HttpUtilService } from "./http.util.service";
import { environment } from "../environments/environment";
import { ApiResponse } from "../response/api.response";

@Injectable({
    providedIn: 'root',
})
export class PeerService {
    constructor(private http: HttpClient, private httpUtilService: HttpUtilService) {

    }

    getPeers(hashID: string) {
        return this.http.get<ApiResponse>(`${environment.apiBaseUrl}/peers`, {
            headers: this.httpUtilService.createHeaders(),
            params: { hash_id: hashID },
        });
    }
}           