import {Component, OnInit, Output, EventEmitter} from '@angular/core';

import {ElastestEusDialogService} from './elastest-eus.dialog.service';
import {EusService} from './elastest-eus.service';

@Component({
  selector: 'app-elastest-eus',
  templateUrl: './elastest-eus.component.html',
  styleUrls: ['./elastest-eus.component.scss'],
})
export class ElastestEusComponent implements OnInit {

  componentTitle: string = "ElasTest User Emulator Service (EUS)";
  sessionId: string = "";
  vncUrl: string = "";
  loading: boolean = false;

  selectedBrowser: string;
  browsers = [
    'chrome',
    'firefox'
  ];

  selectedVersion: string;
  browserVersions = {
    'chrome': [
      {value: '59', viewValue: '59'},
      {value: '58', viewValue: '58'},
      {value: '57', viewValue: '57'}
    ],
    'firefox': [
      {value: '54', viewValue: '54'},
      {value: '53', viewValue: '53'},
      {value: '52', viewValue: '52'}
    ]
  };

  @Output()
  onInitComponent = new EventEmitter<string>();

  constructor(private eusService: EusService, private eusDialog: ElastestEusDialogService) {}

  ngOnInit() {
  }

  startSession() {
    if (this.selectedBrowser) {
      this.loading = true;
      this.eusService.startSession(this.selectedBrowser, this.selectedVersion).subscribe(
        id => {
          this.sessionId = id;
          this.eusService.getVncUrl(this.sessionId).subscribe(
            url => {
              let message = this.capitalize(this.selectedBrowser);
              if (this.selectedVersion) {
                message += " " + this.selectedVersion;
              }
              message += " - live session";
              this.eusDialog.open(message, "", true, url)
                .subscribe(
                result => this.stopSession()
                );
              this.loading = false;
            },
            error => console.error(error)
          );
        },
        error => console.error(error)
      );
    }
    else {
      this.eusDialog.open('Browser not selected', 'You need to chose one browsers to start a session', false, "")
        .subscribe();
    }
  }

  stopSession() {
    this.eusService.stopSession(this.sessionId).subscribe(
      ok => this.vncUrl = null,
      error => console.error(error)
    );
  }

  selectBrowser(browserValue) {
    this.selectedBrowser = browserValue;
  }

  capitalize(value: any) {
    if (value) {
      return value.charAt(0).toUpperCase() + value.slice(1);
    }
    return value;
  }

}
