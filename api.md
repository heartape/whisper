# 接口文档

本文档汇集了即时聊天系统的所有后端 HTTP API 以及 WebSocket 即时通讯接口。

---

## 目录

1. [一、全局说明](#一-全局说明)
2. [二、用户模块 (User API)](#二-用户模块-user-api)
3. [三、会话与群组模块 (Session API)](#三-会话与群组模块-session-api)
4. [四、申请与审核模块 (Apply API)](#四-申请与审核模块-apply-api)
5. [五、消息与公告模块 (Message API)](#五-消息与公告模块-message-api)
6. [六、成员及关系管理模块 (Member API)](#六-成员及关系管理模块-member-api)
7. [七、WebSocket 即时通讯模块](#七-websocket-即时通讯模块)

---

## 一、全局说明

- Base URL: `http://localhost:8080`
- 鉴权方式: 请求头需携带 Token，格式为 `Authorization: Bearer <Token>`（在 WebSocket 连接时也需携带此参数）。
- 通用响应数据结构:
  前端通过判断 `HTTP Status === 200` 且 `code === 0` 来确认业务是否成功，业务数据统一包装在 `data` 字段中。

  ```json
  {
    "code": 0,
    "data": {}
  }
  ```
## 二、用户模块 (User API)
### 1. 账号登录
接口路径: POST /login
接口描述: 用户通过手机号和验证码登录，成功后返回鉴权 Token。
请求参数 (JSON):
code
JSON
{
  "phone": "12345678901",
  "code": "1234"
}
响应 Data:
code
JSON
{
  "Authorization": "Bearer eyJhbGciOiJIUzI1NiJ9..." 
}
### 2. 账号注册
接口路径: POST /register
接口描述: 注册新用户账号。
请求参数 (JSON):
code
JSON
{
  "phone": "12345678903",
  "code": "123456",
  "username": "孤独的浪",
  "password": "your_password",
  "avatar": "https://picsum.photos/seed/u1/200"
}
### 3. 获取当前登录用户信息
接口路径: GET /user
接口描述: 获取当前 Token 对应的用户基本档案。
响应 Data:
code
JSON
{
  "id": 1001,
  "username": "孤独的浪",
  "nickname": "浪哥",
  "avatar": "https://picsum.photos/seed/u1/200",
  "bio": "个人简介"
}
### 4. 获取指定用户信息
接口路径: GET /user/{id}
接口描述: 获取指定用户的公开档案信息。
响应 Data: 结构与 GET /user 完全相同。
### 5. 模糊搜索用户
接口路径: GET /user/find?keyword={keyword}
接口描述: 根据关键字模糊搜索其他用户（用于添加好友）。
响应 Data:
code
Json[
{
    "id": 1002,
    "username": "张三",
    "avatar": "https://...",
    "bio": "热爱生活"
  }
]
### 6. 修改用户username
接口路径: PUT /user/username
接口描述: 修改用户username。
请求参数 (JSON):
code
JSON
{
    "username": "孤独的浪"
}
### 7. 修改用户头像
接口路径: PUT /user/avatar
接口描述: 修改用户头像。
请求参数 (JSON):
code
JSON
{
    "avatar": "https://picsum.photos/seed/u1/200"
}
### 8. 修改用户签名
接口路径: PUT /user/bio
接口描述: 修改用户签名。
请求参数 (JSON):
code
JSON
{
    "bio": "热爱生活"
}
### 9. 修改用户phone
接口路径: PUT /user/phone
接口描述: 修改用户phone。
请求参数 (JSON):
code
JSON
{
    "phone": "12345678901"
}

## 三、会话与群组模块 (Session API)
### 1. 获取当前用户的会话列表
接口路径: GET /session
接口描述: 拉取当前用户参与的所有单聊和群聊会话列表。
响应 Data:
code
Json[
{
    "id": 1,
    "name": "周末约球群",   // 会话名称，若是单聊可能为空或显示对方名称
    "type": "GROUP",      // 会话类型枚举：'GROUP' (群聊), 'PEER' (单聊/私聊)
    "icon": "https://..." // 会话头像
  }
]
### 2. 创建群聊
接口路径: POST /session/group
接口描述: 创建一个新的群组会话。
请求参数 (JSON):
code
JSON
{
  "name": "周末约球群",
  "avatar": "https://..."
}
### 3. 搜索群聊
接口路径: GET /session/find?type=GROUP&keyword={keyword}
接口描述: 根据群名称关键字进行模糊搜索。
响应 Data:
code
Json[
{
    "id": 10,
    "name": "周末约球群",
    "icon": "https://..."
  }
]
### 4. 获取当前用户的联系人列表
接口路径: GET /session/contact
接口描述: 拉取当前用户参与的所有单聊的联系人列表。
响应 Data:
code
Json[
{
"id": 1,
"username": "alan",   // 用户名
"avatar": "https://..." // 头像
}
]
## 四、申请与审核模块 (Apply API)
### 1. 申请添加好友
接口路径: POST /session/peer/apply
接口描述: 向指定用户发起添加好友请求。
请求参数 (JSON):
code
JSON
{
  "reviewerId": 1002,     // 目标用户ID
  "applyInfo": "你好，我是张三", // 申请附言
  "aliasName": "张总"      // （选填）顺便为该好友设置的初始备注名
}
### 2. 申请加入群聊
接口路径: POST /session/group/apply
接口描述: 申请加入指定的群聊。
请求参数 (JSON):
code
JSON
{
  "sessionId": 10,       // 目标群聊ID
  "applyInfo": "请求加入群聊，一起打球" 
}
### 3. 查询收到的申请 (待处理)
接口路径: GET /session/apply/pending
接口描述: 获取当前用户收到的待审核申请记录（包含好友申请和入群申请）。
响应 Data:
code
Json[
{
    "id": 100,
    "type": "FRIEND",       // 申请类型枚举：'FRIEND' 或是 'GROUP'
    "sessionId": 0,         // 若是群聊申请，则带有目标群的 sessionId
    "applicantId": 1005,    // 发起申请人的用户ID
    "applyInfo": "请求添加为好友",
    "status": "PENDING"     // 状态枚举：'PENDING' (待处理)
  }
]
### 4. 查询发出的申请
接口路径: GET /session/apply/sent
接口描述: 获取当前用户主动发出的所有申请记录。
响应 Data: 返回格式与 /session/apply/pending 类似，状态可能为 PENDING、APPROVED、REJECTED。
### 5. 审核/审批请求
接口路径: POST /session/apply/review
接口描述: 同意或拒绝收到的好友请求或入群申请。
请求参数 (JSON):
code
JSON
{
  "applyId": 100,       // 申请记录的唯一ID
  "approved": true,     // 布尔值：true 代表同意，false 代表拒绝
  "reviewNote": "同意",  // 审核附言
  "aliasName": "李四"    // （选填）仅当批准好友申请时，可顺便为对方设置备注名
}
## 五、消息与公告模块 (Message API)
### 1. 拉取历史消息
接口路径: GET /session/messages?count={count}&since={since}&before={before}
接口描述: 加载所有会话的聊天历史记录。
响应 Data:
code
Json[
{
    "id": 10001,
    "sessionId": 1, 
    "userId": 1002,         // 发送者ID (若为 0 代表系统发送的消息)
    "messageType": "TEXT",  // 消息类型枚举：'TEXT' (纯文本), 'SYSTEM_JOIN_GROUP' (系统通知) 等
    "messageInfo": "你好啊"  // 消息正文内容
  }
]
### 2. 拉取历史消息（session）
接口路径: GET /session/{sessionId}/messages
接口描述: 加载指定会话的聊天历史记录。
响应 Data:
code
Json[
{
    "id": 10001,
    "userId": 1002,         // 发送者ID (若为 0 代表系统发送的消息)
    "messageType": "TEXT",  // 消息类型枚举：'TEXT' (纯文本), 'SYSTEM_JOIN_GROUP' (系统通知) 等
    "messageInfo": "你好啊"  // 消息正文内容
  }
]
### 3. 获取群公告
接口路径: GET /session/{sessionId}/announcement
接口描述: 获取指定群聊的历史公告列表。
响应 Data:
code
Json[
{
    "content": "今晚8点准时开黑",
    "userId": 1001,        // 发布此公告的用户ID
    "publishTime": 1690000000000 // 发布时间的时间戳
  }
]
### 4. 发布群公告
接口路径: POST /session/{sessionId}/announcement
接口描述: 发布新的群公告（通常仅限群主或管理员调用）。
请求参数 (JSON):
code
JSON
{
  "content": "今晚8点准时开黑，大家准备好！"
}
## 六、成员及关系管理模块 (Member API)
### 1. 获取会话成员列表
接口路径: GET /session/{sessionId}/members
接口描述: 获取指定群聊的成员列表，或者单聊会话中双方的用户信息（前端常用于建立用户缓存 UserCache）。
响应 Data:
code
Json[
{
    "userId": 1001,
    "username": "张三",
    "aliasName": "三哥",     // 当前登录用户对其设置的备注
    "avatar": "https://...",
    "role": "OWNER",       // 群聊角色枚举: 'OWNER'(群主), 'ADMIN'(管理员), 'MEMBER'(普通成员)
    "isBlock": false,      // 该成员是否被当前登录用户拉黑
    "joinTime": 1690000000000 // 加入会话的时间戳
  }
]
code
### 2. 删除好友 (退出单聊)
接口路径: DELETE /session/exit
接口描述: 删除指定的好友，并退出该单聊会话。
请求参数 (JSON):
code
JSON
{
  "sessionId": 1 
}
### 5. 主动退出群聊
接口路径: POST /session/exit
接口描述: 当前登录用户主动退出指定的群聊。
请求参数 (JSON):
code
JSON
{
  "sessionId": 1 
}
### 6. 私聊成员管理
接口路径: PUT /session/peer/member/manage
接口描述: 对私聊成员执行管理操作。
action: [BLOCK, UNBLOCK]
请求参数 (JSON):
code
JSON
{
"sessionId": 1,
"userId": 1005,
"action": "KICK"
}
### 7. 群成员管理
接口路径: PUT /session/group/member/manage
接口描述: 管理员或群主对群内指定成员执行管理操作。
action: [SET_ADMIN, REMOVE_ADMIN, KICK, BLOCK_AND_KICK, UNBLOCK, MUTE, UNMUTE]
请求参数 (JSON):
code
JSON
{
  "sessionId": 1,
  "userId": 1005,
  "action": "KICK"
}
### 8. 备注
接口路径: PUT /session/alias
接口描述: 备注
请求参数 (JSON):
code
JSON
{
"sessionId": 1,
"userId": 1005,
"aliasName": '张三'
}

响应 Data:
code
Json[
{
"userId": 1001,
"role": "OWNER",
"aliasName": "三哥",     // 当前登录用户对其设置的备注
"avatar": "https://...",
"joinTime": 1690000000000 // 加入会话的时间戳
}
]

## 七、WebSocket 即时通讯模块
系统采用基于 WebSocket 的 STOMP (Simple Text Oriented Messaging Protocol) 协议实现实时消息的双向通信。
### 1. 连接配置
Broker URL: ws://localhost:8080/ws
连接 Header: 握手连接时必须在 Headers 中携带授权信息。
code
JSON
{
  "Authorization": "Bearer <Token>"
}
### 2. 消息订阅通道 (Subscribe)
订阅通道规则: /queue/session
收到的实时消息体格式 (JSON解析后):
code
JSON
{
  "sessionId": 1,           // 会话ID
  "userId": 1002,           // 消息发送者的用户 ID (0 表示系统级事件消息)
  "messageType": "TEXT",    // 消息类型：'TEXT', 'SYSTEM_JOIN_GROUP' 等
  "messageInfo": "Hello!"   // 消息正文内容
}
### 3. 发送消息通道 (Publish)
用户在聊天输入框发送消息时，需向对应后端的管道发布数据流。
发送通道规则: /app/session
发送 Header: 发布消息时需携带鉴权 Authorization: Bearer <Token>。
发送的消息体载荷 (JSON):
code
JSON
{
  "sessionId": 1,
  "messageType": "TEXT",
  "messageInfo": "用户在此处输入的真实聊天文本内容"
}
code