package tfg.licensoft.statistics;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import tfg.licensoft.licenses.License;

@Service
public class LicenseStatisticsService {
	
	@Autowired
	private LicenseStatisticsRepository licStatRepo;
	
	public LicenseStatistics save(LicenseStatistics l) {
		return this.licStatRepo.save(l);
	}
	
	public LicenseStatistics findByLicenseAndIp(License lic, String ip) {
		return this.licStatRepo.findByLicenseAndIp(lic, ip);
	}
	
	public LicenseStatistics delete(LicenseStatistics l) {
		return this.delete(l);
	}
}
