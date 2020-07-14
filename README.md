# OnePoint
![onepoint](https://user-images.githubusercontent.com/33366501/87279771-51cf6280-c52c-11ea-8e6a-170bbf44bfa9.png)

# OnePoint - 멤버십 회원기반 포인트 관리 시스템

# Table of contents

- [OnePoint - 멤버십 포인트관리](#---)
  - [서비스 시나리오](#서비스-시나리오)
  - [체크포인트](#체크포인트)
  - [분석/설계](#분석설계)
  - [구현:](#구현-)
    - [DDD 의 적용](#ddd-의-적용)
    - [폴리글랏 퍼시스턴스](#폴리글랏-퍼시스턴스)
    - [폴리글랏 프로그래밍](#폴리글랏-프로그래밍)
    - [동기식 호출 과 Fallback 처리](#동기식-호출-과-Fallback-처리)
    - [비동기식 호출 과 Eventual Consistency](#비동기식-호출-과-Eventual-Consistency)
  - [운영](#운영)
    - [CI/CD 설정](#cicd설정)
    - [동기식 호출 / 서킷 브레이킹 / 장애격리](#동기식-호출-서킷-브레이킹-장애격리)
    - [오토스케일 아웃](#오토스케일-아웃)
    - [무정지 재배포](#무정지-재배포)
  - [신규 개발 조직의 추가](#신규-개발-조직의-추가)

# 서비스 시나리오

기능적 요구사항
1. 고객이 멤버십 회원을 가입한다
1. 멤버십 포인트가 생성된다
1. 멤버십 가맹점에서 거래가 발생하면 특정비율로 포인트가 적립된다.
1. 멤버십 가맹점에서 결제시 포인트를 사용하면 포인트가 차감된다.
1. 포인트 적립/사용 거래를 취소하면 해당 포인트가 취소/복구 된다.
1. 월말 가맹점별 거래에 대한 정산 작업을 진행한다.
1. 멤버십 회원 탈퇴 시 적립된 포인트는 소멸된다.
1. 멤버십 담당자는 대시보드를 통해 고객별 거래현황을 분석한다.

비기능적 요구사항
1. 트랜잭션
    1. 포인트 사용거래의 경우 멤버십의 사용가능 포인트가 실제 사용 Point보다 적을때 거래가 성립되지 않아야 한다  Sync 호출 
1. 장애격리
    1. 포인트 적립/사용거래 기능이 수행되지 않더라도 멤버십 회원가입 처리는 365일 24시간 받을 수 있어야 한다  Async (event-driven), Eventual Consistency
    1. 포인사용거래 취소가 과중되면 거래 취소처리 후 실제 포인트 취소처리를 잠시 유보하고 잠시후에 하도록 유도한다  Circuit breaker, fallback
	   (이벤트 상품 거래 폭주 후 타 상점 동일물품 가격우위로 대략 취소 발생 가정)
1. 성능
    1. 고객 거래 현황 분석을 위한 대량의 데이터를 실제 거래와 분리하여 확인할 수 있어야 한다.  CQRS
    1. 포인트 적립/사용거래 부하와 격리되어 회원가입/탈퇴는 자유로워야 한다.  Event driven

# 분석/설계


## AS-IS 조직 (Horizontally-Aligned)
  ![image](https://user-images.githubusercontent.com/487999/79684144-2a893200-826a-11ea-9a01-79927d3a0107.png)

## TO-BE 조직 (Vertically-Aligned)
  ![org](https://user-images.githubusercontent.com/33366501/87282662-77aa3680-c52f-11ea-9a68-15de1cee8be8.PNG)


## Event Storming 결과
* MSAEz 로 모델링한 이벤트스토밍 결과
![MSA설계2](https://user-images.githubusercontent.com/33366501/87380560-b1ca1580-c5cd-11ea-83f9-b13d5d2efb9d.PNG)

## 헥사고날 아키텍처 다이어그램 도출
    
![hexa](https://user-images.githubusercontent.com/33366501/87289597-cfe53680-c537-11ea-8d08-53620c8c9997.PNG)


    - Chris Richardson, MSA Patterns 참고하여 Inbound adaptor와 Outbound adaptor를 구분함
    - 호출관계에서 PubSub 과 Req/Resp 를 구분함
    - 서브 도메인과 바운디드 컨텍스트의 분리:  각 팀의 KPI 별로 아래와 같이 관심 구현 스토리를 나눠가짐

# 구현:

분석/설계 단계에서 도출된 헥사고날 아키텍처에 따라, 각 BC별로 대변되는 마이크로 서비스들을 스프링부트와 파이선으로 구현하였다. 구현한 각 서비스를 로컬에서 실행하는 방법은 아래와 같다 (각자의 포트넘버는 8081 ~ 808n 이다)

```
cd billing
mvn spring-boot:run

cd deal
mvn spring-boot:run 

cd point
mvn spring-boot:run  

cd member
mvn spring-boot:run

cd dealdashboard
mvn spring-boot:run 
```

## DDD 의 적용

- 각 서비스내에 도출된 핵심 Aggregate Root 객체를 Entity 로 선언하였다: 

```
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
  private String status; // 거래 성공여부
  private String billingStatus; //정산여부

  /**
   * 적립거래 발생 시 데이터 셋
   */
  public void setSaveDeal() {
    Date now = new Date();
    this.setDealDate(now);
    if (this.dealAmount != null) {
      this.setPoint(this.dealAmount * 0.01);
    }
    this.setStatus("success");
    this.setBillingStatus("no");
  }

  /**
   * 사용거래 발생 시 데이터 셋팅 , 포인트가 함께 들어옴
   */
  public void setUseDeal() {
    System.out.println("##사용 생성자로 들어옴");
    Date now = new Date();
    this.setDealDate(now);
    this.setStatus("success");
    this.setBillingStatus("no");
  }

```
- Entity Pattern 과 Repository Pattern 을 적용하여 JPA 를 통하여 다양한 데이터소스 유형 (RDB or NoSQL) 에 대한 별도의 처리가 없도록 데이터 접근 어댑터를 자동 생성하기 위하여 Spring Data REST 의 RestRepository 를 적용하였다
```
package OnePoint.repostory;

import OnePoint.Point;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface PointRepository extends PagingAndSortingRepository<Point, Long>{

}
```

## REST API 테스트 시나리오 
1. memberId 0001 사용자가 적립거래를 10000원 발생 시킨 뒤 다시 취소시킴. 적립률(0.01)은 고정이고 거래금액*적립률 만큼의 Point가 쌓임 


|요청 | Point | status |dealDate |
|-------------------------------------------------------------------------------------------------|:-------:|------:|------------:|
|'http POST http://localhost:8085/deals memberId=0001 merchantId=20 dealAmount=100000 type="save"'|  1000 원 |    |    |
|http GET http://localhost:8081/points/0001							  | 1000원   |    |    |  
|http GET http://localhost:8085/deals/1                                                          |  1000원  | "success"|  거래 발생시간|
|'http POST http://localhost:8085/deals memberId=0001 merchantId=20 dealAmount=100000 type="cancel"'|   0 원 |    |    |
|http GET http://localhost:8081/points/0001							  |  0 원  |    |    |  
|http GET http://localhost:8085/deals/2                                                           |  0 원  | "success"|  거래 발생시간|

2. memberId 0001 사용자의 point 사용거래 . 포인트 사용 거래 시 거래금액은 상관이 없음. 

|요청 | Point | status |dealDate |
|-------------------------------------------------------------------------------------------------|:-------:|------:|------------:|
|http POST http://localhost:8085/deals memberId=0001 merchantId=20 dealAmount=10000 type="use" point="100"   |100| | |
|http GET http://localhost:8081/points/0001  		|900| | | 
|http GET http://localhost:8085/deals/3 			|100| | | 
|http GET http://localhost:8083/billingAmountViews/2 (Id는 deal을 따라감)| | | |

3. memberId 0001 사용자의 point 사용거래 (포인트 부족)

|요청 | Point | status |dealDate |
|-------------------------------------------------------------------------------------------------|:-------:|------:|------------:|
|http POST http://localhost:8085/deals memberId=0001 merchantId=20 dealAmount=10000 type="use" point="10000" || | |
|http GET http://localhost:8081/points/0001 		|900| | |
|http GET http://localhost:8085/deals/4 			|10000| fail | |
|http GET http://localhost:8083/billingAmountViews/3 (I실패했으므로 안나옴)| | | |


4. point 증가 테스트 : memberId 0001 의 포인트를 증가시킴 

|요청 | Point | status |dealDate |
|-------------------------------------------------------------------------------------------------|:-------:|------:|------------:|
|http POST http://localhost:8085/deals memberId=0001 merchantId=20 dealAmount=100000 type="save"  |         |       |             |
|http GET http://localhost:8081/points/0001 		                                          | 1900    |       |             |
|http GET http://localhost:8085/deals/5 		                                          | 1000    |       |             |


5. memberId 0002 사용자의 Point 적립 거래 발생 후 사용거래 발생. ( 정산기능 테스트를 위해) 

|요청 | Point | status |dealDate |
|-------------------------------------------------------------------------------------------------|:-------:|------:|------------:|
|http POST http://localhost:8085/deals memberId=0002 merchantId=20 dealAmount=100000 type="save" | | | |
|http POST http://localhost:8085/deals memberId=0002 merchantId=20 dealAmount=10000 type="use"  point=" 200" | | | |
|http POST http://localhost:8085/deals memberId=0002 merchantId=20 dealAmount=10000 type="use"  point=" 200" | | | |
|http GET http://localhost:8085/deals/6 | | | |
|http GET http://localhost:8085/deals/7 | | | |
|http GET http://localhost:8081/points/0002 		|  600 | | |


6. memberId 0002 사용자의 모든 거래내역 조회. 사용자 탈퇴 후 상태 조회. 

|요청 | Point | status |dealDate |
|-------------------------------------------------------------------------------------------------|:-------:|------:|------------:|
|http "거래내역 조회(DealDashBoard)"                                                               | | | |
|http "사용자 탈퇴"                                                                                | | | |
|http "사용자 상태 조회(DealDashBoard)"                                                            | | | |


7. 가맹점 20 번의 7월 정산요청 

|요청 | Point | status |dealDate |
|-------------------------------------------------------------------------------------------------|:-------:|------:|------------:|
|http POST http://localhost:8084/billings mercharntId=20 billingMonth="202007" | | | |

test 2, 5 의 사용거래 (100원 , 200원, 200원 ) * 0.98 만큼의 금액이 정산금액으로 책정됨  : 490 원 

## 폴리글랏 퍼시스턴스

## 폴리글랏 프로그래밍

## 동기식 호출 과 Fallback 처리

분석단계에서의 조건 중 하나로 거래(deal)->적립(point) 간의 호출은 동기식 일관성을 유지하는 트랜잭션으로 처리하기로 하였다. 호출 프로토콜은 이미 앞서 Rest Repository 에 의해 노출되어있는 REST 서비스를 FeignClient 를 이용하여 호출하도록 한다. 

- 결제서비스를 호출하기 위하여 Stub과 (FeignClient) 를 이용하여 Service 대행 인터페이스 (Proxy) 를 구현 

```
# (deal) PointService.java

package OnePoint.external;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@FeignClient(name = "point", url = "http://localhost:8081")
//@FeignClient(name = "point", url = "http://point:8080")
public interface PointService {

  @RequestMapping(method = RequestMethod.POST, path = "/pointIncrease")
  void pointIncrease(@RequestBody Point point);

  @RequestMapping(method = RequestMethod.POST, path = "/pointDecrease")
  @ResponseBody
  String pointDecrease(@RequestBody Point point);
}
```

- 거래를 수신한 직후 (@PostPersist) Point 관련 TR을 요청하도록 처리
```
# Deal.java (Entity)

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


  /**
   * 1. 사용거래 success 발생 시 , BillingAmountView 전달을 위해 비동기 통신을 함
   */

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
    //  useRequested.setBillingStatus("no");

      BeanUtils.copyProperties(this, useRequested);
      useRequested.publishAfterCommit();
    } else if (this.getType().equals("save")) {

      SaveDealt saveDealt = new SaveDealt();
      saveDealt.setId(this.getId());
      saveDealt.setMerchantId(this.getMerchantId());
      saveDealt.setDealDate(this.getDealDate());
      saveDealt.setType(this.getType());
      saveDealt.setPoint(this.getPoint());
     // saveDealt.setBillingStatus("no");


      BeanUtils.copyProperties(this, saveDealt);
      saveDealt.publishAfterCommit();


    } else if (this.getType().equals("saveCancel")) { //3. 적립거래취
      setSaveCancelDeal();
      SavedDealCancelled savedDealCancelled = new SavedDealCancelled();
      savedDealCancelled.setId(this.getId());
      savedDealCancelled.setMerchantId(this.getMerchantId());
      savedDealCancelled.setDealDate(this.getDealDate());
      savedDealCancelled.setType(this.getType());
      savedDealCancelled.setPoint(this.getPoint());
  //    savedDealCancelled.setBillingStatus("no");

      BeanUtils.copyProperties(this, savedDealCancelled);
      savedDealCancelled.publishAfterCommit();

      //
    } else if (this.getType().equals("useCancel")) { //4. 사용거래 취소
      setUseCancelDeal();

      //사용거래 취소 !! -> 만약 dealingstatus가 ... 아 그러면 동기호출을 해와야 되는데 잠깐 패쓰!

      UsedDealCancelled usedDealCancelled = new UsedDealCancelled();

      usedDealCancelled.setId(this.getId());
      usedDealCancelled.setMerchantId(this.getMerchantId());
      usedDealCancelled.setDealDate(this.getDealDate());
      usedDealCancelled.setType(this.getType());
      usedDealCancelled.setPoint(this.getPoint());
     // usedDealCancelled.setBillingStatus("no");


      BeanUtils.copyProperties(this, usedDealCancelled);
      usedDealCancelled.publishAfterCommit();
    }
  }
}
```

- 동기식 호출에서는 호출 시간에 따른 타임 커플링이 발생하며, Point 관리 시스템이 장애가 나면 Point관련 거래는 불가능 하다는 것을 확인:


```
[[[[[[ 동기식 호출 이미지 첨부 ]]]]]]
```
- 또한 과도한 요청시에 서비스 장애가 도미노 처럼 벌어질 수 있다. (서킷브레이커, 폴백 처리는 운영단계에서 설명한다.)

## 비동기식 호출 / 시간적 디커플링 / 장애격리 / 최종 (Eventual) 일관성 테스트


회원가입이 이루어진 후에 Point시스템으로 이를 알려주는 행위는 동기식이 아니라 비 동기식으로 처리하여 Point관리 시스템의 처리를 위하여 회원가입/탈퇴가 블로킹 되지 않아도록 처리한다.
 
- 이를 위하여 회원가입처리를 진행한 이후에 가입처리가 완료되었다는 도메인 이벤트를 카프카로 송출한다(Publish)
 
```
package OnePoint;

@Entity
@Table(name = "Member_table")
public class Member {

 ...
  @PrePersist
  public void onPrePersist() {
    MemberCreated memberCreated = new MemberCreated();
    memberCreated.setStatus("valid");
    this.setStatus("valid");
    memberCreated.setMemberId(this.getMemberId());
    BeanUtils.copyProperties(this, memberCreated);
    memberCreated.publishAfterCommit();
  }

}
```
- Point 서비스에서는 결제승인 이벤트에 대해서 이를 수신하여 자신의 정책을 처리하도록 PolicyHandler 를 구현한다:

```
package fooddelivery;

...

@Service
public class PolicyHandler{

    @StreamListener(KafkaProcessor.INPUT)
    public void whenever결제승인됨_주문정보받음(@Payload 결제승인됨 결제승인됨){

        if(결제승인됨.isMe()){
            System.out.println("##### listener 주문정보받음 : " + 결제승인됨.toJson());
            // 주문 정보를 받았으니, 요리를 슬슬 시작해야지..
            
        }
    }

}

```
실제 구현을 하자면, 카톡 등으로 점주는 노티를 받고, 요리를 마친후, 주문 상태를 UI에 입력할테니, 우선 주문정보를 DB에 받아놓은 후, 이후 처리는 해당 Aggregate 내에서 하면 되겠다.:
  
```
  @Autowired 주문관리Repository 주문관리Repository;
  
  @StreamListener(KafkaProcessor.INPUT)
  public void whenever결제승인됨_주문정보받음(@Payload 결제승인됨 결제승인됨){

      if(결제승인됨.isMe()){
          카톡전송(" 주문이 왔어요! : " + 결제승인됨.toString(), 주문.getStoreId());

          주문관리 주문 = new 주문관리();
          주문.setId(결제승인됨.getOrderId());
          주문관리Repository.save(주문);
      }
  }

```

상점 시스템은 주문/결제와 완전히 분리되어있으며, 이벤트 수신에 따라 처리되기 때문에, 상점시스템이 유지보수로 인해 잠시 내려간 상태라도 주문을 받는데 문제가 없다:
```
[[[[[[ 동기식 호출 이미지 첨부 ]]]]]]

```

=====================================================================

##서비스 개요 
*계약을 맺은 가맹점과 고객간의 포인트를 적립/사용 하는 회원포인트 시스템?? 

## Micro Serive 소개 
* Billing
	* 가맹점 별 월 단위 정산을 도와주는 시스템 
	* 주요기능 : 정산년월, 정산가맹점 번호를 입력받아 해당 정산년월의 정산금액을 반환  
* BillingAmountView
	*  정산을 위해 거래 발생 시, 실패하지 않은 사용 거래에 대해서만 Data를 쌓아 놓은 View 
* Point
	* 회원 별 보유포인트 금액 을 관리하는 시스템 
	* Deal 시스템에서 성공 거래가 일어날 시, 보유포인트를 증가/감소 시켜 줌 
* Member
	* 회원 정보 관리 시스템으로 보유 포인트에 대한 정보가 아닌 전화번호, 이메일주소 등 변경이 잦지 않은 회원정보를 관리하는 시스템
	* 회원가입 시 Point 시스템에 회원-Point 생성을 요청 함 
	* 회원 탈퇴 시 Point 시스템에 회원-Point 삭제를 요청 함 
* Deal 
	* 거래관리 시스템. 성공/실패 거래를 모두 관리하는 시스템. 
	* 적립거래 발생 시 Point 시스템에 적립된 금액 만큼에 대한 보유포인트 증가를 요청함 
	* 사용거래 발생 시 Point 시스템과 동기 통신을 통해 사용가능한 포인트인지 확인하고, 거래 성공 시 Point 시스템에 보유포인트 차감을 요청함  
	* 사용거래 발생 시 거래가 성공인 case에 대해서만 BillingAmountView 시스템에 거래를 누적시킴
* DealDashBoard
	* 회원별 Point 거래내역과 회원 탈퇴 여부를 조회하는 시스템
	* 적립거래, 사용거래 시 발생한 모든 Point 내역을 보여줌
	* 회원 탈퇴 시 회원 State 변경, 정산 시 정산 State를 변경함
 
##


