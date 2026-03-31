package com.example.doanck.config;

public class VNPayConfig {
    public static String vnp_TmnCode = "ONCIECR4";
    public static String vnp_HashSecret = "E24ASC935Z1ETC7C91IOHPJFKR3HZWL5";
    public static String vnp_Url = "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html";

    // ĐẢM BẢO PORT NÀY PHẢI KHỚP VỚI PORT SERVER ĐANG CHẠY (8080 HOẶC 9090)
    public static String vnp_ReturnUrl = "http://localhost:8080/payment-return";
}