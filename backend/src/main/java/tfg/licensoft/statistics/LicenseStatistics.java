package tfg.licensoft.statistics;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;

import com.fasterxml.jackson.annotation.JsonIgnore;

import tfg.licensoft.licenses.License;
import tfg.licensoft.licenses.LicenseSubscription;

@Entity
public class LicenseStatistics {
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	
	@JsonIgnore
	@ManyToOne
	private License license;
	private String ip;
	private int nUsage;
    @ElementCollection(targetClass=Date.class)
	private List<Date> usages;
	private String userName;
	@Lob
	private HashMap<String,Integer> usagePerTime;

	
	public LicenseStatistics() {}
	public LicenseStatistics(License l) {
		this.nUsage=0;
		this.usages= new ArrayList<>();
		this.license=l;
		this.setUsagePerTimeReady();

		
	}
	
	private void setUsagePerTimeReady() {
		this.usagePerTime = new HashMap<>();
	    try{
			List<Date> datesInRange = new ArrayList<>();
		    Calendar calendar = new GregorianCalendar();
		    calendar.setTime(this.license.getStartDate());
		     
		    Calendar endCalendar = new GregorianCalendar();
		    LicenseSubscription ls = (LicenseSubscription)(this.license);
		    endCalendar.setTime(ls.getEndDate());
		    
		    while (calendar.before(endCalendar)) {
		        Date result = calendar.getTime();
		        String formatedDate = new SimpleDateFormat("MM/dd/yyyy").format(result);
		        this.usagePerTime.put(formatedDate,0);
		        calendar.add(Calendar.DATE, 1);
		    }
		    System.out.println(this.usagePerTime);
		}catch (ClassCastException e) {
			System.out.println("Error while setting up the usage per time ready");
		}

	 

	}
	

	
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public License getLicense() {
		return license;
	}
	public void setLicense(License license) {
		this.license = license;
	}



	public HashMap<String, Integer> getUsagePerTime() {
		return usagePerTime;
	}


	public void setUsagePerTime(HashMap<String, Integer> usagePerTime) {
		this.usagePerTime = usagePerTime;
	}


	public String getUserName() {
		return userName;
	}


	public void setUserName(String userName) {
		this.userName = userName;
	}



	public List<Date> getUsages() {
		return usages;
	}


	public void setUsages(List<Date> usages) {
		this.usages = usages;
	}


	public String getIp() {
		return ip;
	}


	public void setIp(String ip) {
		this.ip = ip;
	}


	public int getnUsage() {
		return nUsage;
	}


	public void setnUsage(int nUsage) {
		this.nUsage = nUsage;
	}




	
	
	

}
