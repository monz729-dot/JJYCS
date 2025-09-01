package com.ysc.lms.service;

import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * 향상된 QR 코드 생성/스캔 서비스 (ZXing 라이브러리 사용)
 */
@Service
@Slf4j
public class EnhancedQrCodeService {
    
    private static final int DEFAULT_QR_SIZE = 300;
    private static final int DEFAULT_BARCODE_WIDTH = 400;
    private static final int DEFAULT_BARCODE_HEIGHT = 100;
    
    /**
     * QR 코드 생성 (실제 ZXing 라이브러리 사용)
     */
    public Map<String, Object> generateQrCode(String data) {
        try {
            log.info("Generating QR code for data: {}", data);
            
            if (data == null || data.isEmpty()) {
                throw new IllegalArgumentException("Data cannot be null or empty");
            }
            
            // QR 코드 생성 설정
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.CHARACTER_SET, StandardCharsets.UTF_8.name());
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
            hints.put(EncodeHintType.MARGIN, 1);
            
            // QR 코드 생성
            BitMatrix bitMatrix = qrCodeWriter.encode(data, BarcodeFormat.QR_CODE, DEFAULT_QR_SIZE, DEFAULT_QR_SIZE, hints);
            
            // BufferedImage로 변환
            BufferedImage qrImage = MatrixToImageWriter.toBufferedImage(bitMatrix);
            
            // 이미지를 Base64로 인코딩
            String base64Image = encodeImageToBase64(qrImage);
            
            Map<String, Object> result = new HashMap<>();
            result.put("qrCodeData", base64Image);
            result.put("format", "PNG");
            result.put("size", DEFAULT_QR_SIZE + "x" + DEFAULT_QR_SIZE);
            result.put("data", data);
            result.put("generatedAt", LocalDateTime.now());
            
            return result;
        } catch (Exception e) {
            log.error("Error generating QR code: ", e);
            throw new RuntimeException("QR 코드 생성 실패: " + e.getMessage());
        }
    }
    
    /**
     * 박스 라벨용 QR 코드 생성
     */
    public Map<String, Object> generateBoxQrCode(String boxId, String orderNumber) {
        String qrData = String.format("BOX:%s|ORDER:%s|TS:%d", boxId, orderNumber, System.currentTimeMillis());
        return generateQrCode(qrData);
    }
    
    /**
     * 주문용 QR 코드 생성
     */
    public Map<String, Object> generateOrderQrCode(String orderNumber) {
        String qrData = String.format("ORDER:%s|TS:%d", orderNumber, System.currentTimeMillis());
        return generateQrCode(qrData);
    }
    
    /**
     * 라벨용 QR 코드 생성 (텍스트 포함)
     */
    public Map<String, Object> generateLabelQrCode(String code, String labelText) {
        try {
            // QR 코드 생성
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.CHARACTER_SET, StandardCharsets.UTF_8.name());
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
            hints.put(EncodeHintType.MARGIN, 1);
            
            BitMatrix bitMatrix = qrCodeWriter.encode(code, BarcodeFormat.QR_CODE, DEFAULT_QR_SIZE, DEFAULT_QR_SIZE, hints);
            BufferedImage qrImage = MatrixToImageWriter.toBufferedImage(bitMatrix);
            
            // 텍스트를 포함한 라벨 이미지 생성
            int labelHeight = DEFAULT_QR_SIZE + 60;
            BufferedImage labelImage = new BufferedImage(DEFAULT_QR_SIZE, labelHeight, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = labelImage.createGraphics();
            
            // 안티앨리어싱 설정
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            
            // 배경 설정
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, DEFAULT_QR_SIZE, labelHeight);
            
            // QR 코드 그리기
            g.drawImage(qrImage, 0, 0, null);
            
            // 텍스트 추가
            g.setColor(Color.BLACK);
            g.setFont(new Font("Arial", Font.BOLD, 14));
            FontMetrics fm = g.getFontMetrics();
            int textWidth = fm.stringWidth(labelText);
            int textX = (DEFAULT_QR_SIZE - textWidth) / 2;
            g.drawString(labelText, textX, DEFAULT_QR_SIZE + 25);
            
            // 작은 텍스트로 코드 추가
            g.setFont(new Font("Arial", Font.PLAIN, 10));
            fm = g.getFontMetrics();
            textWidth = fm.stringWidth(code);
            textX = (DEFAULT_QR_SIZE - textWidth) / 2;
            g.drawString(code, textX, DEFAULT_QR_SIZE + 45);
            
            g.dispose();
            
            // 이미지를 Base64로 인코딩
            String base64Image = encodeImageToBase64(labelImage);
            
            Map<String, Object> result = new HashMap<>();
            result.put("qrCodeData", base64Image);
            result.put("format", "PNG");
            result.put("size", DEFAULT_QR_SIZE + "x" + labelHeight);
            result.put("data", code);
            result.put("label", labelText);
            result.put("generatedAt", LocalDateTime.now());
            
            return result;
        } catch (Exception e) {
            log.error("Error generating label QR code: ", e);
            throw new RuntimeException("라벨 QR 코드 생성 실패: " + e.getMessage());
        }
    }
    
    /**
     * QR 코드 스캔 (디코딩)
     */
    public Map<String, String> scanQrCode(String base64Image) {
        try {
            // Base64 디코딩
            String imageData = base64Image;
            if (base64Image.contains(",")) {
                imageData = base64Image.split(",")[1];
            }
            
            byte[] imageBytes = Base64.getDecoder().decode(imageData);
            BufferedImage bufferedImage = ImageIO.read(new ByteArrayInputStream(imageBytes));
            
            if (bufferedImage == null) {
                throw new RuntimeException("Invalid image data");
            }
            
            // QR 코드 디코딩
            LuminanceSource source = new BufferedImageLuminanceSource(bufferedImage);
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
            
            Map<DecodeHintType, Object> hints = new HashMap<>();
            hints.put(DecodeHintType.CHARACTER_SET, StandardCharsets.UTF_8.name());
            
            Result result = new MultiFormatReader().decode(bitmap, hints);
            String decodedText = result.getText();
            
            log.info("Successfully decoded QR code: {}", decodedText);
            
            // 데이터 파싱
            return parseQrCodeData(decodedText);
            
        } catch (NotFoundException e) {
            log.error("QR code not found in image");
            throw new RuntimeException("이미지에서 QR 코드를 찾을 수 없습니다");
        } catch (Exception e) {
            log.error("Error scanning QR code: ", e);
            throw new RuntimeException("QR 코드 스캔 실패: " + e.getMessage());
        }
    }
    
    /**
     * QR 코드 데이터 파싱
     */
    private Map<String, String> parseQrCodeData(String qrData) {
        Map<String, String> dataMap = new HashMap<>();
        
        if (qrData == null || qrData.isEmpty()) {
            return dataMap;
        }
        
        String[] parts = qrData.split("\\|");
        for (String part : parts) {
            String[] keyValue = part.split(":");
            if (keyValue.length == 2) {
                dataMap.put(keyValue[0].trim(), keyValue[1].trim());
            }
        }
        
        return dataMap;
    }
    
    /**
     * QR 코드 유효성 검증
     */
    public boolean validateQrCode(String qrData, String expectedType) {
        try {
            Map<String, String> dataMap = parseQrCodeData(qrData);
            
            // 타입 확인
            if (expectedType != null) {
                String type = dataMap.containsKey("ORDER") ? "ORDER" : 
                             dataMap.containsKey("BOX") ? "BOX" : "UNKNOWN";
                if (!expectedType.equals(type)) {
                    log.warn("QR code type mismatch. Expected: {}, Actual: {}", expectedType, type);
                    return false;
                }
            }
            
            // 타임스탬프 확인 (24시간 이내)
            String timestamp = dataMap.get("TS");
            if (timestamp != null) {
                long ts = Long.parseLong(timestamp);
                long currentTime = System.currentTimeMillis();
                long hoursDiff = (currentTime - ts) / (1000 * 60 * 60);
                if (hoursDiff > 24) {
                    log.warn("QR code is older than 24 hours");
                    return false;
                }
            }
            
            return true;
        } catch (Exception e) {
            log.error("Failed to validate QR code: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * 바코드 생성 (Code128 형식)
     */
    public Map<String, Object> generateBarcode(String data) {
        try {
            if (data == null || data.isEmpty()) {
                throw new IllegalArgumentException("Data cannot be null or empty");
            }
            
            com.google.zxing.oned.Code128Writer barcodeWriter = new com.google.zxing.oned.Code128Writer();
            BitMatrix bitMatrix = barcodeWriter.encode(data, BarcodeFormat.CODE_128, DEFAULT_BARCODE_WIDTH, DEFAULT_BARCODE_HEIGHT);
            
            BufferedImage barcodeImage = MatrixToImageWriter.toBufferedImage(bitMatrix);
            String base64Image = encodeImageToBase64(barcodeImage);
            
            Map<String, Object> result = new HashMap<>();
            result.put("barcodeData", base64Image);
            result.put("format", "PNG");
            result.put("size", DEFAULT_BARCODE_WIDTH + "x" + DEFAULT_BARCODE_HEIGHT);
            result.put("data", data);
            result.put("type", "CODE_128");
            result.put("generatedAt", LocalDateTime.now());
            
            return result;
        } catch (Exception e) {
            log.error("Error generating barcode: ", e);
            throw new RuntimeException("바코드 생성 실패: " + e.getMessage());
        }
    }
    
    /**
     * 이미지를 Base64로 인코딩
     */
    private String encodeImageToBase64(BufferedImage image) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "PNG", baos);
        byte[] imageBytes = baos.toByteArray();
        return "data:image/png;base64," + Base64.getEncoder().encodeToString(imageBytes);
    }
}