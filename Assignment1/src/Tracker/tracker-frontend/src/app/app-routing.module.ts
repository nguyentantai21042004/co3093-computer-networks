import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { FilepageComponent } from './components/filepage/filepage.component';
import { PeerpageComponent } from './components/peerpage/peerpage.component';
import { HomepageComponent } from './components/homepage/homepage.component';
import { UploadComponent } from './components/upload/upload.component';

const routes: Routes = [
  { path: '', component: HomepageComponent },
  { path: 'files', component: FilepageComponent },
  { path: 'peers', component: PeerpageComponent },
  { path: 'upload', component: UploadComponent },
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
