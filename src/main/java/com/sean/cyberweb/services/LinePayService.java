package com.sean.cyberweb.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sean.cyberweb.domain.Order;
import com.sean.cyberweb.domain.OrderItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class LinePayService implements PaymentService {

    private final OrderService orderService;

    @Autowired
    public LinePayService(OrderService orderService){
        this.orderService = orderService;
    }

    @Value("${line.pay.api.url}")
    private String linePayApiUrl;

    @Value("${line.pay.request.path}")
    private String linePayRequestPath;

    @Value("${line.pay.confirm.path}")
    private String linePayConfirmPath;

    @Value("${line.pay.channel.id}")
    private String channelId;

    @Value("${line.pay.channel.secret}")
    private String channelSecret;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    // 初始交易
    @Override
    public String initiatePayment(Order order) {
        try {
            // 設置linePay官方規定header
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            String nonce = UUID.randomUUID().toString();
            headers.set("X-LINE-ChannelId", channelId);
            headers.set("X-LINE-Authorization-Nonce", nonce);

            // 構建請求
            Map<String, Object> requestBody = constructRequestBody(order);
            String requestBodyJson = objectMapper.writeValueAsString(requestBody);
            String signature = generateSignature(channelSecret, "/v3/payments/request", requestBodyJson, nonce);
            headers.set("X-LINE-Authorization", signature);

            // 發送請求
            HttpEntity<String> entity = new HttpEntity<>(requestBodyJson, headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(linePayApiUrl + linePayRequestPath, entity, Map.class);

            // 處理回應
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();
                if (responseBody.containsKey("info")) {
                    Map<String, Object> info = (Map<String, Object>) responseBody.get("info");
                    if (info.containsKey("paymentUrl")) {
                        Map<String, String> paymentUrlMap = (Map<String, String>) info.get("paymentUrl");
                        return paymentUrlMap.get("web"); // 返回給前端
                    }
                }
            } else {
                System.out.println("Error response from LINE Pay: " + response.getBody());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        throw new RuntimeException("Failed to initiate LINE Pay payment.");
    }

    // confirmAPI
    public void confirmPayment(String transactionId, UUID orderId, BigDecimal amount, String currency) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            String nonce = UUID.randomUUID().toString();
            headers.set("X-LINE-ChannelId", channelId);
            headers.set("X-LINE-Authorization-Nonce", nonce);

            // 構建請求體，付款金額和貨幣類型
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("amount", amount);
            requestBody.put("currency", currency);

            String requestBodyJson = objectMapper.writeValueAsString(requestBody);

            // 生成簽名
            String signature = generateSignature(channelSecret, "/v3/payments/" + transactionId + "/confirm", requestBodyJson, nonce);
            headers.set("X-LINE-Authorization", signature);

            HttpEntity<String> entity = new HttpEntity<>(requestBodyJson, headers);

            // 構造確認支付的URL
            String confirmPaymentUrl = linePayApiUrl + linePayConfirmPath.replace("{transactionId}", transactionId);

            // 發送確認付款請求
            ResponseEntity<Map> confirmResponse = restTemplate.postForEntity(confirmPaymentUrl, entity, Map.class);


            // 成功
            if (confirmResponse.getStatusCode().is2xxSuccessful() && confirmResponse.getBody() != null) {
                // 更新訂單狀態
                orderService.updateOrderStatus(orderId, Order.OrderStatus.PAID);
                System.out.println("Payment confirmation succeeded");
            } else {
                System.out.println("Error response from LINE Pay confirmation API: " + confirmResponse.getBody());
                throw new RuntimeException("Payment confirmation failed");
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("An exception occurred during payment confirmation process");
        }
    }

    // 構建package
    private Map<String, Object> constructRequestBody(Order order) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("amount", order.getTotalPrice());
        requestBody.put("currency", "TWD"); // 台幣
        requestBody.put("orderId", order.getId());

        List<Map<String, Object>> packages = getMaps(order);

        requestBody.put("packages", packages);

        // 設定跳轉url
        Map<String, String> redirectUrls = new HashMap<>();
        redirectUrls.put("confirmUrl", "http://localhost:8080/payment/confirm"); // 付款成功後返回的url
        redirectUrls.put("cancelUrl", "http://localhost:8080/site/index"); // 付款取消後返回的url
        requestBody.put("redirectUrls", redirectUrls);

        return requestBody;
    }

    // package組成方法
    private static List<Map<String, Object>> getMaps(Order order) {
        List<Map<String, Object>> packages = new ArrayList<>();
        Map<String, Object> packageItem = new HashMap<>();
        packageItem.put("id", order.getId());
        packageItem.put("amount", order.getTotalPrice()); // package總金額與訂單金額應相同
        packageItem.put("name", order.getOrderNumber()); // 用userId作為package名字

        List<Map<String, Object>> products = new ArrayList<>();
        for (OrderItem item : order.getOrderItems()) {
            Map<String, Object> product = new HashMap<>();
            product.put("id", item.getId().toString());
            product.put("name", item.getProduct().getName());
            product.put("quantity", item.getQuantity());
            product.put("price", item.getPrice());
            products.add(product);
        }
        packageItem.put("products", products);
        packages.add(packageItem);
        return packages;
    }


    // linePay官方規定的簽名檔(皆按照文件要求指定包的方法來撰寫)
    private String generateSignature(String channelSecret, String uri, String requestBody, String nonce) throws Exception {
        String message = channelSecret + uri + requestBody + nonce;
        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKeySpec = new SecretKeySpec(channelSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        mac.init(secretKeySpec);
        byte[] hash = mac.doFinal(message.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(hash);
    }
}
