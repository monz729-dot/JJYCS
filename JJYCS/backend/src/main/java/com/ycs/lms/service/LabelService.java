package com.ycs.lms.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;

@Service
@RequiredArgsConstructor
@Slf4j
public class LabelService {

    private static final int LABEL_WIDTH = 400;
    private static final int LABEL_HEIGHT = 300;
    private static final int QR_SIZE = 80;

    /**
     * 주문 라벨 생성
     */
    public LabelGenerationResult generateOrderLabel(String orderNumber, String recipientName, 
                                                   String recipientAddress, String packageType) {
        try {
            log.info("Generating label for order: {}", orderNumber);
            
            BufferedImage labelImage = createLabelImage(orderNumber, recipientName, recipientAddress, packageType);
            String qrCode = generateQRCode(orderNumber);
            
            // 이미지를 Base64로 인코딩
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(labelImage, "PNG", baos);
            String labelImageBase64 = Base64.getEncoder().encodeToString(baos.toByteArray());
            
            LabelGenerationResult result = new LabelGenerationResult();
            result.setOrderNumber(orderNumber);
            result.setLabelImageBase64(labelImageBase64);
            result.setQrCode(qrCode);
            result.setGeneratedAt(LocalDateTime.now());
            
            log.info("Label generated successfully for order: {}", orderNumber);
            return result;
            
        } catch (Exception e) {
            log.error("Failed to generate label for order: {}", orderNumber, e);
            throw new RuntimeException("라벨 생성에 실패했습니다: " + e.getMessage());
        }
    }
    
    /**
     * 박스 라벨 생성 (박스별)
     */
    public LabelGenerationResult generateBoxLabel(String orderNumber, String boxId, 
                                                 int boxNumber, int totalBoxes) {
        try {
            log.info("Generating box label for order: {}, box: {}", orderNumber, boxId);
            
            String boxLabel = String.format("%s-BOX%d/%d", orderNumber, boxNumber, totalBoxes);
            BufferedImage labelImage = createBoxLabelImage(boxLabel, orderNumber, boxId);
            String qrCode = generateQRCode(boxId);
            
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(labelImage, "PNG", baos);
            String labelImageBase64 = Base64.getEncoder().encodeToString(baos.toByteArray());
            
            LabelGenerationResult result = new LabelGenerationResult();
            result.setOrderNumber(orderNumber);
            result.setBoxId(boxId);
            result.setLabelImageBase64(labelImageBase64);
            result.setQrCode(qrCode);
            result.setGeneratedAt(LocalDateTime.now());
            
            log.info("Box label generated successfully for order: {}, box: {}", orderNumber, boxId);
            return result;
            
        } catch (Exception e) {
            log.error("Failed to generate box label for order: {}, box: {}", orderNumber, boxId, e);
            throw new RuntimeException("박스 라벨 생성에 실패했습니다: " + e.getMessage());
        }
    }
    
    /**
     * QR 코드 생성 (텍스트로 간단 구현)
     */
    private String generateQRCode(String data) {
        // 실제 구현에서는 ZXing 라이브러리 등을 사용
        return "QR:" + data + ":" + System.currentTimeMillis();
    }
    
    /**
     * 라벨 이미지 생성
     */
    private BufferedImage createLabelImage(String orderNumber, String recipientName, 
                                         String recipientAddress, String packageType) {
        BufferedImage image = new BufferedImage(LABEL_WIDTH, LABEL_HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();
        
        // 안티앨리어싱 설정
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // 배경 색상
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, LABEL_WIDTH, LABEL_HEIGHT);
        
        // 테두리
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(2));
        g2d.drawRect(5, 5, LABEL_WIDTH - 10, LABEL_HEIGHT - 10);
        
        // 제목
        g2d.setFont(new Font("Arial", Font.BOLD, 16));
        g2d.drawString("YCS LOGISTICS", 20, 30);
        
        // 주문번호
        g2d.setFont(new Font("Arial", Font.BOLD, 14));
        g2d.drawString("Order: " + orderNumber, 20, 60);
        
        // 수취인 정보
        g2d.setFont(new Font("Arial", Font.PLAIN, 12));
        g2d.drawString("To: " + recipientName, 20, 90);
        
        // 주소 (여러 줄로 분할)
        String[] addressLines = splitAddress(recipientAddress, 45);
        int yPos = 110;
        for (String line : addressLines) {
            g2d.drawString(line, 20, yPos);
            yPos += 18;
        }
        
        // 패키지 타입
        g2d.setFont(new Font("Arial", Font.BOLD, 12));
        g2d.drawString("Type: " + packageType, 20, yPos + 10);
        
        // QR 코드 영역 (간단한 사각형으로 표시)
        g2d.setColor(Color.LIGHT_GRAY);
        g2d.fillRect(LABEL_WIDTH - QR_SIZE - 20, 20, QR_SIZE, QR_SIZE);
        g2d.setColor(Color.BLACK);
        g2d.drawRect(LABEL_WIDTH - QR_SIZE - 20, 20, QR_SIZE, QR_SIZE);
        g2d.setFont(new Font("Arial", Font.PLAIN, 10));
        g2d.drawString("QR CODE", LABEL_WIDTH - QR_SIZE - 5, QR_SIZE + 35);
        
        // 생성 시간
        g2d.setFont(new Font("Arial", Font.PLAIN, 10));
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        g2d.drawString("Generated: " + timestamp, 20, LABEL_HEIGHT - 20);
        
        g2d.dispose();
        return image;
    }
    
    /**
     * 박스 라벨 이미지 생성
     */
    private BufferedImage createBoxLabelImage(String boxLabel, String orderNumber, String boxId) {
        BufferedImage image = new BufferedImage(LABEL_WIDTH, LABEL_HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();
        
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // 배경
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, LABEL_WIDTH, LABEL_HEIGHT);
        
        // 테두리
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(2));
        g2d.drawRect(5, 5, LABEL_WIDTH - 10, LABEL_HEIGHT - 10);
        
        // 제목
        g2d.setFont(new Font("Arial", Font.BOLD, 16));
        g2d.drawString("YCS BOX LABEL", 20, 30);
        
        // 박스 라벨
        g2d.setFont(new Font("Arial", Font.BOLD, 18));
        g2d.drawString(boxLabel, 20, 70);
        
        // 주문번호
        g2d.setFont(new Font("Arial", Font.PLAIN, 14));
        g2d.drawString("Order: " + orderNumber, 20, 100);
        
        // 박스 ID
        g2d.drawString("Box ID: " + boxId, 20, 130);
        
        // QR 코드 영역
        g2d.setColor(Color.LIGHT_GRAY);
        g2d.fillRect(LABEL_WIDTH - QR_SIZE - 20, 50, QR_SIZE, QR_SIZE);
        g2d.setColor(Color.BLACK);
        g2d.drawRect(LABEL_WIDTH - QR_SIZE - 20, 50, QR_SIZE, QR_SIZE);
        g2d.setFont(new Font("Arial", Font.PLAIN, 10));
        g2d.drawString("BOX QR", LABEL_WIDTH - QR_SIZE - 5, QR_SIZE + 65);
        
        // 스캔 안내
        g2d.setFont(new Font("Arial", Font.PLAIN, 12));
        g2d.drawString("Scan for tracking", 20, 180);
        
        // 생성 시간
        g2d.setFont(new Font("Arial", Font.PLAIN, 10));
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        g2d.drawString("Generated: " + timestamp, 20, LABEL_HEIGHT - 20);
        
        g2d.dispose();
        return image;
    }
    
    /**
     * 주소를 여러 줄로 분할
     */
    private String[] splitAddress(String address, int maxCharsPerLine) {
        if (address.length() <= maxCharsPerLine) {
            return new String[]{address};
        }
        
        int numLines = (int) Math.ceil((double) address.length() / maxCharsPerLine);
        String[] lines = new String[numLines];
        
        for (int i = 0; i < numLines; i++) {
            int start = i * maxCharsPerLine;
            int end = Math.min(start + maxCharsPerLine, address.length());
            lines[i] = address.substring(start, end);
        }
        
        return lines;
    }
    
    /**
     * 라벨 생성 결과 DTO
     */
    public static class LabelGenerationResult {
        private String orderNumber;
        private String boxId;
        private String labelImageBase64;
        private String qrCode;
        private LocalDateTime generatedAt;
        
        // Getters and Setters
        public String getOrderNumber() { return orderNumber; }
        public void setOrderNumber(String orderNumber) { this.orderNumber = orderNumber; }
        
        public String getBoxId() { return boxId; }
        public void setBoxId(String boxId) { this.boxId = boxId; }
        
        public String getLabelImageBase64() { return labelImageBase64; }
        public void setLabelImageBase64(String labelImageBase64) { this.labelImageBase64 = labelImageBase64; }
        
        public String getQrCode() { return qrCode; }
        public void setQrCode(String qrCode) { this.qrCode = qrCode; }
        
        public LocalDateTime getGeneratedAt() { return generatedAt; }
        public void setGeneratedAt(LocalDateTime generatedAt) { this.generatedAt = generatedAt; }
    }
    
    /**
     * 라벨 생성 요청 DTO
     */
    public static class LabelGenerationRequest {
        private String orderNumber;
        private String recipientName;
        private String recipientAddress;
        private String packageType;
        private String boxId;
        private Integer boxNumber;
        private Integer totalBoxes;
        
        // Getters and Setters
        public String getOrderNumber() { return orderNumber; }
        public void setOrderNumber(String orderNumber) { this.orderNumber = orderNumber; }
        
        public String getRecipientName() { return recipientName; }
        public void setRecipientName(String recipientName) { this.recipientName = recipientName; }
        
        public String getRecipientAddress() { return recipientAddress; }
        public void setRecipientAddress(String recipientAddress) { this.recipientAddress = recipientAddress; }
        
        public String getPackageType() { return packageType; }
        public void setPackageType(String packageType) { this.packageType = packageType; }
        
        public String getBoxId() { return boxId; }
        public void setBoxId(String boxId) { this.boxId = boxId; }
        
        public Integer getBoxNumber() { return boxNumber; }
        public void setBoxNumber(Integer boxNumber) { this.boxNumber = boxNumber; }
        
        public Integer getTotalBoxes() { return totalBoxes; }
        public void setTotalBoxes(Integer totalBoxes) { this.totalBoxes = totalBoxes; }
    }
}