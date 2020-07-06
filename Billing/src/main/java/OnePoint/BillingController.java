package OnePoint;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class BillingController {

  /*
  @Autowired
  BillingService billingService;

  @PostMapping("/billing")
  public void billing(@RequestBody BillingRequest billingRequest)
      throws Exception {

    System.out.println(billingRequest.getBillingMonth());
    System.out.println(billingRequest.getMercharntId());

    billingService.billing(billingRequest.getMercharntId(), billingRequest.getBillingMonth());

    //service에 billing 월과 billing

  }
  */

}
