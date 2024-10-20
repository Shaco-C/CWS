package com.watergun.utils;


import org.springframework.stereotype.Component;

@Component
public class BankAccountChecker {
    public boolean isValidIban(String iban) {
        // 使用正则表达式或特定逻辑验证IBAN
        String ibanRegex = "[A-Z0-9]{15,34}"; // 示例，具体视业务需求
        return iban.matches(ibanRegex);
    }

    public boolean isValidSwiftCode(String swiftCode) {
        // 使用正则表达式或特定逻辑验证SWIFT代码
        String swiftRegex = "^[A-Z]{4}[A-Z]{2}[A-Z2-9][A-NP-Z0-9](XXX)?$";
        return swiftCode.matches(swiftRegex);
    }

    public boolean isValidAccountNumber(String accountNumber) {
        // 使用正则表达式验证银行账户号码的格式
        // 假设账户号码只包含数字，长度为8到20位
        String accountNumberRegex = "^[0-9]{8,20}$"; // 可根据实际需求调整
        return accountNumber.matches(accountNumberRegex);
    }


}
