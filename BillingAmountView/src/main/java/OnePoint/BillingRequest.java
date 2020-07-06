package OnePoint;

/**
 * @author seoyeon on 2020/07/05
 * @project OnePoint
 */

public class BillingRequest {

  private Long mercharntId;
  private String billingMonth;

  public Long getMercharntId() {
    return mercharntId;
  }

  public void setMercharntId(Long mercharntId) {
    this.mercharntId = mercharntId;
  }

  public String getBillingMonth() {
    return billingMonth;
  }

  public void setBillingMonth(String billingMonth) {
    this.billingMonth = billingMonth;
  }
}
