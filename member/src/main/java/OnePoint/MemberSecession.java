package OnePoint;

public class MemberSecession extends AbstractEvent {

    private Long memberId;

    public MemberSecession(){
        super();
    }

    public Long getMemberId() {
        return memberId;
    }

    public void setMemberId(Long memberId) {
        this.memberId = memberId;
    }
}
