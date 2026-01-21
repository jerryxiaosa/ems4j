package info.zhihui.ems.business.finance.service.order.thirdparty.wx.sdk;

import com.wechat.pay.java.core.RSAConfig;
import com.wechat.pay.java.core.cipher.AeadAesCipher;
import com.wechat.pay.java.core.cipher.AeadCipher;
import com.wechat.pay.java.core.http.*;
import com.wechat.pay.java.core.notification.NotificationParser;
import com.wechat.pay.java.core.notification.RSANotificationConfig;
import com.wechat.pay.java.core.notification.RequestParam;
import com.wechat.pay.java.core.util.NonceUtil;
import com.wechat.pay.java.service.billdownload.BillDownloadServiceExtension;
import com.wechat.pay.java.service.billdownload.DigestBillEntity;
import com.wechat.pay.java.service.billdownload.model.GetTradeBillRequest;
import com.wechat.pay.java.service.certificate.model.Data;
import com.wechat.pay.java.service.certificate.model.DownloadCertificateResponse;
import com.wechat.pay.java.service.certificate.model.EncryptCertificate;
import com.wechat.pay.java.service.payments.jsapi.JsapiService;
import com.wechat.pay.java.service.payments.jsapi.model.*;
import com.wechat.pay.java.service.payments.model.Transaction;
import com.wechat.pay.java.service.refund.RefundService;
import com.wechat.pay.java.service.refund.model.AmountReq;
import com.wechat.pay.java.service.refund.model.CreateRequest;
import com.wechat.pay.java.service.refund.model.QueryByOutRefundNoRequest;
import com.wechat.pay.java.service.refund.model.Refund;
import info.zhihui.ems.business.finance.dto.order.thirdparty.wx.WxRefundQuery;
import info.zhihui.ems.business.finance.dto.order.thirdparty.wx.WxPrepayQuery;
import info.zhihui.ems.business.finance.dto.order.thirdparty.wx.WxPayConfig;
import info.zhihui.ems.business.finance.dto.order.thirdparty.wx.WxRefundConfig;
import info.zhihui.ems.business.finance.utils.MoneyUtil;
import info.zhihui.ems.common.exception.BusinessRuntimeException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Service
@Slf4j
public class WxMiniProgramPaySdk {
    private static final DateTimeFormatter DTF = DateTimeFormatter
            .ofPattern("yyyy-MM-dd'T'HH:mm:ss+08:00")
            .withZone(ZoneId.of("Asia/Shanghai"));

    // 人民币
    private static final String DEFAULT_CURRENCY = "CNY";

    private static final String RSA_URL = "https://api.mch.weixin.qq.com/v3/certificates";

    public String getPrepayId(WxPrepayQuery wxPrepayQuery, WxPayConfig wxPayConfig) {
        JsapiService service = initService(wxPayConfig);

        return getPrepayIdByService(wxPrepayQuery, service, wxPayConfig);
    }

    public PrepayWithRequestPaymentResponse getPrepayResponseByPrePayId(String prepayId, WxPayConfig wxPayConfig) {
        RSAConfig config = buildRSAConfig(wxPayConfig);

        long timestamp = Instant.now().getEpochSecond();
        String nonceStr = NonceUtil.createNonce(32);
        String packageVal = "prepay_id=" + prepayId;
        String message = wxPayConfig.getAppId() + "\n" + timestamp + "\n" + nonceStr + "\n" + packageVal + "\n";
        log.debug("Message for RequestPayment signatures is[{}]", message);
        String sign = config.createSigner().sign(message).getSign();
        PrepayWithRequestPaymentResponse response = new PrepayWithRequestPaymentResponse();
        response.setAppId(wxPayConfig.getAppId());
        response.setTimeStamp(String.valueOf(timestamp));
        response.setNonceStr(nonceStr);
        response.setPackageVal(packageVal);
        response.setSignType("RSA");
        response.setPaySign(sign);
        return response;
    }

    private JsapiService initService(WxPayConfig wxPayConfig) {
        RSAConfig config = buildRSAConfig(wxPayConfig);

        return new JsapiService.Builder()
                .config(config)
                .build();
    }

    private RSAConfig buildRSAConfig(WxPayConfig wxPayConfig) {
        return new RSAConfig.Builder()
                .merchantId(wxPayConfig.getMerchantId())
                .privateKeyFromPath(wxPayConfig.getPrivateKeyPath())
                .merchantSerialNumber(wxPayConfig.getMerchantSerialNumber())
                .wechatPayCertificatesFromPath(wxPayConfig.getWechatPayCertificatePath())
                .build();
    }

    private String getPrepayIdByService(WxPrepayQuery wxPrepayQuery, JsapiService service, WxPayConfig wxPayConfig) {
        PrepayRequest request = new PrepayRequest();
        request.setAppid(wxPayConfig.getAppId());
        request.setMchid(wxPayConfig.getMerchantId());
        request.setDescription(wxPrepayQuery.getDescription());
        request.setOutTradeNo(wxPrepayQuery.getOutTradeNo());

        // 支付截止早于订单过期的时间，避免支付成功后系统订单超时
        LocalDateTime expireTime = wxPrepayQuery.getExpireTime();
        request.setTimeExpire(DTF.format(expireTime));
        request.setNotifyUrl(wxPayConfig.getNotifyUrl());
        request.setSupportFapiao(false);

        Amount amount = new Amount();
        // 默认人民币
        amount.setCurrency(DEFAULT_CURRENCY);
        amount.setTotal(MoneyUtil.yuan2fen(wxPrepayQuery.getAmount()));
        request.setAmount(amount);

        Payer payer = new Payer();
        payer.setOpenid(wxPrepayQuery.getOpenId());
        request.setPayer(payer);

        SettleInfo settleInfo = new SettleInfo();
        settleInfo.setProfitSharing(false);
        request.setSettleInfo(settleInfo);

        // 调用接口
        return service.prepay(request).getPrepayId();
    }

    public void closePay(String orderSn, WxPayConfig wxPayConfig) {
        JsapiService service = initService(wxPayConfig);

        CloseOrderRequest request = new CloseOrderRequest();
        request.setOutTradeNo(orderSn);
        request.setMchid(wxPayConfig.getMerchantId());

        service.closeOrder(request);
    }

    public Transaction queryOrderByOutTradeNo(String orderSn, WxPayConfig wxPayConfig) {
        JsapiService service = initService(wxPayConfig);

        QueryOrderByOutTradeNoRequest request = new QueryOrderByOutTradeNoRequest();
        request.setOutTradeNo(orderSn);
        request.setMchid(wxPayConfig.getMerchantId());

        Transaction transaction = service.queryOrderByOutTradeNo(request);
        log.info("查询到订单[{}]状态： {}", orderSn, transaction.getTradeState().name());

        return transaction;
    }

    public <T> T parseWeiXinNotification(RequestParam requestParam, WxPayConfig wxPayConfig, Class<T> clazz) {
        try {
            RSANotificationConfig config = new RSANotificationConfig.Builder()
                    .apiV3Key(wxPayConfig.getApiV3Key())
                    .certificatesFromPath(wxPayConfig.getWechatPayCertificatePath())
                    .build();

            NotificationParser parser = new NotificationParser(config);

            return parser.parse(requestParam, clazz);
        } catch (Exception e) {
            log.error("微信回调解析异常", e);
            throw new BusinessRuntimeException("微信回调解析异常");
        }
    }

    public Refund createRefund(WxRefundQuery wxRefundQuery, WxRefundConfig wxRefundConfig) {
        RSAConfig config = buildRSAConfig(wxRefundConfig);
        RefundService refundService = new RefundService.Builder().config(config).build();

        AmountReq amountReq = new AmountReq();
        amountReq.setRefund((long) MoneyUtil.yuan2fen(wxRefundQuery.getRefundAmount()));
        amountReq.setTotal((long) MoneyUtil.yuan2fen(wxRefundQuery.getOrderAmount()));
        amountReq.setCurrency(DEFAULT_CURRENCY);

        CreateRequest createRequest = new CreateRequest();
        createRequest.setAmount(amountReq);
        createRequest.setOutRefundNo(wxRefundQuery.getRefundSn());
        createRequest.setReason(wxRefundQuery.getReason());
        createRequest.setOutTradeNo(wxRefundQuery.getOrderSn());
        createRequest.setNotifyUrl(wxRefundConfig.getRefundNotifyUrl());

        return refundService.create(createRequest);
    }

    public Refund queryByOutRefundNo(String refundSn, WxRefundConfig wxRefundConfig) {
        RSAConfig config = buildRSAConfig(wxRefundConfig);
        RefundService refundService = new RefundService.Builder().config(config).build();

        QueryByOutRefundNoRequest queryByOutRefundNoRequest = new QueryByOutRefundNoRequest();
        queryByOutRefundNoRequest.setOutRefundNo(refundSn);
        return refundService.queryByOutRefundNo(queryByOutRefundNoRequest);
    }

    public DigestBillEntity getTradeBill(GetTradeBillRequest request, WxPayConfig wxPayConfig) {
        RSAConfig config = buildRSAConfig(wxPayConfig);

        BillDownloadServiceExtension billDownloadServiceExtension = new BillDownloadServiceExtension.Builder()
                .config(config)
                .build();
        return billDownloadServiceExtension.getTradeBill(request);
    }

    public List<PlainCertificateItem> downloadCertificate(WxPayConfig wxPayConfig) {
        AeadCipher aeadCipher = new AeadAesCipher(wxPayConfig.getApiV3Key().getBytes(StandardCharsets.UTF_8));
        HttpClient httpClient = new DefaultHttpClientBuilder().config(buildRSAConfig(wxPayConfig)).build();

        HttpRequest request =
                new HttpRequest.Builder()
                        .httpMethod(HttpMethod.GET)
                        .url(RSA_URL)
                        .addHeader(Constant.ACCEPT, " */*")
                        .addHeader(Constant.CONTENT_TYPE, MediaType.APPLICATION_JSON.getValue())
                        .build();
        HttpResponse<DownloadCertificateResponse> httpResponse =
                httpClient.execute(request, DownloadCertificateResponse.class);
        List<Data> dataList = httpResponse.getServiceResponse().getData();

        List<PlainCertificateItem> plainCertificateItemList = new ArrayList<>();
        for (Data data : dataList) {
            EncryptCertificate encryptCertificate = data.getEncryptCertificate();
            String decryptCertificate =
                    aeadCipher.decrypt(
                            encryptCertificate.getAssociatedData().getBytes(StandardCharsets.UTF_8),
                            encryptCertificate.getNonce().getBytes(StandardCharsets.UTF_8),
                            Base64.getDecoder().decode(encryptCertificate.getCiphertext()));

            PlainCertificateItem plainCertificateItem = new PlainCertificateItem();
            plainCertificateItem.serialNo = data.getSerialNo();
            plainCertificateItem.plainCertificate = decryptCertificate;
            plainCertificateItem.expireTime = LocalDateTime.parse(data.getExpireTime(), DTF);

            plainCertificateItemList.add(plainCertificateItem);
        }

        return plainCertificateItemList;
    }

    public static class PlainCertificateItem {
        public String serialNo;

        public LocalDateTime expireTime;

        public String plainCertificate;

    }
}
