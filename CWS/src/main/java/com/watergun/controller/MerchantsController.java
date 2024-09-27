package com.watergun.controller;

import com.watergun.common.R;
import com.watergun.entity.Merchants;
import com.watergun.service.MerchantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/merchants")
public class MerchantsController {

    @Autowired
    private MerchantService merchantService;

    @PostMapping
    public R<String> createMerchant(@RequestBody Merchants merchant) {

        return R.success("Merchant created successfully");
    }

    @PutMapping("/{id}")
    public R<String> updateMerchant(@PathVariable Integer id, @RequestBody Merchants merchant) {

        return R.success("Merchant updated successfully");
    }

    @DeleteMapping("/{id}")
    public R<String> deleteMerchant(@PathVariable Integer id) {

        return R.success("Merchant deleted successfully");
    }
}
