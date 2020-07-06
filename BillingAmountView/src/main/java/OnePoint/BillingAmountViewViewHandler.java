package OnePoint;

import OnePoint.config.kafka.KafkaProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
public class BillingAmountViewViewHandler {

    static final double commission = 0.02;
    @Autowired
    private BillingAmountViewRepository billingAmountViewRepository;

    @StreamListener(KafkaProcessor.INPUT)
    public void whenUseRequested_then_CREATE_1 (@Payload UseRequested useRequested) {
        try {
            if (useRequested.isMe()) {
                System.out.println("##### listener MemberCreated : " + useRequested.toJson());
                // view 객체 생성
                BillingAmountView billingAmountView = new BillingAmountView();

                // view 객체에 이벤트의 Value 를 set 함
                billingAmountView.setId(useRequested.getId());
                billingAmountView.setMerchantId(useRequested.getMerchantId());
                billingAmountView.setDealDate(useRequested.getDealDate());
                billingAmountView.setType(useRequested.getType());
                billingAmountView.setPoint(useRequested.getPoint());

                //수수료를 뗀 정산금액을 계산해서 넣어줌 : 사용자가 쓴 포인트 x 0.98을 가맹점에 지급해야
                billingAmountView.setBillingAmount(useRequested.getPoint()*(1-commission));
                System.out.println("생성된금액"+billingAmountView.getBillingAmount());

                // view 레파지 토리에 save
                billingAmountViewRepository.save(billingAmountView);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }



}