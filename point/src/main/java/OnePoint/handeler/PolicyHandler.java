package OnePoint.handeler;

import OnePoint.Model.Event.MemberCreated;
import OnePoint.Model.Event.MemberSecession;
import OnePoint.Point;
import OnePoint.config.kafka.KafkaProcessor;
import OnePoint.repostory.PointRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Service
public class PolicyHandler{

    @Autowired
    PointRepository pointRepository;

    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverMemberSecession_PointDisappeared(@Payload MemberSecession memberSecession){

        if(memberSecession.isMe()){

            System.out.println("##### listener PointDisappeared : " + memberSecession.toJson());
        }
    }
    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverMemberCreated_MemberCreated(@Payload MemberCreated memberCreated){

        if(memberCreated.isMe()){

            Point point =new Point();
            point.setMemberId(memberCreated.getMemberId());
            point.setPoint(0.0);

            pointRepository.save(point);

            System.out.println("##### listener MemberCreated : " + memberCreated.toJson());
        }
    }

}
