package OnePoint;

import OnePoint.external.PointService;
import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PostPersist;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.BeanUtils;


@Entity
@Getter
@Setter
@Table(name = "Deal_table")
public class Deal {

  // @Autowired
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;
  private Long memberId;
  private Long merchantId;
  private Date dealDate;
  private Double point;
  private String type;
  private Double dealAmount;
  private String status;

  /**
   * 적립거래 발생 시 데이터 셋
   */
  public void setSaveDeal() {

    System.out.println("##적립 생성자로 들어옴");
    Date now = new Date();
    this.setDealDate(now);
    if (this.dealAmount != null) {
      this.setPoint(this.dealAmount * 0.01);
    }
    this.setStatus("success");

//   dealRepository.save(this);

  }

  /**
   * 사용거래 발생 시 데이터 셋팅 , 포인트가 함께 들어옴
   */
  public void setUseDeal() {

    System.out.println("##사용 생성자로 들어옴");
    Date now = new Date();
    this.setDealDate(now);
    this.setStatus("success");

//   dealRepository.save(this);
  }

  /**
   * 적립거래/사용거래 발생 1. 적립거래 발생 시 Point System에 memberId, point 정보를 전달 2. 사용거래 발생 시 Point System에
   * memberId, point 정보를 전달 회원의 잔여Point 부족으로 응답값이 false가 올 경우 거래상태(status)를 fail로 셋팅
   */

  @PrePersist
  public void onPrePersist() {
    if (this.getType().equals("save")) { // 1. 적립 거래 발생
      setSaveDeal();
      OnePoint.external.Point point = new OnePoint.external.Point();
      //1.input값 셋팅
      point.setMemberId(this.getMemberId());
      point.setPoint(this.getPoint());

      Application.applicationContext.getBean(OnePoint.external.PointService.class)
          .pointIncrease(point);

    } else if (this.getType().equals("use")) { //2. 사용거래
      setUseDeal();
      OnePoint.external.Point point = new OnePoint.external.Point();
      point.setMemberId(this.getMemberId());
      point.setPoint(this.getPoint());

      String pointDecreaseResult = Application.applicationContext.getBean(PointService.class)
          .pointDecrease(point);

      if (pointDecreaseResult.equals("false")) {
        this.setStatus("fail");
        System.out.println("유효하지 않은 거래이기 떄문에 삭제되었습니다.");
      }


    }


  }

  @PostPersist
  public void onPostPersist() {
    if (this.getType().equals("use") && this.getStatus().equals("success")) {

      // 사용거래, 성공 시 view 생성을 위해 이벤트 발행
      UseRequested useRequested = new UseRequested();
      useRequested.setId(this.getId());
      useRequested.setMerchantId(this.getMerchantId());
      useRequested.setDealDate(this.getDealDate());
      useRequested.setType(this.getType());
      useRequested.setPoint(this.getPoint());

      BeanUtils.copyProperties(this, useRequested);
      useRequested.publishAfterCommit();
    }
  }
}
