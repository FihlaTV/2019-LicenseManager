import { User } from '../login/login.service';

export interface StripeCard {
    customerStripeId:string;
    addressZip:string;
    id:string;
    last4:string;
    expMonth:number;
    expYear:number;

}