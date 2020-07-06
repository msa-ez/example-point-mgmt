package OnePoint;

public class Billed extends AbstractEvent {

    private Long id;
    private Long merchantId;
    private Float chargedAmount;

    public Billed(){
        super();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    public Long getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(Long merchantId) {
        this.merchantId = merchantId;
    }
    public Float getChargedAmount() {
        return chargedAmount;
    }

    public void setChargedAmount(Float chargedAmount) {
        this.chargedAmount = chargedAmount;
    }
}
