package com.ycs.lms.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class TariffCalculationService {

    private final HSCodeService hsCodeService;
    private final ExchangeRateService exchangeRateService;
    
    // 기본 관세율 (HS Code별)
    private static final Map<String, BigDecimal> DEFAULT_TARIFF_RATES = Map.of(
        "61", new BigDecimal("13.0"),    // 의류 편물
        "62", new BigDecimal("13.0"),    // 의류 직물
        "84", new BigDecimal("8.0"),     // 기계류
        "85", new BigDecimal("8.0"),     // 전자제품
        "33", new BigDecimal("8.0"),     // 화장품
        "95", new BigDecimal("8.0"),     // 장난감
        "19", new BigDecimal("8.0"),     // 식품
        "39", new BigDecimal("6.5"),     // 플라스틱
        "48", new BigDecimal("0.0"),     // 종이제품
        "49", new BigDecimal("0.0")      // 서적
    );
    
    // 부가세율
    private static final BigDecimal VAT_RATE = new BigDecimal("10.0");
    
    // 특별소비세율 (일부 품목)
    private static final Map<String, BigDecimal> SPECIAL_TAX_RATES = Map.of(
        "3303", new BigDecimal("7.0"),   // 향수
        "3304", new BigDecimal("7.0"),   // 화장품
        "2203", new BigDecimal("30.0"),  // 맥주
        "2204", new BigDecimal("30.0")   // 와인
    );
    
    /**
     * 관세 계산
     */
    public TariffCalculationResult calculateTariff(TariffCalculationRequest request) {
        log.info("Calculating tariff for HS Code: {}", request.getHsCode());
        
        TariffCalculationResult result = new TariffCalculationResult();
        result.setHsCode(request.getHsCode());
        result.setCurrency(request.getCurrency());
        result.setQuantity(request.getQuantity());
        result.setUnitPrice(request.getUnitPrice());
        
        // 1. 물품가격 계산
        BigDecimal totalValue = request.getUnitPrice().multiply(BigDecimal.valueOf(request.getQuantity()));
        result.setTotalValue(totalValue);
        
        // 2. 환율 적용 (KRW로 변환)
        BigDecimal exchangeRate = exchangeRateService.getExchangeRate(request.getCurrency());
        BigDecimal krwValue = totalValue.multiply(exchangeRate);
        result.setExchangeRate(exchangeRate);
        result.setKrwValue(krwValue);
        
        // 3. 관세율 조회
        BigDecimal tariffRate = getTariffRate(request.getHsCode());
        result.setTariffRate(tariffRate);
        
        // 4. 관세 계산
        BigDecimal tariffAmount = krwValue.multiply(tariffRate).divide(new BigDecimal("100"), 0, RoundingMode.HALF_UP);
        result.setTariffAmount(tariffAmount);
        
        // 5. 과세가격 (CIF) = 물품가격 + 관세
        BigDecimal cifValue = krwValue.add(tariffAmount);
        result.setCifValue(cifValue);
        
        // 6. 특별소비세 계산 (해당하는 경우)
        BigDecimal specialTaxRate = getSpecialTaxRate(request.getHsCode());
        BigDecimal specialTaxAmount = BigDecimal.ZERO;
        if (specialTaxRate.compareTo(BigDecimal.ZERO) > 0) {
            specialTaxAmount = cifValue.multiply(specialTaxRate).divide(new BigDecimal("100"), 0, RoundingMode.HALF_UP);
        }
        result.setSpecialTaxRate(specialTaxRate);
        result.setSpecialTaxAmount(specialTaxAmount);
        
        // 7. 부가세 계산
        BigDecimal vatBase = cifValue.add(specialTaxAmount);
        BigDecimal vatAmount = vatBase.multiply(VAT_RATE).divide(new BigDecimal("100"), 0, RoundingMode.HALF_UP);
        result.setVatRate(VAT_RATE);
        result.setVatAmount(vatAmount);
        
        // 8. 총 세금
        BigDecimal totalTax = tariffAmount.add(specialTaxAmount).add(vatAmount);
        result.setTotalTax(totalTax);
        
        // 9. 총 금액
        BigDecimal totalAmount = krwValue.add(totalTax);
        result.setTotalAmount(totalAmount);
        
        // 10. 면세 한도 체크
        checkDutyFreeLimit(result, krwValue);
        
        log.info("Tariff calculation completed: Total tax = {} KRW", totalTax);
        
        return result;
    }
    
    /**
     * 관세율 조회
     */
    private BigDecimal getTariffRate(String hsCode) {
        if (hsCode == null || hsCode.length() < 2) {
            return new BigDecimal("8.0"); // 기본 관세율
        }
        
        // HS Code 앞 2자리로 기본 관세율 적용
        String prefix = hsCode.substring(0, 2);
        BigDecimal rate = DEFAULT_TARIFF_RATES.get(prefix);
        
        if (rate != null) {
            return rate;
        }
        
        // 앞 4자리로 재시도
        if (hsCode.length() >= 4) {
            String prefix4 = hsCode.substring(0, 4);
            for (Map.Entry<String, BigDecimal> entry : DEFAULT_TARIFF_RATES.entrySet()) {
                if (prefix4.startsWith(entry.getKey())) {
                    return entry.getValue();
                }
            }
        }
        
        return new BigDecimal("8.0"); // 기본 관세율
    }
    
    /**
     * 특별소비세율 조회
     */
    private BigDecimal getSpecialTaxRate(String hsCode) {
        if (hsCode == null || hsCode.length() < 4) {
            return BigDecimal.ZERO;
        }
        
        String prefix4 = hsCode.substring(0, 4);
        return SPECIAL_TAX_RATES.getOrDefault(prefix4, BigDecimal.ZERO);
    }
    
    /**
     * 면세 한도 체크
     */
    private void checkDutyFreeLimit(TariffCalculationResult result, BigDecimal krwValue) {
        // 개인 면세 한도: USD 600 (약 660,000원)
        BigDecimal dutyFreeLimit = new BigDecimal("660000");
        
        if (krwValue.compareTo(dutyFreeLimit) <= 0) {
            result.setDutyFreeEligible(true);
            result.setDutyFreeMessage("면세 한도 내 물품입니다. (한도: 60만원)");
        } else {
            result.setDutyFreeEligible(false);
            result.setDutyFreeMessage("면세 한도를 초과하여 관세가 부과됩니다.");
        }
        
        // 소액 면세 (USD 150, 약 165,000원)
        BigDecimal smallAmountLimit = new BigDecimal("165000");
        if (krwValue.compareTo(smallAmountLimit) <= 0) {
            result.setSmallAmountExemption(true);
            result.setDutyFreeMessage("소액 면세 대상입니다. (15만원 이하)");
        }
    }
    
    /**
     * 관세 계산 요청 DTO
     */
    public static class TariffCalculationRequest {
        private String hsCode;
        private BigDecimal unitPrice;
        private Integer quantity;
        private String currency;
        private String shippingType; // AIR, SEA
        
        // Getters and Setters
        public String getHsCode() { return hsCode; }
        public void setHsCode(String hsCode) { this.hsCode = hsCode; }
        
        public BigDecimal getUnitPrice() { return unitPrice; }
        public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
        
        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
        
        public String getCurrency() { return currency; }
        public void setCurrency(String currency) { this.currency = currency; }
        
        public String getShippingType() { return shippingType; }
        public void setShippingType(String shippingType) { this.shippingType = shippingType; }
    }
    
    /**
     * 관세 계산 결과 DTO
     */
    public static class TariffCalculationResult {
        private String hsCode;
        private String currency;
        private Integer quantity;
        private BigDecimal unitPrice;
        private BigDecimal totalValue;
        private BigDecimal exchangeRate;
        private BigDecimal krwValue;
        private BigDecimal tariffRate;
        private BigDecimal tariffAmount;
        private BigDecimal cifValue;
        private BigDecimal specialTaxRate;
        private BigDecimal specialTaxAmount;
        private BigDecimal vatRate;
        private BigDecimal vatAmount;
        private BigDecimal totalTax;
        private BigDecimal totalAmount;
        private boolean dutyFreeEligible;
        private boolean smallAmountExemption;
        private String dutyFreeMessage;
        
        // Getters and Setters
        public String getHsCode() { return hsCode; }
        public void setHsCode(String hsCode) { this.hsCode = hsCode; }
        
        public String getCurrency() { return currency; }
        public void setCurrency(String currency) { this.currency = currency; }
        
        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
        
        public BigDecimal getUnitPrice() { return unitPrice; }
        public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
        
        public BigDecimal getTotalValue() { return totalValue; }
        public void setTotalValue(BigDecimal totalValue) { this.totalValue = totalValue; }
        
        public BigDecimal getExchangeRate() { return exchangeRate; }
        public void setExchangeRate(BigDecimal exchangeRate) { this.exchangeRate = exchangeRate; }
        
        public BigDecimal getKrwValue() { return krwValue; }
        public void setKrwValue(BigDecimal krwValue) { this.krwValue = krwValue; }
        
        public BigDecimal getTariffRate() { return tariffRate; }
        public void setTariffRate(BigDecimal tariffRate) { this.tariffRate = tariffRate; }
        
        public BigDecimal getTariffAmount() { return tariffAmount; }
        public void setTariffAmount(BigDecimal tariffAmount) { this.tariffAmount = tariffAmount; }
        
        public BigDecimal getCifValue() { return cifValue; }
        public void setCifValue(BigDecimal cifValue) { this.cifValue = cifValue; }
        
        public BigDecimal getSpecialTaxRate() { return specialTaxRate; }
        public void setSpecialTaxRate(BigDecimal specialTaxRate) { this.specialTaxRate = specialTaxRate; }
        
        public BigDecimal getSpecialTaxAmount() { return specialTaxAmount; }
        public void setSpecialTaxAmount(BigDecimal specialTaxAmount) { this.specialTaxAmount = specialTaxAmount; }
        
        public BigDecimal getVatRate() { return vatRate; }
        public void setVatRate(BigDecimal vatRate) { this.vatRate = vatRate; }
        
        public BigDecimal getVatAmount() { return vatAmount; }
        public void setVatAmount(BigDecimal vatAmount) { this.vatAmount = vatAmount; }
        
        public BigDecimal getTotalTax() { return totalTax; }
        public void setTotalTax(BigDecimal totalTax) { this.totalTax = totalTax; }
        
        public BigDecimal getTotalAmount() { return totalAmount; }
        public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
        
        public boolean isDutyFreeEligible() { return dutyFreeEligible; }
        public void setDutyFreeEligible(boolean dutyFreeEligible) { this.dutyFreeEligible = dutyFreeEligible; }
        
        public boolean isSmallAmountExemption() { return smallAmountExemption; }
        public void setSmallAmountExemption(boolean smallAmountExemption) { this.smallAmountExemption = smallAmountExemption; }
        
        public String getDutyFreeMessage() { return dutyFreeMessage; }
        public void setDutyFreeMessage(String dutyFreeMessage) { this.dutyFreeMessage = dutyFreeMessage; }
    }
}