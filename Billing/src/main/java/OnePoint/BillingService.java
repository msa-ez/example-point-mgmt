package OnePoint;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author seoyeon on 2020/07/05
 * @project OnePoint
 */
@Service
public class BillingService {

  @Autowired
  BillingRepository billingRepository;

  public Billing billing(Long MercharntId, String month) throws ParseException {
    String monthStart = month + "01";
    String monthEnd = month + "31";


    SimpleDateFormat transFormat = new SimpleDateFormat("yyyyMMdd");

    Date monthStartDate = transFormat.parse(monthStart);
    Date monthEndDate = transFormat.parse(monthEnd);

    System.out.println(monthStartDate);
    System.out.println(monthEndDate);

    Billing billing =new Billing();
    return billing;
  }
}
