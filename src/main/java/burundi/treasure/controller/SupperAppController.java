package burundi.treasure.controller;

import burundi.treasure.common.Utils;
import burundi.treasure.model.MPSRequest;
import burundi.treasure.model.User;
import burundi.treasure.payload.CallBackSupperAppRequest;
import burundi.treasure.payload.GetPaymentUrlResponse;
import burundi.treasure.payload.VerifyPaymentResponse;
import burundi.treasure.service.MPSService;
import burundi.treasure.service.SupperAppService;
import burundi.treasure.service.UserService;
import jakarta.validation.Valid;
import lombok.extern.log4j.Log4j2;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@Log4j2
@RequestMapping("/api/sp_app")
public class SupperAppController {
    @Autowired
    private Utils utils;

    @Autowired
    private SupperAppService supperAppService;

    @Autowired
    private UserService userService;

    @Autowired
    private MPSService mpsService;

    @PostMapping("/get_payment_register_url")
    public ResponseEntity<?> getPaymentRegisterUrl(@AuthenticationPrincipal UserDetails userDetails, @RequestParam(name = "saToken") String saToken) {
        try {
            log.info(saToken);
            User user = userService.findByPhone(userDetails.getUsername());
            // Trường hợp đăng ký vip bằng ussd. Nếu chưa đăng ký tài khoản sẽ tự động đăng ký
            if(user == null) {
                user = userService.createNewUserWithPhone(userDetails.getUsername(), "SP");
            }

            GetPaymentUrlResponse getPaymentUrlResponse = supperAppService.getPaymentUrl(userDetails.getUsername(), "REGISTER", saToken);
            MPSRequest mpsRequest = mpsService.newMPSRequestSupperApp(user, "PRE_REGISTER_SP", getPaymentUrlResponse.getTransactionId());
            mpsRequest.setUser(user);
            mpsService.save(mpsRequest);

            if("200".equals(getPaymentUrlResponse.getCode())) {
                user.setLastAction("REGISTER_SP");
                userService.saveUser(user);
            }
            return ResponseEntity.ok(getPaymentUrlResponse);
        } catch (Exception e) {
            log.warn("BUGS", e);
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(utils.getResponseFailed());
    }


    @PostMapping("/get_payment_charge_url")
    public ResponseEntity<?> getPaymentChargeUrl(@AuthenticationPrincipal UserDetails userDetails, @RequestParam(name = "saToken") String saToken) {
        try {
            log.info(saToken);
            User user = userService.findByPhone(userDetails.getUsername());
            // Trường hợp đăng ký vip bằng ussd. Nếu chưa đăng ký tài khoản sẽ tự động đăng ký
            if(user == null) {
                user = userService.createNewUserWithPhone(userDetails.getUsername(), "SP");
            }

            GetPaymentUrlResponse getPaymentUrlResponse = supperAppService.getPaymentUrl(userDetails.getUsername(), "CHARGE", saToken);
            MPSRequest mpsRequest = mpsService.newMPSRequestSupperApp(user, "PRE_CHARGE_SP", getPaymentUrlResponse.getTransactionId());
            mpsRequest.setUser(user);
            mpsService.save(mpsRequest);

            if("200".equals(getPaymentUrlResponse.getCode())) {
                user.setLastAction("CHARGE_SP");
                userService.saveUser(user);
            }
            return ResponseEntity.ok(getPaymentUrlResponse);
        } catch (Exception e) {
            log.warn("BUGS", e);
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(utils.getResponseFailed());
    }

    @PostMapping("/cancel")
    public ResponseEntity<?> cancel(@AuthenticationPrincipal UserDetails userDetails, @RequestParam(name = "saToken") String saToken) {
        try {
            log.info(saToken);
            User user = userService.findByPhone(userDetails.getUsername());
            // Trường hợp đăng ký vip bằng ussd. Nếu chưa đăng ký tài khoản sẽ tự động đăng ký
            if(user == null) {
                user = userService.createNewUserWithPhone(userDetails.getUsername(), "SP");
            }

            GetPaymentUrlResponse getPaymentUrlResponse = supperAppService.getPaymentUrl(userDetails.getUsername(), "CANCEL", saToken);
            MPSRequest mpsRequest = mpsService.newMPSRequestSupperApp(user, "CANCEL_SP", getPaymentUrlResponse.getTransactionId());
            mpsRequest.setUser(user);
            mpsService.save(mpsRequest);

            if("200".equals(getPaymentUrlResponse.getCode())) {
                user.setLastAction("CANCEL_SP");
                userService.saveUser(user);
            }
            return ResponseEntity.ok(getPaymentUrlResponse);

        } catch (Exception e) {
            log.warn("BUGS", e);
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(utils.getResponseFailed());
    }

    @PostMapping("/callback_transaction")
    public ResponseEntity<?> verifyPayment(@Valid @RequestBody CallBackSupperAppRequest request) {
        try {
            String bodyDecrypt = supperAppService.decrypt(request.getPartnerToken());
            log.info(bodyDecrypt);
            JSONObject jsonObject = new JSONObject(bodyDecrypt);
            String type = String.valueOf(jsonObject.get("type")); // 1:thanh toán hoặc đăng ký, 2:gia hạn, 3: hủy dịch vụ
            String phone = jsonObject.getString("saUser");
            String transactionId = jsonObject.getString("transaction");
            phone = utils.formatPhoneBurundi(phone);
            User user = userService.findByPhone(phone);
            // Trường hợp đăng ký vip bằng ussd. Nếu chưa đăng ký tài khoản sẽ tự động đăng ký
            if(user == null) {
                user = userService.createNewUserWithPhone(phone, "SP");
            }
            if("1".equals(type)) {

                String action = user.getLastAction();
                MPSRequest mpsRequest = mpsService.newMPSRequestSupperApp(user, action, transactionId);
                mpsRequest.setUser(user);
                String status = jsonObject.get("status").toString(); // 1: Thành công, -1: Thất bại
                if("1".equals(status)) {
                    int amount = (int) jsonObject.getDouble("amount");
                    if("REGISTER_SP".equals(action)) {
                        if(amount > 0) {
                            user.setFirstRegister(true);
                            user.setTotalPlay(user.getTotalPlay() + 5);
                            user.setIsPremiumSupperApp(true);
                            userService.saveUser(user);
                            mpsRequest.setAmount(amount);
                            mpsRequest.setStatus("PROCESSED");
                        } else {
                            if(user.getFirstRegister() == null || !user.getFirstRegister()) {
                                user.setFirstRegister(true);
                                user.setTotalPlay(user.getTotalPlay() + 5);
                            }
                            user.setIsPremiumSupperApp(true);
                            userService.saveUser(user);
                            // Tạo 1 record để làm báo cáo doanh thu
                            mpsRequest.setStatus("PROCESSED");
                        }
                    } else if("CHARGE_SP".equals(action)) {
                        if(amount > 0) {
                            user.setTotalPlay(user.getTotalPlay() + 5);
                            userService.saveUser(user);

                            mpsRequest.setAmount(amount);
                            mpsRequest.setStatus("PROCESSED");
                        }
                    }
                } else {
                    mpsRequest.setStatus("FAILED");
                }

                mpsService.save(mpsRequest);
            } else if("2".equals(type)) {
                MPSRequest mpsRequest = mpsService.newMPSRequestSupperApp(user, "RENEW_SP", transactionId);
                mpsRequest.setUser(user);
                String status = jsonObject.get("status").toString(); // 1: Thành công, -1: Thất bại
                if ("1".equals(status)) {
                    int amount = (int) jsonObject.getDouble("amount");
                    if(amount > 0) {
                        user.setIsPremiumSupperApp(true);
                        user.setTotalPlay(user.getTotalPlay() + 5);
                        userService.saveUser(user);

                        mpsRequest.setAmount(amount);
                        mpsRequest.setStatus("PROCESSED");
                    }
                } else {
                    mpsRequest.setStatus("FAILED");

                    user.setIsPremiumSupperApp(false);
                    userService.saveUser(user);
                }

                mpsService.save(mpsRequest);

            } else if("3".equals(type)) {
                String status = jsonObject.get("status").toString(); // 1: Thành công, -1: Thất bại
                if ("1".equals(status)) {
                    MPSRequest mpsRequest = mpsService.newMPSRequestSupperApp(user, "CANCEL_SP", transactionId);
                    mpsRequest.setStatus("PROCESSED");
                    user.setIsPremiumSupperApp(false);
                    userService.saveUser(user);
                }
            }
            VerifyPaymentResponse verifyPaymentResponse = new VerifyPaymentResponse(200,"Success");
            return ResponseEntity.ok(verifyPaymentResponse);
        } catch (Exception e) {
            log.warn("BUGS", e);
            VerifyPaymentResponse verifyPaymentResponse = new VerifyPaymentResponse(400,"Fail");
            return ResponseEntity.ok(verifyPaymentResponse);
        }
    }

}
