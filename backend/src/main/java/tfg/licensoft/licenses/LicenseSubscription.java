package tfg.licensoft.licenses;


import java.util.Calendar;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import tfg.licensoft.products.Product;

@Entity
public class LicenseSubscription extends License {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	private boolean trial;
	private boolean cancelAtEnd;
	private String subscriptionItemId;
	private String subscriptionId;
	private int nUsage;
	private Date endDate;

	public LicenseSubscription(boolean active, String type,  Product product, String owner,long trialDays) {
		super(active, product, owner);
		this.setType(type);
		this.nUsage=0;
		this.trial=true;
		if(active) {
			Calendar ahoraCal = Calendar.getInstance();
			this.calculateEndDate(ahoraCal,trialDays);
		}
		this.setPrice(product.getPlansPrices().get(type));

	}
	
	


	
	public LicenseSubscription() {}






	public void calculateEndDate(Calendar ahoraCal, long trialDays) {
		if(trialDays==0) {
			this.trial=false;

			switch(this.getType()) {
				case "MB":
				case "M": {
					ahoraCal.add(Calendar.MONTH, 1);
					break;
				}
				case "A":{
					ahoraCal.add(Calendar.YEAR, 1);
					break;	
				} 
				case "D":{
					ahoraCal.add(Calendar.HOUR, 24);
					break;	
				}
				default: this.endDate=null;
			}
		}else {
			ahoraCal.add(Calendar.HOUR, (int)(24*trialDays));
		}
		this.endDate = ahoraCal.getTime();

	}
	
	public void renew(long trialDays) {
		Calendar ahoraCal = Calendar.getInstance();
		this.setStartDate(ahoraCal.getTime());
		this.calculateEndDate(ahoraCal,trialDays);
		this.nUsage= 0 ;
	}

	public boolean isTrial() {
		return trial;
	}

	public void setTrial(boolean trial) {
		this.trial = trial;
	}

	public boolean getCancelAtEnd() {
		return cancelAtEnd;
	}

	public void setCancelAtEnd(boolean cancelAtEnd) {
		this.cancelAtEnd = cancelAtEnd;
	}

	public String getSubscriptionItemId() {
		return subscriptionItemId;
	}

	public void setSubscriptionItemId(String subscriptionItemId) {
		this.subscriptionItemId = subscriptionItemId;
	}

	public String getSubscriptionId() {
		return subscriptionId;
	}

	public void setSubscriptionId(String subscriptionId) {
		this.subscriptionId = subscriptionId;
	}

	public int getnUsage() {
		return nUsage;
	}

	public void setnUsage(int nUsage) {
		this.nUsage = nUsage;
	}

	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + (cancelAtEnd ? 1231 : 1237);
		result = prime * result + ((endDate == null) ? 0 : endDate.hashCode());
		result = prime * result + nUsage;
		result = prime * result + ((subscriptionId == null) ? 0 : subscriptionId.hashCode());
		result = prime * result + ((subscriptionItemId == null) ? 0 : subscriptionItemId.hashCode());
		result = prime * result + (trial ? 1231 : 1237);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		LicenseSubscription other = (LicenseSubscription) obj;
		if (cancelAtEnd != other.cancelAtEnd)
			return false;
		if (endDate == null) {
			if (other.endDate != null)
				return false;
		} else if (!endDate.equals(other.endDate))
			return false;
		if (nUsage != other.nUsage)
			return false;
		if (subscriptionId == null) {
			if (other.subscriptionId != null)
				return false;
		} else if (!subscriptionId.equals(other.subscriptionId))
			return false;
		if (subscriptionItemId == null) {
			if (other.subscriptionItemId != null)
				return false;
		} else if (!subscriptionItemId.equals(other.subscriptionItemId))
			return false;
		if (trial != other.trial)
			return false;
		return true;
	}
	
	
	

}