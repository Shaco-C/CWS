package com.watergun.controller;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.watergun.common.R;
import com.watergun.service.WithdrawalRecordsService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/withdrawalRecords")
@Slf4j
public class WithdrawalRecordsController {

    private final WithdrawalRecordsService withdrawalRecordsService;


    public WithdrawalRecordsController(WithdrawalRecordsService withdrawalRecordsService) {
        this.withdrawalRecordsService = withdrawalRecordsService;
    }

    //申请提现钱包中的钱
    @PutMapping("/withdraw")
    public R<String> withdraw(HttpServletRequest request, @RequestParam BigDecimal amount, @RequestParam Long bankAccountId){
        String token = request.getHeader("Authorization").replace("Bearer ", "");
        return withdrawalRecordsService.withdrawApplication(token, amount,bankAccountId);
    }

    //查询提现记录
    @GetMapping("/page")
    public R<Page> getWithdrawApplications(@RequestParam(value = "page", defaultValue = "1") int page,
                                           @RequestParam(value = "pageSize", defaultValue = "10") int pageSize,
                                           HttpServletRequest request, String status){
        String token = request.getHeader("Authorization").replace("Bearer ", "");
        return withdrawalRecordsService.getWithdrawApplications(page, pageSize, token, status);
    }
}
