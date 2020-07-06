package OnePoint.Model.Event;

import OnePoint.AbstractEvent;

public class PointDisappeared extends AbstractEvent {

    private Long id;
    private Long memberId;

    public PointDisappeared(){
        super();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    public Long getMemberId() {
        return memberId;
    }

    public void setMemberId(Long memberId) {
        this.memberId = memberId;
    }
}
