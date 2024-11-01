package com.watergun.controller;


import com.watergun.common.R;
import com.watergun.entity.UserAddress;
import com.watergun.service.UserAddressService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/addresses")
@RestController
public class UserAddressesController {

    private final UserAddressService userAddressService;


    public UserAddressesController(UserAddressService userAddressService) {
        this.userAddressService = userAddressService;
    }

    //用户为自己添加地址

    @PostMapping("/addAddress")
    public R<String> addAddress(HttpServletRequest request,@RequestBody UserAddress userAddress){
        String token = request.getHeader("Authorization").replace("Bearer ", ""); //获取token
        return userAddressService.addAddress(token,userAddress);
    }

    //用户修改自己的地址
    @PutMapping("/updateAddress")
    public R<String> updateAddress(HttpServletRequest request,@RequestBody UserAddress userAddress){
        String token = request.getHeader("Authorization").replace("Bearer ", ""); //获取token
        return userAddressService.updateAddress(token,userAddress);
    }

    //用户删除地址
    @DeleteMapping("/deleteAddress/{id}")
    public R<String> deleteAddress(HttpServletRequest request,@PathVariable Long id){
        String token = request.getHeader("Authorization").replace("Bearer ", ""); //获取token
        return userAddressService.deleteAddress(token,id);
    }

    //用户设置默认地址
    @PutMapping("/setDefaultAddress/{id}")
    public R<String> setDefaultAddress(HttpServletRequest request,@PathVariable Long id){
        String token = request.getHeader("Authorization").replace("Bearer ", ""); //获取token
        return userAddressService.setDefaultAddress(token,id);
    }


    //用户查询自己的所有地址
    @GetMapping("/getAddress/page")
    public R<List<UserAddress>> getAddressList(HttpServletRequest request){
        String token = request.getHeader("Authorization").replace("Bearer ", ""); //获取token
        return userAddressService.getAddressList(token);
    }

}
