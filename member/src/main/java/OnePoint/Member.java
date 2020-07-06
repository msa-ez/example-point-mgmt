package OnePoint;

import javax.persistence.*;
import org.springframework.beans.BeanUtils;
import java.util.List;

@Entity
@Table(name="Member_table")
public class Member {

    private String name;
    private String phone;
    private String addess;
    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long memberId;

    @PrePersist
    public void onPrePersist(){
        MemberCreated memberCreated = new MemberCreated();
        memberCreated.setMemberId(this.getMemberId());
        BeanUtils.copyProperties(this, memberCreated);
        memberCreated.publishAfterCommit();


    }

    @PreRemove
    public void onPreRemove(){
        MemberSecession memberSecession = new MemberSecession();
        memberSecession.setMemberId(this.getMemberId());
        BeanUtils.copyProperties(this, memberSecession);
        memberSecession.publishAfterCommit();


    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
    public String getAddess() {
        return addess;
    }

    public void setAddess(String addess) {
        this.addess = addess;
    }
    public Long getMemberId() {
        return memberId;
    }

    public void setMemberId(Long memberId) {
        this.memberId = memberId;
    }




}
