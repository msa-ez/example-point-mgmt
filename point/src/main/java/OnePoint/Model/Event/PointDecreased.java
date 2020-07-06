package OnePoint.Model.Event;

import OnePoint.AbstractEvent;

public class PointDecreased extends AbstractEvent {

    private Long id;

    public PointDecreased(){
        super();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
