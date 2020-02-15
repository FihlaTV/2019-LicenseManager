import { Component, OnInit } from '@angular/core';
import { Product } from '../product/product.model';
import { ProductService } from '../product/product.service';
import { Router } from '@angular/router';

const BASE_URL_PRODUCT = "http://localhost:8080/api/product/"

@Component({
  selector: 'app-catalog',
  templateUrl: './catalog.component.html',
  styleUrls: ['./catalog.component.css']
})
export class CatalogComponent {

  products:Product[];
  searchInputTerm:string;

  constructor(private productService:ProductService, private router:Router){
      productService.getProducts().subscribe( 
          (resp:any) =>{console.log(resp); this.products = resp.content},
          error => console.log(error)
      );
  }

  goToProduct(name:string){
    this.router.navigate(["/product/",name]);
  }

  searchProduct(){
    this.productService.getProductSearch(this.searchInputTerm).subscribe(
      (ps:any) => this.products = ps.content,
      error => console.log(error)
    )
  }

  pathPhotos(productName:string){
    return BASE_URL_PRODUCT + productName +"/image";
  }

}