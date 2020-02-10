package tfg.licensoft.api;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import tfg.licensoft.licenses.*;
import tfg.licensoft.products.Product;
import tfg.licensoft.products.ProductService;
import tfg.licensoft.users.User;
import tfg.licensoft.users.UserComponent;
import tfg.licensoft.users.UserService;

import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.Subscription;


@CrossOrigin
@RestController
@RequestMapping(value = "/api/licenses")
public class ApiLicenseController {

	@Autowired 
	private LicenseService licServ;
	
	@Autowired
	private ProductService productServ;
	
	@Autowired
	private UserComponent userComp;
	
	@Autowired
	private UserService userServ;
	

	@PostMapping(value = "/")
	public ResponseEntity<License> postLicense(@RequestBody License license){
		Product p = this.productServ.findOne(license.getProduct().getName());
		if (this.licServ.getSubTypes().contains(license.getType()) && p!=null) {
			License l = this.licServ.findOne(license.getSerial());
			if (l==null) {
				license.setProduct(p);
				this.licServ.save(license);
				p.getLicenses().add(license);
				return new ResponseEntity<>(license, HttpStatus.CREATED);
			}
			else {
				return new ResponseEntity<>(HttpStatus.CONFLICT);
			}
		}else {
			return new ResponseEntity<>(HttpStatus.NOT_ACCEPTABLE);
		}
	}
	
	@GetMapping(value = "/product/{product}")
	public Page<License> getLicensesOfProduct(@PathVariable String product, Pageable page){
		Product p = this.productServ.findOne(product);
		Page<License> licenses = this.licServ.findLicensesOfProduct(p,page);
		return licenses;
		
	}
	
	@GetMapping(value = "/{serial}/{product}")
	public ResponseEntity<License> getLicenseBySerialAndProduct(@PathVariable String serial, @PathVariable String product){
		Product p = this.productServ.findOne(product);
		License license = this.licServ.findBySerialAndProduct(serial, p);
		if (license!=null) {
			return new ResponseEntity<License>(license, HttpStatus.OK);
		}else {
			return new ResponseEntity<License>(HttpStatus.NOT_FOUND);
		}
	} 
	
	@DeleteMapping(value = "/{serial}/{product}")
	public ResponseEntity<License> deleteLicenseBySerial(@PathVariable String serial, @PathVariable String product){
		Product p = this.productServ.findOne(product);
		License license = this.licServ.findBySerialAndProduct(serial,p);
		if (license!=null) {
			this.licServ.delete(license);
			return new ResponseEntity<License>(license, HttpStatus.OK);
		}else {
			return new ResponseEntity<License>(HttpStatus.NOT_FOUND);
		}
	}
	
	@GetMapping(value = "/{userName}")
	public ResponseEntity<Page<License>> getLicensesOfUser(@PathVariable String userName, Pageable page){
		Page<License> licenses = this.licServ.findByUsername(userName, page);
		if(licenses==null) {
			return new ResponseEntity<Page<License>>(HttpStatus.NOT_FOUND);
		}else { 
			return new ResponseEntity<Page<License>>(licenses,HttpStatus.OK);
		}
	}
	
	@PutMapping(value="/cancelAtEnd/{serial}/{product}")
	public ResponseEntity<License> cancelAtEndLicense(@PathVariable String serial, @PathVariable String product){
		Product p = this.productServ.findOne(product);
		License l = this.licServ.findBySerialAndProduct(serial,p);
		boolean newCancelAtEnd = !l.getCancelAtEnd();
		if(l!=null && p!=null) {
			User u = this.userServ.findByName(l.getOwner());
			try {
				Customer c = Customer.retrieve(u.getCustomerStripeId());
				
				for(Subscription s:c.getSubscriptions().getData()) {
					System.out.println(s.getPlan().getNickname() + " ->");
					if(s.getPlan().getNickname().equals(l.getType())) {
							com.stripe.model.Product productStripe = com.stripe.model.Product.retrieve(s.getPlan().getProduct());
							if(productStripe.getName().equals(p.getName())){
								Map<String, Object> params = new HashMap<>();
								params.put("cancel_at_period_end", newCancelAtEnd);
								//s.cancel(params);
								s.update(params);
								l.setCancelAtEnd(newCancelAtEnd);
								this.licServ.save(l);
							}

					}
					

				}
				
			} catch (StripeException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			/* 
			if(l.isActive()) {
				l.setActive(false);
				l.setEndDate(new Date());
			}else {
				l.setActive(true);
				l.setStartDate(new Date());
			}
			
			this.licServ.save(l);
			*/
			return new ResponseEntity<License>(l,HttpStatus.OK);
		}else {
			return new ResponseEntity<License>(HttpStatus.PRECONDITION_REQUIRED);
		}
	}
	
	@PutMapping(value="/update/")
	public ResponseEntity<License> updateLicense(@RequestBody License license){
		Product p = this.productServ.findOne(license.getProduct().getName());
		License l = this.licServ.findBySerialAndProduct(license.getSerial(),p);
		if(l!=null && p!=null) {
			l.setType(license.getType());
			l.setEndDate(license.getEndDate());
			l.setOwner(license.getOwner());
			l.setSerial(license.getSerial()); 
			l.setStartDate(license.getStartDate());
			l.setActive(license.isActive());

			this.licServ.save(l);
			return new ResponseEntity<License>(l,HttpStatus.OK);
		}else {
			return new ResponseEntity<License>(HttpStatus.NOT_FOUND);
		}
	}
	
	@GetMapping(value="/user/{userName}/product/{productName}")
	public ResponseEntity<Page<License>> getLicensesOfUserAndProduct(@PathVariable String productName, @PathVariable String userName, Pageable page){
		Product p = this.productServ.findOne(productName);
		User user = this.userComp.getLoggedUser();
		
		if(!user.getName().equals(userName)) { //Si no se llaman igual el usuario pasado y el logueado
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
		}
		
		if(p==null) { //Si el producto no existe
			return new ResponseEntity<>(HttpStatus.NOT_ACCEPTABLE);
		}
		
		Page<License> licenses=this.licServ.findByProductAndOwner(p, userName, page);
		return new ResponseEntity<>(licenses,HttpStatus.OK);
		
		
		
	}
	
}
