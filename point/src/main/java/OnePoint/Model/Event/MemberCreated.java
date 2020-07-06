package OnePoint.Model.Event;


import OnePoint.AbstractEvent;

public class MemberCreated extends AbstractEvent {

  private Long memberId;

  public Long getMemberId() {
    return memberId;
  }

  public void setMemberId(Long memberId) {
    this.memberId = memberId;
  }
}