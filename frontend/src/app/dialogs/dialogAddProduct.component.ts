import { Component, Inject } from "@angular/core";
import { Product } from '../product/product.model';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material';
import { License } from '../licenses/license.model';
import { ProductService } from '../product/product.service';

@Component({
    selector: 'app-dialogAddProduct',
    templateUrl: './dialogAddProduct.component.html',
    styleUrls: ['./dialogAddProduct.component.css']

  })
  export class DialogAddProductComponent {

    lifetime:boolean;
    annual:boolean;
    monthly:boolean;
    daily:boolean;
    type:string;
    name:string;
    description:string;
    webLink:string;
    priceDaily:number;
    priceMonthly:number;
    priceAnnual:number;
    price:number;
    file:File;
    edit:boolean;

    productEdit:Product;

    constructor(@Inject(MAT_DIALOG_DATA) public data:any,private dialogRef:MatDialogRef<DialogAddProductComponent>, private productServ:ProductService){
      if(data.type=="edit"){
        this.edit=true;
        let prod:Product = data.product;
        this.name=prod.name;
        this.description=prod.description;
        this.webLink=prod.webLink;
        this.productEdit=prod;
        prod.typeSubs.forEach(function (value){
          if (value=="L"){
            this.lifetime=true;
            this.type="lifetime";
            this.price=prod.plansPrices["L"];
          }else if(value=="D"){
            this.daily=true;
            this.type="subscription";
            this.priceDaily=prod.plansPrices["D"];
          }else if(value=="M"){
            this.monthly=true;
            this.type=="subscription";
            this.priceMonthly=prod.plansPrices["M"];
          }else if (value=="A"){
            this.annual=true;
            this.type="subscription"
            this.priceAnnual=prod.plansPrices["A"];
          }
        }.bind(this))
      }else{
        this.edit=false;
        this.daily=false;
        this.annual=false;
        this.monthly=false;
      }

    }
      
  close(){
      this.dialogRef.close();
  }

  add(){
    let isFile;
    if(this.file!=null){
      isFile=true;
    }else{
      isFile=false;
    }
    let licenses:License[];
    licenses=[];
    let typeSubs:string[];
    typeSubs = [];
    let plansPricesN:{ [name: string]: number }={};
    if (this.type==='subscription'){
      if (this.daily){
        typeSubs.push('D');
        plansPricesN['D']=this.priceDaily;
      }
      if (this.monthly){
        typeSubs.push('M');
        plansPricesN['M']=this.priceMonthly;
      }
      if (this.annual){
        typeSubs.push('A');
        plansPricesN['A']=this.priceAnnual;
      }
    }else if(this.type==='lifetime'){
      typeSubs.push('L');
      plansPricesN['L']=this.price;
    }
    let prod:Product = {name: this.name, licenses:licenses, typeSubs:typeSubs,photoAvailable:isFile,description: this.description,webLink:this.webLink,photoSrc:"",plansPrices:plansPricesN,sku:null, active:true};
    this.productServ.postProduct(prod).subscribe(
      g => {
        if(this.file!=null){
           this.uploadEvent(this.file);
    }this.dialogRef.close()},
      error => console.log(error)
    )
  }

  save(){
    let isFile;
    if(this.file!=null){
      isFile=true;
    }else{
      isFile=false;
    }
    let prod:Product = {name: this.productEdit.name, licenses:this.productEdit.licenses, typeSubs:this.productEdit.typeSubs,photoAvailable:isFile,description: this.description,webLink:this.webLink,photoSrc:"",plansPrices:this.productEdit.plansPrices,sku:this.productEdit.sku, active:true};
    this.productServ.putProduct(prod).subscribe(
      g => {if(this.file!=null){
        this.uploadEvent(this.file);
 }this.dialogRef.close()},
      error => console.log(error)
    )
  }

  selectEvent(file: File): void {
    this.file=file;
  }

  uploadEvent(file: File): void {
    this.productServ.addImage(this.file, this.name).subscribe(
      u => {  },
      error => console.log(error)
    );
  }

  cancelEvent(): void {
  }

  }