package com.example.doanck.util;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Base64;
import javax.imageio.ImageIO;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.Result;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.client.j2se.MatrixToImageWriter;

@Service
public class QRCodeService {

    public String generateQRCode(String text) {

        try {

            QRCodeWriter writer = new QRCodeWriter();

            BitMatrix matrix = writer.encode(
                    text,
                    BarcodeFormat.QR_CODE,
                    300,
                    300);

            ByteArrayOutputStream stream =
                    new ByteArrayOutputStream();

            MatrixToImageWriter.writeToStream(
                    matrix,
                    "PNG",
                    stream);

            byte[] qrBytes = stream.toByteArray();

            return "data:image/png;base64," +
                    Base64.getEncoder().encodeToString(qrBytes);

        } catch (Exception e) {

            throw new RuntimeException("QR generation failed");

        }

    }

    public String decodeQRCode(MultipartFile file) {

        try {

            if (file == null || file.isEmpty()) {
                return null;
            }

            BufferedImage bufferedImage = ImageIO.read(file.getInputStream());
            if (bufferedImage == null) {
                return null;
            }

            BinaryBitmap bitmap = new BinaryBitmap(
                    new HybridBinarizer(new BufferedImageLuminanceSource(bufferedImage)));

            Result result = new MultiFormatReader().decode(bitmap);
            return result.getText();

        } catch (Exception e) {

            return null;

        }

    }

}
