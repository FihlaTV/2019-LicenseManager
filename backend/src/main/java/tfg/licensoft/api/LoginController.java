package tfg.licensoft.api;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.stripe.exception.StripeException;
import com.stripe.model.Customer;

import tfg.licensoft.stripe.StripeCard;
import tfg.licensoft.users.User;
import tfg.licensoft.users.UserComponent;
import tfg.licensoft.users.UserService;

/**
 * This class is used to provide REST endpoints to logIn and logOut to the
 * service. These endpoints are used by Angular SPA client application.
 * 
 * NOTE: This class is not intended to be modified by app developer.
 */
@CrossOrigin
@RestController
public class LoginController {

	private static final Logger log = LoggerFactory.getLogger(LoginController.class);

	@Autowired
	private UserComponent userComponent;
	
	@Autowired
	private UserService userServ;

	
	@RequestMapping("/api/logIn")
	public ResponseEntity<User> logIn() {
		if (!userComponent.isLoggedUser()) {
			log.info("Not user logged");
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
		} else {
			User loggedUser = userComponent.getLoggedUser();
			log.info("Logged as " + loggedUser.getName());
			
			Optional<User> u = userServ.findOne(loggedUser.getId());
			
			List<StripeCard> list = u.get().getStripeCards();
			if (list==null) {
				loggedUser.setStripeCards(new ArrayList<StripeCard>());
			}
			else {
				loggedUser.setStripeCards(list);
			}
			this.userServ.save(loggedUser);
			return new ResponseEntity<>(loggedUser, HttpStatus.OK);
		}
	}

	@RequestMapping("/api/logOut")
	public ResponseEntity<Boolean> logOut(HttpSession session) {
		System.out.println("--------------------------" + userComponent.getLoggedUser());
		if (!userComponent.isLoggedUser()) {
			log.info("No user logged");
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
		} else {
			session.invalidate();

			log.info("Logged out");
			return new ResponseEntity<>(true, HttpStatus.OK);
		}
	}
	
	@RequestMapping(value="/api/register/{user}/{pass1}/{pass2}", method= RequestMethod.POST)
	public ResponseEntity<User> register(Model model, @PathVariable String user, @PathVariable String pass1,
			@PathVariable String pass2, HttpServletRequest request, HttpServletResponse httpServletResponse) {
		User newUser =userServ.findByName(user);
		if ((pass1.equals(pass2)) && (newUser == null)) {
			Map<String,Object> customerParameter = new HashMap<String,Object>();
			customerParameter.put("name", user);
			customerParameter.put("email",user+"@email.com");
			try {
				Customer customer = Customer.create(customerParameter);
				userServ.save(new User(customer.getId(),user, pass1, "ROLE_USER"));
			} catch (StripeException e) {
				e.printStackTrace();
			}
			try {
				request.login(user, pass1);
			} catch (ServletException e) {
				e.printStackTrace();
			}
			
			return new ResponseEntity<User>(newUser, HttpStatus.OK);
		} else {
			return new ResponseEntity<User>(newUser, HttpStatus.CONFLICT);
		}
		
		
	}
	
	/*
	@RequestMapping(value="/api/register", method= RequestMethod.POST)
	public ResponseEntity<User> register2(Model model, @RequestParam String user, @RequestParam String pass1,
			@RequestParam String pass2, HttpServletRequest request, HttpServletResponse httpServletResponse) {
		User newUser =userServ.findByName(user);
		if ((pass1.equals(pass2)) && (newUser == null)) {
			userServ.save(new User(user, pass1, "ROLE_USER"));
			try {
				request.login(user, pass1);
			} catch (ServletException e) {
				e.printStackTrace();
			}
			return new ResponseEntity<User>(newUser, HttpStatus.CREATED);
		} else {
			return new ResponseEntity<User>(newUser, HttpStatus.CONFLICT);
		}
		
		
	}*/
	
	@RequestMapping(value="/api/users", method=RequestMethod.GET)
	public ResponseEntity<Page<User>> getAllUsers(Pageable page){
		Page<User> users= userServ.findAll(page);
		return new ResponseEntity<Page<User>>(users,HttpStatus.OK);
	}
	
	@RequestMapping("/api/getUserLogged")
	public void getUserLogged() {
		System.out.println("??? ->  "+this.userComponent.getLoggedUser());
	}
	
}
