package com.example.doanck.config;

public class VNPayConfig {

    // Terminal ID do VNPay cung cấp
    public static String vnp_TmnCode = "ONCIECR4";

    // Secret key để tạo chữ ký
    public static String vnp_HashSecret = "E24ASC935Z1ETC7C91IOHPJFKR3HZWL5";

    // URL thanh toán sandbox
    public static String vnp_Url = "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html";

    // URL VNPay trả kết quả về
    public static String vnp_ReturnUrl =
            "http://localhost:9090/payment/return";

}