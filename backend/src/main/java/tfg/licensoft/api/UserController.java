package tfg.licensoft.api;

import tfg.licensoft.licenses.License;
import tfg.licensoft.licenses.LicenseService;
import tfg.licensoft.products.Product;
import tfg.licensoft.products.ProductService;
import tfg.licensoft.users.User;
import tfg.licensoft.users.UserComponent;
import tfg.licensoft.users.UserService;

import org.springframework.web.bind.annotation.*;

import com.stripe.exception.StripeException;
import com.stripe.model.Card;
import com.stripe.model.CardCollection;
import com.stripe.model.Customer;
import com.stripe.model.Order;
import com.stripe.model.PaymentIntent;
import com.stripe.model.PaymentSource;
import com.stripe.model.PaymentSourceCollection;
import com.stripe.model.Plan;
import com.stripe.model.Subscription;
import com.stripe.model.Token;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@CrossOrigin
@RestController
@RequestMapping(value = "/api/user")
public class UserController {

	@Autowired
	private LicenseService licenseServ;
	
	@Autowired
	private UserService userServ;
	
	@Autowired
	private UserComponent userComponent;
	
	@Autowired
	private ProductService productServ;
	
	
	/*
	//TODO error
	@PutMapping("/")
	private ResponseEntity<User> updateStripeCards(@RequestBody User updatedUser) {
		User user = userServ.findByName(updatedUser.getName());
		User userLogged = userComponent.getLoggedUser(); 
		if (user!=null && user.getId()==userLogged.getId()) {
			System.out.println("++++ " + user);
			user.getStripeCards().addAll(updatedUser.getStripeCards());
			userServ.save(user);
			return new ResponseEntity<User>(user,HttpStatus.OK);
		}else {
			return new ResponseEntity<User>(HttpStatus.NOT_FOUND);
		}
	}
	
	
	@PostMapping("{userName}/addCard")
	public ResponseEntity<StripeCard> addCard(@PathVariable String userName,@RequestBody StripeCard stripeCard) {
		User user = userComponent.getLoggedUser();
	//	User user = this.userServ.findByName(userName);
		try {
			Customer c = Customer.retrieve(user.getCustomerStripeId());
			Map<String,Object> cardParam = new HashMap<String,Object>();
			cardParam.put("number", stripeCard.getNumber());
			cardParam.put("cvc",stripeCard.getCvv());
			cardParam.put("exp_month",stripeCard.getExp_month());
			cardParam.put("exp_year",stripeCard.getExp_year());
			
			Map<String,Object> tokenParam = new HashMap<String,Object>();
			tokenParam.put("card", cardParam);
			
			Token token = Token.create(tokenParam);
			
			Map<String,Object> source = new HashMap<String,Object>();
			source.put("source", token.getId());
			
			c.getSources().create(source);
			
			stripeCard.setUser(user);
			this.stripeCardServ.save(stripeCard);
			
			return new ResponseEntity<StripeCard>(stripeCard,HttpStatus.OK);
		} catch (StripeException e) {
			e.printStackTrace();
			return new ResponseEntity(HttpStatus.NOT_FOUND);
		}
		
		
	}
	*/
	
	@PostMapping("/addCard/{tokenId}")
	public ResponseEntity<User> addCardStripeElements(@PathVariable String tokenId){
		User user = userComponent.getLoggedUser();
		try {
			Customer c = Customer.retrieve(user.getCustomerStripeId());
			Map<String,Object> source = new HashMap<String,Object>();
			source.put("source", tokenId);
			c.getSources().create(source);
			return new ResponseEntity<User>(user,HttpStatus.OK);

		} catch (StripeException e) {
			e.printStackTrace();
			return new ResponseEntity(HttpStatus.NOT_FOUND);
		}
 
	}
	

	
	@PutMapping("/{productName}/{typeSubs}/{userName}/addSubscription/renewal/{automaticRenewal}")
	public ResponseEntity<License> addSubscription(@PathVariable String productName, @PathVariable String typeSubs, @PathVariable String userName, @PathVariable boolean automaticRenewal){
		Product product = this.productServ.findOne(productName);
		User user = userComponent.getLoggedUser();
		if(user==null || !user.getName().equals(userName)) {
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
		}
		//User user = this.userServ.findByName(userName);
		Map<String,String> plans = product.getPlans();
				 
		String planId = plans.get(typeSubs);
		if (planId!=null && product!=null) {
			List<License> list= this.licenseServ.findByProductAndOwnerAndType(product, user.getName(), typeSubs);
			if (list.isEmpty()) {
				Map<String, Object> item = new HashMap<>();
				item.put("plan", planId);
				Map<String, Object> items = new HashMap<>();
				items.put("0", item);
				Map<String, Object> expand = new HashMap<>();
				expand.put("0", "latest_invoice.payment_intent");
				Map<String, Object> params = new HashMap<>();
				params.put("customer", user.getCustomerStripeId());
				params.put("items", items);
				params.put("expand", expand);
				params.put("cancel_at_period_end", !automaticRenewal);

				try {
					Subscription subscription = Subscription.create(params);
				} catch (StripeException e) {
					if(e.getCode().equals("resource_missing") && e.getMessage().contains("This customer has no attached payment source")) {
						return new ResponseEntity<License>(HttpStatus.PRECONDITION_REQUIRED); //The precondition is to have an attached payment source
					}
				}
				this.productServ.save(product);			
				License license = new License(true, typeSubs, product, user.getName());
				license.setCancelAtEnd(!automaticRenewal);
				licenseServ.save(license);
				if(automaticRenewal) {
					this.setTimerAndEndDate(license);
				}
				return new ResponseEntity<License>(license,HttpStatus.OK);
			}else {
				return new ResponseEntity<License>(HttpStatus.CONFLICT);
			}
			
		}else {
			return new ResponseEntity<License>(HttpStatus.BAD_REQUEST);
		}
	}
	
	private void setTimerAndEndDate(License license) {
		Timer time = new Timer();
		time.schedule(new TimerTask() {

			@Override
			public void run() {
				license.renew();
				System.out.println(new Date().toGMTString());
				License newL = licenseServ.save(license);
				setTimerAndEndDate(newL);
			}
			
		}, 
				license.getEndDate()
		);
	}
	
	
	@PutMapping("/{userName}/buy/{productName}/{token}")
	public ResponseEntity<License> buyProduct(@PathVariable String productName, @PathVariable String userName, @PathVariable String token){
		Product product = this.productServ.findOne(productName);
		User user = userComponent.getLoggedUser();
		System.out.println(user + " " + product);


		if(token!=null && product!=null && user!=null && user.getName().equals(userName)) {
			System.out.println("Dentro");
			List<Object> items = new ArrayList<>();
			Map<String, Object> item1 = new HashMap<>();
			item1.put("type", "sku");
			item1.put("parent", product.getSku());
			items.add(item1);
			Map<String, Object> params = new HashMap<>();
			params.put("currency", "eur");
			params.put("email", user.getName()+"@test.com");
			params.put("items", items);
			params.put("customer", user.getCustomerStripeId());

			try {
				Order order = Order.create(params);
				Map<String, Object> paramsNew = new HashMap<>();
				paramsNew.put("source", token);

				order = order.pay(paramsNew); //Paying order , state changed to Paid
				
				paramsNew.clear();
				
				paramsNew.put("status", "fulfilled");
				
				License license = new License(true, "L", product, user.getName());
				licenseServ.save(license);
				
				order = order.update(paramsNew);  //Order state changed to Fulfilled
				
				return new ResponseEntity<>(license,HttpStatus.OK);
			} catch (StripeException e) {
				e.printStackTrace();
				return new ResponseEntity<>(HttpStatus.CONFLICT);

			}
		}
		return new ResponseEntity<>(HttpStatus.NOT_FOUND);

	}

	
	/* TODO
	@GetMapping("/stripeCardsBDStripe")
	public List<StripeCard> getCardsBDStripe(Pageable page){
		User user = userComponent.getLoggedUser();
		try {
			Customer c = Customer.retrieve(user.getCustomerStripeId());
			Map<String, Object> params = new HashMap<>();
			params.put("object", "card");
			params.put("limit", 100);

			PaymentSourceCollection cards =
					  c.getSources().list(params);
			
			List<PaymentSource> p = cards.getData();
			List<StripeCard> pages = new ArrayList();
			for (PaymentSource ps : p) {
				Card card =  (Card) c.getSources().retrieve(ps.getId());
				StripeCard cardStripe = new StripeCard();
				

			}
		} catch (StripeException e) {
			e.printStackTrace();
		}
	}*/
}
