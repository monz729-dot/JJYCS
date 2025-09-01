package com.ysc.lms.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
public class QrCodeService {
    
    /**
     * QR 코드 생성
     */
    public Map<String, Object> generateQrCode(String data) {
        try {
            log.info("Generating QR code for data: {}", data);
            
            // 간단한 QR 코드 대신 텍스트 이미지 생성 (실제 구현 시 ZXing 라이브러리 사용)
            BufferedImage qrImage = createTextImage(data, 200, 200);
            
            // 이미지를 Base64로 인코딩
            String base64Image = encodeImageToBase64(qrImage);
            
            Map<String, Object> result = new HashMap<>();
            result.put("qrCodeData", base64Image);
            result.put("format", "PNG");
            result.put("size", "200x200");
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
        String qrData = String.format("BOX:%s|ORDER:%s", boxId, orderNumber);
        return generateQrCode(qrData);
    }
    
    /**
     * 주문용 QR 코드 생성
     */
    public Map<String, Object> generateOrderQrCode(String orderNumber) {
        String qrData = String.format("ORDER:%s", orderNumber);
        return generateQrCode(qrData);
    }
    
    /**
     * 텍스트 이미지 생성 (QR 코드 대신 임시로 사용)
     */
    private BufferedImage createTextImage(String text, int width, int height) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();
        
        // 안티앨리어싱 설정
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // 배경 흰색으로 채우기
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, width, height);
        
        // 테두리 그리기
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(2));
        g2d.drawRect(5, 5, width - 10, height - 10);
        
        // 텍스트 설정
        Font font = new Font("Arial", Font.BOLD, 12);
        g2d.setFont(font);
        FontMetrics fm = g2d.getFontMetrics();
        
        // 텍스트를 여러 줄로 나누어 표시
        String[] lines = text.split("\\|");
        int lineHeight = fm.getHeight();
        int startY = (height - (lines.length * lineHeight)) / 2 + fm.getAscent();
        
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            int stringWidth = fm.stringWidth(line);
            int startX = (width - stringWidth) / 2;
            g2d.drawString(line, startX, startY + i * lineHeight);
        }
        
        // QR 코드처럼 보이는 패턴 추가
        drawQrPattern(g2d, width, height);
        
        g2d.dispose();
        return image;
    }
    
    /**
     * QR 코드 패턴 그리기
     */
    private void drawQrPattern(Graphics2D g2d, int width, int height) {
        g2d.setColor(Color.BLACK);
        
        // 모서리에 사각형 패턴 그리기
        int squareSize = 20;
        int margin = 15;
        
        // 왼쪽 위
        drawFinderPattern(g2d, margin, margin, squareSize);
        
        // 오른쪽 위
        drawFinderPattern(g2d, width - margin - squareSize, margin, squareSize);
        
        // 왼쪽 아래
        drawFinderPattern(g2d, margin, height - margin - squareSize, squareSize);
        
        // 중앙에 작은 패턴들
        for (int x = 60; x < width - 60; x += 15) {
            for (int y = 60; y < height - 60; y += 15) {
                if (Math.random() > 0.5) {
                    g2d.fillRect(x, y, 3, 3);
                }
            }
        }
    }
    
    /**
     * 파인더 패턴 그리기 (QR 코드의 모서리 사각형)
     */
    private void drawFinderPattern(Graphics2D g2d, int x, int y, int size) {
        // 외곽 사각형
        g2d.fillRect(x, y, size, size);
        
        // 내부 흰색 사각형
        g2d.setColor(Color.WHITE);
        g2d.fillRect(x + 2, y + 2, size - 4, size - 4);
        
        // 중앙 검은색 사각형
        g2d.setColor(Color.BLACK);
        g2d.fillRect(x + 6, y + 6, size - 12, size - 12);
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