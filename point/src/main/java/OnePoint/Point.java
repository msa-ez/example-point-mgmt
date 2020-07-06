package OnePoint;

import OnePoint.Model.Event.PointCreated;
import OnePoint.Model.Event.PointDisappeared;
import javax.persistence.*;
import org.springframework.beans.BeanUtils;

@Entity
@Table(name="Point_table")
public class Point {

    @Id
   // @GeneratedValue(strategy=GenerationType.AUTO)
    private Long memberId;
    private Double point;

    @PostPersist
    public void onPostPersist(){

        PointCreated pointCreated = new PointCreated();
        BeanUtils.copyProperties(this, pointCreated);
        pointCreated.publishAfterCommit();
    }

    @PostUpdate
    public void onPostUpdate(){
        System.out.println("*******OnPostUpdate");
     //   PointIncreased pointIncreased = new PointIncreased();
     //   BeanUtils.copyProperties(this, pointIncreased);
     //   pointIncreased.publishAfterCommit();


    }

    @PrePersist
    public void onPrePersist(){
     //   PointDecreased pointDecreased = new PointDecreased();
     //   BeanUtils.copyProperties(this, pointDecreased);
     //   pointDecreased.publishAfterCommit();
    }

    @PreRemove
    public void onPreRemove(){
        PointDisappeared pointDisappeared = new PointDisappeared();
        BeanUtils.copyProperties(this, pointDisappeared);
        pointDisappeared.publishAfterCommit();


    }


    public Long getMemberId() {
        return memberId;
    }

    public void setMemberId(Long memberId) {
        this.memberId = memberId;
    }


    public Double getPoint() {
        return point;
    }

    public void setPoint(Double point) {
        this.point = point;
    }
}
