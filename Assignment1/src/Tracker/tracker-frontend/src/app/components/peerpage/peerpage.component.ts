import { Component, OnInit } from '@angular/core';
import { Peer } from '../../models/peer';
import { ApiResponse } from '../../response/api.response';
import { PeerService } from '../../service/peer.service';

@Component({
  selector: 'app-peers',
  templateUrl: './peerpage.component.html',
  styleUrls: ['./peerpage.component.scss'],
})
export class PeerpageComponent implements OnInit {
  peers: Peer[] = [];


  constructor(private peerService: PeerService) { }

  ngOnInit(): void {
    this.getPeers("");
  }

  // Xử lý tìm kiếm
  onSearch(event: Event): void {
    const searchTerm = (event.target as HTMLInputElement).value;
    this.peers = [];
    this.getPeers(searchTerm);
  }

  getPeers(searchTerm: string) {
    this.peerService.getPeers(searchTerm).subscribe({
      next: (response: ApiResponse) => {
        this.peers = response.data;
      },
      error: (error: any) => {
        console.log(error);
      }
    })
  }
}
