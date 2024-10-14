package com.watergun.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.watergun.entity.ReturnRequest;
import com.watergun.mapper.ReturnRequestMapper;
import com.watergun.service.ReturnRequestService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ReturnRequestServiceImpl extends ServiceImpl<ReturnRequestMapper, ReturnRequest> implements ReturnRequestService {
}
