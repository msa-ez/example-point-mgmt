package OnePoint.external;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@FeignClient(name = "point", url = "http://localhost:8081")
public interface PointService {

  @RequestMapping(method = RequestMethod.POST, path = "/pointIncrease")
  void pointIncrease(@RequestBody Point point);

  @RequestMapping(method = RequestMethod.POST, path = "/pointDecrease")
  @ResponseBody
  String pointDecrease(@RequestBody Point point);
}