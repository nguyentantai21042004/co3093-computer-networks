import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { HttpClientModule } from '@angular/common/http';
import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app/app.component';
import { FilepageComponent } from './components/filepage/filepage.component';
import { PeerpageComponent } from './components/peerpage/peerpage.component';
import { HomepageComponent } from './components/homepage/homepage.component';
import { UploadComponent } from './components/upload/upload.component';
import { FormsModule } from '@angular/forms';

@NgModule({
  declarations: [
    AppComponent,
    FilepageComponent,
    PeerpageComponent,
    HomepageComponent,
    UploadComponent
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    HttpClientModule,
    FormsModule
  ],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule { }
