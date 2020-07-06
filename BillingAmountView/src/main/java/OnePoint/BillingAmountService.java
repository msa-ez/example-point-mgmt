package OnePoint;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author seoyeon on 2020/07/05
 * @project OnePoint
 */

@Service
public class BillingAmountService {

  @Autowired
  BillingAmountViewRepository billingAmountViewRepository;

  public Double billing(Long mercharntId, String billingMonth) throws ParseException {

    SimpleDateFormat transFormat = new SimpleDateFormat("yyyyMMdd");

    Date startDate = transFormat.parse(billingMonth + "01");
    Date endDate = transFormat.parse(billingMonth + "31");

    System.out.println("##startDate : " + startDate.toString());
    System.out.println("##endDate : " + endDate.toString());

    List<BillingAmountView> billingListByMercharntIdAndBillngMont = billingAmountViewRepository
        .findBillingListByMercharntIdAndBillngMont(mercharntId, startDate, endDate);

    Double sum = 0.0;

    for (int i = 0; i < billingListByMercharntIdAndBillngMont.size(); i++) {
      System.out
          .println(i + "번째 값 : " + billingListByMercharntIdAndBillngMont.get(i).getBillingAmount());
      sum += billingListByMercharntIdAndBillngMont.get(i).getBillingAmount();
    }

    System.out.println("sum : " + sum);
    return sum;
  }
}
