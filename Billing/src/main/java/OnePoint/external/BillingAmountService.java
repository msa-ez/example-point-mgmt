package OnePoint.external;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@FeignClient(name = "billingAmountView", url = "http://localhost:8083")
public interface BillingAmountService {

  @RequestMapping(method = RequestMethod.POST, path = "/billingAmount")
  @ResponseBody
  Double billingAmount(@RequestBody BillingAmount billingAmount);
}