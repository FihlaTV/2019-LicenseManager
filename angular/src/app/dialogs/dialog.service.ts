import { Injectable } from "@angular/core";
import { MatDialog } from '@angular/material';
import { ConfirmationDialogComponent } from './confirmation-dialog/confirmation-dialog.component';
import { LoginComponent } from '../login/login.component';

@Injectable({
    providedIn:'root'
})

export class DialogService {
    
    constructor(private dialog:MatDialog){}

    openConfirmDialog(msg,cb:boolean){
        return this.dialog.open(ConfirmationDialogComponent,{
            width: '390px',
            panelClass: 'confirm-dialog-container',
            disableClose: true,
            data :{
              message : msg,
              checkbox:cb,
              checkboxStatus:null,
            }
          });
    }

    openLoginDialog(){
      this.dialog.open(LoginComponent);
    }
}