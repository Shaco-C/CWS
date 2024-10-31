package com.watergun.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.watergun.common.R;
import com.watergun.entity.UserAddress;

import java.util.List;

public interface UserAddressService extends IService<UserAddress>  {

    R<String> addAddress(String token, UserAddress userAddress);

    R<String> updateAddress(String token,UserAddress userAddress);

    R<String> deleteAddress(String token,Long addressId);

    R<String> setDefaultAddress(String token,Long id);

    R<List<UserAddress>> getAddressList(String token);

}
