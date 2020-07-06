package OnePoint.service;

import OnePoint.Point;
import OnePoint.repostory.PointRepository;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class PointService {

  @Autowired
  PointRepository pointRepository;

  public boolean pointDecrease(Point point) {
    //1. point가 있는지 먼저 확인 하기
    //2. point가 있는 경우만 사용하기
    Optional<Point> finedPoint = pointRepository.findById(point.getMemberId());
    if (finedPoint.isPresent()) {

      if (finedPoint.get().getPoint() >= point.getPoint()) {

        Double decreasedPoint = finedPoint.get().getPoint() - point.getPoint();
        point.setPoint(decreasedPoint);

        pointRepository.save(point);

        return true;
      }
    }
    return false;
  }

  public void pointIncrease(Point point) {

    Optional<Point> finedPoint = pointRepository.findById(point.getMemberId());
    if (finedPoint.isPresent()) {
      Double IncreasedPoint = finedPoint.get().getPoint() + point.getPoint();
      System.out.println("finedPoint.get().getPoint() : "+finedPoint.get().getPoint() );
      System.out.println("point.getPoint() : "+ point.getPoint());
      point.setPoint(IncreasedPoint);
    }
    pointRepository.save(point);
  }

}