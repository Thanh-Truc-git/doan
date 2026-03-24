package com.example.doanck.util;

import java.io.ByteArrayOutputStream;
import java.util.Base64;

import org.springframework.stereotype.Service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.common.BitMatrix;
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

}