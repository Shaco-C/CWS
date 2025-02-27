CWS项目是一套电商系统，目前只有后台管理系统，基于Spring Boot+MyBatisPlus+jwt实现。
### **数据库设计概要**

---

### **1. 用户表 (Users)**

用于存储平台用户的信息。

| 字段名       | 数据类型                          | 描述                               |
| ------------ | --------------------------------- | ---------------------------------- |
| user_id      | BIGINT                            | 用户唯一ID                         |
| username     | VARCHAR(255)                      | 用户名                             |
| email        | VARCHAR(255)                      | 邮箱                               |
| password     | VARCHAR(255)                      | 加密后的密码                       |
| phone_number | VARCHAR(20)                       | 手机号                             |
| role         | ENUM('ADMIN', 'MERCHANT', 'USER') | 用户角色（普通用户、商家、管理员） |
| language     | VARCHAR(10)                       | 用户语言偏好（如 'zh', 'en'）      |
| created_at   | TIMESTAMP                         | 账户创建时间                       |
| updated_at   | TIMESTAMP                         | 账户更新时间                       |
| avatar_url   | VARCHAR(255)                      | 用户头像                           |

---

### **2. 商家表 (Merchants)**

用于存储商家信息，扩展自用户表。

| 字段名          | 数据类型      | 描述                          |
| --------------- | ------------- | ----------------------------- |
| merchant_id     | BIGINT        | 商家ID，引用用户表中的user_id |
| shop_name       | VARCHAR(255)  | 商店名称                      |
| address         | TEXT          | 商家地址                      |
| wallet_balance  | DECIMAL(10,2) | 可提现余额                    |
| pending_balance | DECIMAL(10,2) | 待确认金额                    |
| country         | VARCHAR(255)  | 所在国家                      |
| tax_id          | VARCHAR(255)  | 税号                          |
| payment_info    | TEXT          | 支付和结算信息                |
| created_at      | TIMESTAMP     | 注册时间                      |
| updated_at      | TIMESTAMP     | 信息更新时间                  |
| shop_avatar_url | VARCHAR(255)  | 店铺头像                      |

---

### **3. 商品表 (Products)** 

存储商家上传的商品信息。

| 字段名      | 数据类型                                | 描述                   |
| ----------- | --------------------------------------- | ---------------------- |
| product_id  | BIGINT                                  | 商品唯一ID             |
| merchant_id | BIGINT                                  | 商家ID，引用商家表     |
| name        | VARCHAR(255)                            | 商品名称               |
| description | TEXT                                    | 商品描述，多语言支持   |
| price       | DECIMAL(10,2)                           | 商品价格               |
| stock       | INT                                     | 库存数量               |
| is_active   | Boolean                                 | 商品上下架状态         |
| status      | ENUM('PENDING', 'APPROVED', 'REJECTED') | 商品审核状态           |
| category_id | INT (FK)                                | 商品类别ID，引用类别表 |
| sales       | INT                                     | 商品销量               |
| created_at  | TIMESTAMP                               | 商品上架时间           |
| updated_at  | TIMESTAMP                               | 商品更新时间           |
| image_url   | VARCHAR(255)                            | 商品图片               |


---

### **4. 商品类别表 (Categories)**

管理商品类别和层次结构。

| 字段名      | 数据类型     | 描述               |
| ----------- | ------------ | ------------------ |
| category_id | BIGINT       | 类别唯一ID         |
| name        | VARCHAR(255) | 类别名称           |
| parent_id   | INT (FK)     | 父类别ID，允许NULL |
| created_at  | TIMESTAMP    | 创建时间           |
| updated_at  | TIMESTAMP    | 更新时间           |

---

### **5. 订单表 (Orders)**

存储用户的订单信息。

| 字段名         | 数据类型                                                     | 描述                   |
| -------------- | ------------------------------------------------------------ | ---------------------- |
| order_id       | BIGINT                                                       | 订单唯一ID             |
| user_id        | BIGINT                                                       | 用户ID，引用用户表     |
| merchant_id    | BIGINT                                                       | 商家ID，引用商家表     |
| address_id     | BIGINT                                                       | 地址ID，引用用户地址表 |
| total_amount   | DECIMAL(10,2)                                                | 商品总金额             |
| status         | ENUM('PENDING_PAYMENT', 'PENDING',  'SHIPPED','IN_TRANSIT', 'DELIVERED', 'RECEIVED',  'CANCELLED') | 订单状态               |
| return_status  | ENUM('NOT_REQUESTED', 'REQUESTED', 'APPROVED', 'REJECTED', 'RETURNED') | 商品退货状态           |
| payment_method | VARCHAR(50)                                                  | 支付方式               |
| shipping_info  | TEXT                                                         | 物流信息               |
| tax_amount     | DECIMAL(10,2)                                                | 订单税费               |
| shipping_fee   | DECIMAL(10,2)                                                | 运费                   |
| currency       | VARCHAR(10)                                                  | 订单货币种类           |
| created_at     | TIMESTAMP                                                    | 订单创建时间           |
| updated_at     | TIMESTAMP                                                    | 订单更新时间           |

```mysql
	'PENDING_PAYMENT',  -- 待支付
    'PENDING',          -- 已支付待发货
    'SHIPPED',          -- 已发货
    'IN_TRANSIT',       -- 在途，等待收货
    'DELIVERED',        -- 已送达
    'RECEIVED',         -- 已收货
    'CANCELLED'         -- 已取消

'NOT_REQUESTED',  -- 未请求
'REQUESTED',  -- 请求
'APPROVED',  -- 通过请求
'REJECTED',  -- 拒绝请求
'RETURNED  -- 已退货
```

---

### **6. 订单详情表 (Order_Items)**

用于存储订单中的商品详细信息。

| 字段名        | 数据类型                                                     | 描述               |
| ------------- | ------------------------------------------------------------ | ------------------ |
| order_item_id | BIGINT                                                       | 订单详情唯一ID     |
| order_id      | BIGINT                                                       | 订单ID，引用订单表 |
| product_id    | BIGINT                                                       | 商品ID，引用商品表 |
| return_status | ENUM('*NOT_REQUESTED*', '*REQUESTED*', '*PROCESSED*', '*RETURNED*') | 商品退货状态       |
| quantity      | INT                                                          | 购买数量           |
| price         | DECIMAL(10,2)                                                | 商品单价           |

`NOT_REQUESTED`: 默认状态，未申请退货。

`REQUESTED`: 用户已提交退货请求。

`PROCESSED`: 退货请求已经被处理（包括批准和拒绝的情况）。

`RETURNED`: 退货完成。

---

### **7. 购物车表 (Cart)**

用于存储用户的购物车信息。

| 字段名     | 数据类型  | 描述               |
| ---------- | --------- | ------------------ |
| cart_id    | BIGINT    | 购物车唯一ID       |
| user_id    | BIGINT    | 用户ID，引用用户表 |
| created_at | TIMESTAMP | 购物车创建时间     |
| updated_at | TIMESTAMP | 购物车更新时间     |

---

### **8. 购物车详情表 (Cart_Items)**

存储购物车中用户添加的商品。

| 字段名       | 数据类型 | 描述                   |
| ------------ | -------- | ---------------------- |
| cart_item_id | BIGINT   | 购物车详情唯一ID       |
| cart_id      | BIGINT   | 购物车ID，引用购物车表 |
| product_id   | BIGINT   | 商品ID，引用商品表     |
| quantity     | INT      | 添加的商品数量         |

---

### **9. 评论表 (Reviews)**

存储用户对商品的评论信息。

| 字段名     | 数据类型                                | 描述                            |
| ---------- | --------------------------------------- | ------------------------------- |
| review_id  | BIGINT                                  | 评论唯一ID                      |
| product_id | BIGINT                                  | 商品ID，引用商品表              |
| user_id    | BIGINT                                  | 用户ID，引用用户表              |
| rating     | INT                                     | 评分（1-5）                     |
| comment    | TEXT                                    | 评论内容                        |
| status     | ENUM('PENDING', 'APPROVED', 'REJECTED') | 评论审核状态（AI审核/人工复核） |
| created_at | TIMESTAMP                               | 评论创建时间                    |
| updated_at | TIMESTAMP                               | 评论更新时间                    |

---

### **10. AI审核日志表 (AI_Review_Logs)**

记录AI审核用户评论的详细信息。

| 字段名     | 数据类型                         | 描述               |
| ---------- | -------------------------------- | ------------------ |
| log_id     | BIGINT                           | 审核日志唯一ID     |
| review_id  | BIGINT                           | 评论ID，引用评论表 |
| result     | ENUM('*APPROVED*', '*REJECTED*') | AI审核结果         |
| created_at | TIMESTAMP                        | 审核时间           |

---

这个数据库设计涵盖了用户、商家、商品、订单等核心功能，同时集成了AI审核功能。你可以根据实际项目需求对其进行扩展或优化。



### **11. 收藏表 (Favorites)**

存储用户收藏的商品关系。

| 字段名      | 数据类型  | 描述               |
| ----------- | --------- | ------------------ |
| favorite_id | BIGINT    | 收藏记录唯一ID     |
| user_id     | BIGINT    | 用户ID，引用用户表 |
| product_id  | BIGINT    | 商品ID，引用商品表 |
| created_at  | TIMESTAMP | 收藏创建时间       |

---

### **12. 商家申请表 (Merchant_Applications)**

存储用户申请成为商家的信息。

| 字段名          | 数据类型                                      | 描述                       |
| --------------- | :-------------------------------------------- | -------------------------- |
| application_id  | BIGINT                                        | 申请记录的唯一ID           |
| user_id         | BIGINT                                        | 申请者的用户ID，引用用户表 |
| shop_name       | VARCHAR(255)                                  | 商店名称                   |
| address         | TEXT                                          | 商家地址                   |
| country         | VARCHAR(255)                                  | 所在国家                   |
| tax_id          | VARCHAR(255)                                  | 税号                       |
| payment_info    | TEXT                                          | 支付和结算信息             |
| shop_avatar_url | VARCHAR(255)                                  | 店铺头像                   |
| status          | ENUM('*PENDING*', '*APPROVED*', '*REJECTED*') | 申请状态，初始为`pending`  |
| created_at      | TIMESTAMP                                     | 申请提交时间               |
| updated_at      | TIMESTAMP                                     | 申请更新时间               |

---

### 13.**提现记录表 (Withdrawal_Records)**

用于存储商家的提现操作记录。

| 字段名          | 数据类型                                              | 描述                              |
| --------------- | ----------------------------------------------------- | --------------------------------- |
| withdrawal_id   | BIGINT NOT NULL                                       | 提现记录ID，主键                  |
| merchant_id     | BIGINT NOT NULL                                       | 商家ID，关联merchants表           |
| amount          | DECIMAL(10,2) NOT NULL                                | 提现金额                          |
| status          | ENUM NOT NULL（PENDING, COMPLETED, FAILED,CANCELLED） | 提现状态                          |
| transaction_id  | VARCHAR(100)                                          | 第三方交易ID                      |
| bank_account_id | BIGINT NOT NULL                                       | 银行账户ID，关联bank_accounts表   |
| currency        | VARCHAR(10) NOT NULL                                  | 提现的货币种类（如USD, EUR, CNY） |
| request_time    | DATETIME NOT NULL                                     | 提现申请时间                      |
| completion_time | DATETIME                                              | 提现完成时间                      |
| failure_reason  | VARCHAR(255)                                          | 提现失败原因                      |
| created_at      | DATETIME                                              | 记录创建时间                      |
| updated_at      | DATETIME                                              | 记录更新时间                      |

---

###  **14.待确认金额日志表 (Pending_Amount_Log)**

用于记录商家待确认金额的每次变更情况，确保资金流向清晰，便于审计和排查。

| 字段名                | 数据类型               | 描述                                     |
| --------------------- | ---------------------- | ---------------------------------------- |
| pending_amount_log_id | BIGINT NOT NULL        | 日志ID，主键                             |
| merchant_id           | BIGINT NOT NULL        | 商家ID，关联`merchants`表                |
| amount                | DECIMAL(10,2) NOT NULL | 变动的金额（正数表示增加，负数表示减少） |
| currency              | VARCHAR(10) NOT NULL   | 货币种类（如USD, EUR, CNY）              |
| description           | VARCHAR(255)           | 变更描述，提供额外说明                   |
| created_at            | DATETIME               | 记录创建时间                             |
| updated_at            | DATETIME               | 记录更新时间                             |

---

### 15.钱包余额日志表 (Wallet_Balance_Log)

用于记录商家的钱包余额的每次变动。

| 字段名                | 数据类型                                    | 描述                                     |
| --------------------- | ------------------------------------------- | ---------------------------------------- |
| wallet_balance_log_id | BIGINT NOT NULL                             | 日志ID，主键                             |
| merchant_id           | BIGINT NOT NULL                             | 商家ID，关联`merchants`表                |
| order_id              | BIGINT                                      | 订单ID，关联`orders`表                   |
| amount_change         | DECIMAL(10,2) NOT NULL                      | 变动的金额（正数表示增加，负数表示减少） |
| new_balance           | DECIMAL(10,2) NOT NULL                      | 变动后的余额                             |
| currency              | VARCHAR(10) NOT NULL                        | 货币种类（如USD, EUR, CNY）              |
| description           | VARCHAR(255)                                | 变更描述，说明变动原因                   |
| created_at            | DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP | 日志创建时间                             |

---

### 16.**银行账户表 (Bank_Accounts)**

用于存储商家或用户的银行账户信息。

| 字段名              | 数据类型              | 描述                              |
| ------------------- | --------------------- | --------------------------------- |
| bank_account_id     | BIGINT NOT NULL       | 银行账户ID，主键                  |
| user_id             | BIGINT NOT NULL       | 账户所属用户或商家ID，关联users表 |
| account_holder_name | VARCHAR(255) NOT NULL | 账户持有人姓名                    |
| bank_name           | VARCHAR(255) NOT NULL | 银行名称                          |
| account_number      | VARCHAR(50)           | 银行账户号码                      |
| iban                | VARCHAR(34)           | 国际银行账号（IBAN）              |
| swift_code          | VARCHAR(11)           | 银行国际代码（SWIFT/BIC）         |
| currency            | VARCHAR(10) NOT NULL  | 账户货币种类（如USD, EUR, CNY）   |
| created_at          | DATETIME              | 账户创建时间                      |
| updated_at          | DATETIME              | 账户更新时间                      |

---

### 17.**用户地址表 (User_Addresses)**

| 字段名           | 数据类型     | 描述               |
| ---------------- | ------------ | ------------------ |
| address_id       | BIGINT       | 地址唯一ID         |
| user_id          | BIGINT       | 用户ID，引用用户表 |
| recipient_name   | VARCHAR(100) | 收货人姓名         |
| phone_code       | VARCHAR(10)  | 区号               |
| phone_number     | VARCHAR(15)  | 收货人电话         |
| country          | VARCHAR(100) | 国家               |
| state            | VARCHAR(100) | 省/州              |
| city             | VARCHAR(100) | 城市               |
| detailed_address | VARCHAR(100) | 详细地址           |
| is_default       | BOOLEAN      | 是否为默认地址     |
| created_at       | TIMESTAMP    | 地址添加时间       |
| updated_at       | TIMESTAMP    | 地址更新时间       |


--- 
