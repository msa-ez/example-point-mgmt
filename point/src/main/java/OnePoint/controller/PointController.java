package OnePoint.controller;

import OnePoint.Point;
import OnePoint.service.PointService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController

public class PointController {

  @Autowired
  PointService pointService;

  @PostMapping("/pointDecrease")
  public String pointDecrease(@RequestBody PointDecreaseRequest pointDecreaseRequest)
      throws Exception {

    // 포인트 감소
    System.out.println("##### /point/pointDecrease  called #####");

    Point point = new Point();

    point.setMemberId(pointDecreaseRequest.getMemberId());
    point.setPoint(pointDecreaseRequest.getPoint());

    try {

      if (!pointService.pointDecrease(point)) {
        throw new PointLackException();
      }
    } catch (PointLackException e) {
      e.printStackTrace();

      System.err.println("PointLack Exception이 발생했습니다.");
      return "false";
    }
    return "true";
  }

  @PostMapping("/pointIncrease")
  public void pointIncrease(@RequestBody PointIncreaseRequest pointIncreaseRequest)
      throws Exception {

    Point point = new Point();
    point.setMemberId(pointIncreaseRequest.getMemberId());
    point.setPoint(pointIncreaseRequest.getPoint());

    pointService.pointIncrease(point);
  }


}
