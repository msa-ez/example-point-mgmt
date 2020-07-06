package OnePoint;

import java.util.Date;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface BillingAmountViewRepository extends CrudRepository<BillingAmountView, Long> {


  @Query("select b from BillingAmountView b where b.merchantId= :merchantId and b.dealDate >= :startDate and b.dealDate <= :endDate ")
  List<BillingAmountView> findBillingListByMercharntIdAndBillngMont(
      @Param("merchantId") Long merchantId, @Param("startDate") Date startDate,
      @Param("endDate") Date endDate);

}