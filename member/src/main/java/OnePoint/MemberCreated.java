package OnePoint;

public class MemberCreated extends AbstractEvent {

    private Long memberId;

    public MemberCreated(){
        super();
    }

    public Long getMemberId() {
        return memberId;
    }

    public void setMemberId(Long memberId) {
        this.memberId = memberId;
    }
}
