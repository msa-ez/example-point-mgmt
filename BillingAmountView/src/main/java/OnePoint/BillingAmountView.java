package OnePoint;

import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "BillingAmountView_table")
public class BillingAmountView {

  @Id
  //   @GeneratedValue(strategy=GenerationType.AUTO)
  private Long id;
  private Long merchantId;
  private Date dealDate;
  private String type;
  private Double point;
  private Double billingAmount;


  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Long getMerchantId() {
    return merchantId;
  }

  public void setMerchantId(Long merchantId) {
    this.merchantId = merchantId;
  }

  public Date getDealDate() {
    return dealDate;
  }

  public void setDealDate(Date dealDate) {
    this.dealDate = dealDate;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public Double getPoint() {
    return point;
  }

  public void setPoint(Double point) {
    this.point = point;
  }

  public Double getBillingAmount() {
    return billingAmount;
  }

  public void setBillingAmount(Double billingAmount) {
    this.billingAmount = billingAmount;
  }

}
