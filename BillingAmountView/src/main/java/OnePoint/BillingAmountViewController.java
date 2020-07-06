package OnePoint;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author seoyeon on 2020/07/05
 * @project OnePoint
 */

@RestController

public class BillingAmountViewController {

  @Autowired
  BillingAmountService billingAmountService;

  @PostMapping("/billingAmount")
  public Double billingAmount(@RequestBody BillingRequest billingRequest)
      throws Exception {
    System.out.println("#####billingAmount 들어옴");
    double billingAmount = billingAmountService
        .billing(billingRequest.getMercharntId(), billingRequest.getBillingMonth());

    return billingAmount;
  }
}