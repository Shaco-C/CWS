package com.watergun.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.watergun.common.R;
import com.watergun.entity.BankAccounts;
import com.watergun.mapper.BankAccountsMapper;
import com.watergun.service.BankAccountsService;
import com.watergun.utils.BankAccountChecker;
import com.watergun.utils.JwtUtil;
import io.micrometer.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@Slf4j
public class BankAccountsServiceImpl extends ServiceImpl<BankAccountsMapper, BankAccounts> implements BankAccountsService {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private BankAccountChecker bankAccountChecker;

    @Override
    @Transactional(readOnly = true)
    public R<Page> getAllBankAccounts(String token, int page, int pageSize) {
        log.info("getAllBankAccounts方法: token: {}", token);

        try {
            // 校验 Token 是否过期
            if (jwtUtil.isTokenExpired(token)) {
                log.warn("getAllBankAccounts方法: Token已过期");
                return R.error("Token已过期，请重新登录");
            }

            // 从 Token 中解析出用户ID
            Long userId = jwtUtil.extractUserId(token);
            log.info("getAllBankAccounts方法: userId: {}", userId);

            if (userId == null) {
                log.warn("getAllBankAccounts方法: 无法解析到用户ID，token可能无效");
                return R.error("用户不存在或Token无效");
            }

            // 查询用户的银行账户
            LambdaQueryWrapper<BankAccounts> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(BankAccounts::getUserId, userId)
                    .orderByDesc(BankAccounts::getCreatedAt);  // 按照创建时间倒序排列

            // 创建分页对象并查询
            Page<BankAccounts> pageInfo = new Page<>(page, pageSize);
            pageInfo = this.page(pageInfo, queryWrapper);

            log.info("getAllBankAccounts方法: 查询成功，返回账户数量: {}", pageInfo.getRecords().size());
            return R.success(pageInfo);

        } catch (Exception e) {
            log.error("getAllBankAccounts方法: 查询银行账户时发生异常", e);
            return R.error("查询失败，请稍后重试");
        }
    }

    @Override
    public R<String> addBankAccount(String token, BankAccounts bankAccounts) {
        log.info("addBankAccount方法: token: {}, bankAccounts: {}", token, bankAccounts);

        // 1. 检查token和bankAccounts是否为空
        if (token == null || bankAccounts == null) {
            log.warn("addBankAccount方法: token或bankAccounts为空");
            return R.error("参数错误");
        }

        try {
            // 2. 验证Token是否过期
            if (jwtUtil.isTokenExpired(token)) {
                log.warn("addBankAccount方法: Token已过期");
                return R.error("Token已过期，请重新登录");
            }

            Long userId = jwtUtil.extractUserId(token);
            if (userId == null) {
                log.warn("addBankAccount方法: 无法解析到用户ID，token可能无效");
                return R.error("用户不存在或Token无效");
            }

            // 3. 检查关键字段是否为空
            if (StringUtils.isEmpty(bankAccounts.getAccountHolderName()) ||
                    StringUtils.isEmpty(bankAccounts.getBankName()) ||
                    StringUtils.isEmpty(bankAccounts.getAccountNumber()) ||
                    StringUtils.isEmpty(bankAccounts.getCurrency())) {
                log.warn("addBankAccount方法: 必填字段缺失");
                return R.error("账户持有人、银行名称、账户号码和货币类型为必填项");
            }
            // 4. 检查账户号码是否合法
            if (!bankAccountChecker.isValidIban(bankAccounts.getIban())){
                log.warn("addBankAccount方法: IBAN格式不正确");
                return R.error("IBAN格式不正确");
            }
            if (!bankAccountChecker.isValidSwiftCode(bankAccounts.getSwiftCode())){
                log.warn("addBankAccount方法: SWIFT代码格式不正确");
                return R.error("SWIFT代码格式不正确");
            }
            if (!bankAccountChecker.isValidAccountNumber(bankAccounts.getAccountNumber())){
                log.warn("addBankAccount方法: 账户号码格式不正确");
                return R.error("账户号码格式不正确");
            }
            // 5. 检查是否已经存在相同账户
            LambdaQueryWrapper<BankAccounts> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(BankAccounts::getUserId, userId)
                    .eq(BankAccounts::getAccountNumber, bankAccounts.getAccountNumber());

            if (this.count(queryWrapper) > 0) {
                log.warn("addBankAccount方法: 该账户已存在");
                return R.error("该银行账户已存在");
            }

            // 6. 设置用户ID
            bankAccounts.setUserId(userId);

            // 7. 保存银行账户信息
            boolean result = this.save(bankAccounts);
            if (!result) {
                log.warn("addBankAccount方法: 添加银行账户失败");
                return R.error("添加失败，请稍后重试");
            }

            log.info("addBankAccount方法: 添加银行账户成功");
            return R.success("银行账户添加成功");

        } catch (Exception e) {
            log.error("addBankAccount方法: 添加银行账户时发生异常", e);
            return R.error("添加失败，请稍后重试");
        }
    }

    @Override
    public R<String> deleteBankAccount(String token, Long bankAccountId) {
        log.info("deleteBankAccount方法: token: {}, bankAccountId: {}", token, bankAccountId);

        try {
            // 校验 token
            if (jwtUtil.isTokenExpired(token)) {
                log.warn("deleteBankAccount方法: Token已过期");
                return R.error("Token已过期，请重新登录");
            }

            // 从 token 中获取用户 ID
            Long userId = jwtUtil.extractUserId(token);
            if (userId == null) {
                log.warn("deleteBankAccount方法: 无法解析到用户ID，token可能无效");
                return R.error("用户不存在或Token无效");
            }

            // 根据 bankAccountId 查找对应的银行账户
            BankAccounts bankAccount = this.getById(bankAccountId);
            if (bankAccount == null || !bankAccount.getUserId().equals(userId)) {
                log.warn("deleteBankAccount方法: 该银行账户不存在或不属于该用户");
                return R.error("无效的银行账户");
            }

            // 执行删除操作
            boolean result = this.removeById(bankAccountId);
            if (!result) {
                log.warn("deleteBankAccount方法: 删除银行账户失败");
                return R.error("删除失败，请稍后重试");
            }

            log.info("deleteBankAccount方法: 删除银行账户成功，bankAccountId: {}", bankAccountId);
            return R.success("删除成功");

        } catch (Exception e) {
            log.error("deleteBankAccount方法: 删除银行账户时发生异常", e);
            return R.error("删除失败，请稍后重试");
        }
    }



}
