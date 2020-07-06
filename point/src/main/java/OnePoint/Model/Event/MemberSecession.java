package OnePoint.Model.Event;

import OnePoint.AbstractEvent;

public class MemberSecession extends AbstractEvent {

  private Long meberberId;

  public Long getMeberberId() {
    return meberberId;
  }

  public void setMeberberId(Long meberberId) {
    this.meberberId = meberberId;
  }
}
