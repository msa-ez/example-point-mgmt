package OnePoint;

import OnePoint.external.BillingAmountService;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.Table;

@Entity
@Table(name = "Billing_table")
public class Billing {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;
  private Long mercharntId;
  private Double billingAmount;
  private String billingMonth;

  @PrePersist
  public void onPrePersist() {
        /*Billed billed = new Billed();
        BeanUtils.copyProperties(this, billed);
        billed.publishAfterCommit();
        */
    billingMonthAmout();

    OnePoint.external.BillingAmount billingAmount = new OnePoint.external.BillingAmount();
    //1.input값 셋팅
    billingAmount.setMercharntId(this.getMercharntId());
    billingAmount.setBillingMonth(this.getBillingMonth());

    Double billedAmount = Application.applicationContext.getBean(BillingAmountService.class)
        .billingAmount(billingAmount);

    System.out.println("# 가맹점 : " + this.getMercharntId());
    System.out.println("# 청구월 : " + this.getBillingMonth());
    System.out.println("# 정산금액 : " + billedAmount);

    this.setBillingAmount(billedAmount);

  }

  private void billingMonthAmout() {

  }


  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Long getMercharntId() {
    return mercharntId;
  }

  public void setMercharntId(Long mercharntId) {
    this.mercharntId = mercharntId;
  }

  public Double getBillingAmount() {
    return billingAmount;
  }

  public void setBillingAmount(Double billingAmount) {
    this.billingAmount = billingAmount;
  }


  public String getBillingMonth() {
    return billingMonth;
  }

  public void setBillingMonth(String billingMonth) {
    this.billingMonth = billingMonth;
  }
}
