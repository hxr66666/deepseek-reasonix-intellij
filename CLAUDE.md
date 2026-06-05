# idea 插件

bundledPlugin("com.intellij.java")
bundledPlugin("org.jetbrains.plugins.yaml")
bundledPlugin("org.intellij.plugins.markdown")

# DeepSeek-Reasonix HTTP API 文档

## 概述

本文档描述了 DeepSeek-Reasonix 的 HTTP API 接口规范，该接口用于通过 HTTP 协议与控制层（`control.Controller`）进行交互，提供事件流推送和命令执行功能。

---

## 基础信息

| 属性 | 值 |
|------|-----|
| **服务名称** | DeepSeek-Reasonix HTTP Server |
| **认证方式** | 无认证（仅绑定 localhost，依赖同源策略保护） |
| **CORS 支持** | 默认关闭，可通过 `HandlerWithCORS(origin)` 启用 |
| **CSRF 防护** | POST 请求强制要求 `Content-Type: application/json` |
| **事件流** | Server-Sent Events (SSE) |

---

## 数据类型定义

### wireEvent（SSE 事件类型）

```json
{
  "kind": "turn_started",
  "text": "文本内容",
  "reasoning": "推理内容",
  "level": "info",
  "tool": {...},
  "usage": {...},
  "approval": {...},
  "ask": {...},
  "compaction": {...},
  "err": "错误信息"
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `kind` | string | 是 | 事件类型 |
| `text` | string | 否 | 文本内容 |
| `reasoning` | string | 否 | 推理内容 |
| `level` | string | 否 | 通知级别：`info` / `warn` |
| `tool` | wireTool | 否 | 工具调用信息 |
| `usage` | wireUsage | 否 | Token 使用统计 |
| `approval` | wireApproval | 否 | 批准请求信息 |
| `ask` | wireAsk | 否 | 提问请求信息 |
| `compaction` | wireCompaction | 否 | 压缩操作信息 |
| `err` | string | 否 | 错误信息 |

**事件类型列表**：

| 事件类型 | 说明 |
|----------|------|
| `turn_started` | 回合开始 |
| `reasoning` | 推理过程 |
| `text` | 文本输出 |
| `message` | 消息 |
| `tool_dispatch` | 工具调度 |
| `tool_result` | 工具结果 |
| `usage` | 使用统计 |
| `notice` | 通知 |
| `phase` | 阶段 |
| `approval_request` | 批准请求 |
| `ask_request` | 提问请求 |
| `turn_done` | 回合结束 |
| `compaction_started` | 压缩开始 |
| `compaction_done` | 压缩完成 |
| `tool_progress` | 工具进度 |

### wireTool（工具调用）

```json
{
  "id": "tool-123",
  "name": "python",
  "args": "{\"code\": \"print(1)\"}",
  "output": "1",
  "err": "",
  "readOnly": false,
  "truncated": false,
  "partial": false,
  "parentId": ""
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `id` | string | 否 | 工具调用 ID |
| `name` | string | 是 | 工具名称 |
| `args` | string | 否 | 参数（JSON 字符串） |
| `output` | string | 否 | 工具输出 |
| `err` | string | 否 | 错误信息 |
| `readOnly` | bool | 是 | 是否只读操作 |
| `truncated` | bool | 否 | 输出是否被截断 |
| `partial` | bool | 否 | 是否部分输出 |
| `parentId` | string | 否 | 父工具调用 ID |

### wireUsage（Token 使用统计）

```json
{
  "promptTokens": 100,
  "completionTokens": 50,
  "totalTokens": 150,
  "cacheHitTokens": 20,
  "cacheMissTokens": 80,
  "reasoningTokens": 30,
  "sessionCacheHitTokens": 100,
  "sessionCacheMissTokens": 500,
  "costUsd": 0.001
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `promptTokens` | int | 是 | 提示词 Token 数 |
| `completionTokens` | int | 是 | 完成 Token 数 |
| `totalTokens` | int | 是 | 总 Token 数 |
| `cacheHitTokens` | int | 是 | 缓存命中 Token 数 |
| `cacheMissTokens` | int | 是 | 缓存未命中 Token 数 |
| `reasoningTokens` | int | 否 | 推理 Token 数 |
| `sessionCacheHitTokens` | int | 是 | 会话累计缓存命中 |
| `sessionCacheMissTokens` | int | 是 | 会话累计缓存未命中 |
| `costUsd` | float | 否 | 费用（美元） |

### wireApproval（批准请求）

```json
{
  "id": "approval-123",
  "tool": "python",
  "subject": "执行代码"
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `id` | string | 是 | 批准请求 ID |
| `tool` | string | 是 | 工具名称 |
| `subject` | string | 是 | 请求主题 |

### wireAsk（提问请求）

```json
{
  "id": "ask-123",
  "questions": [
    {
      "id": "q1",
      "header": "选择选项",
      "prompt": "请选择一个选项",
      "options": [
        {"label": "选项1", "description": "描述1"},
        {"label": "选项2", "description": "描述2"}
      ],
      "multi": false
    }
  ]
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `id` | string | 是 | 提问请求 ID |
| `questions` | array | 是 | 问题列表 |

#### wireAskQuestion（问题）

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `id` | string | 是 | 问题 ID |
| `header` | string | 否 | 问题标题 |
| `prompt` | string | 是 | 问题内容 |
| `options` | array | 是 | 选项列表 |
| `multi` | bool | 否 | 是否允许多选 |

#### wireAskOption（选项）

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `label` | string | 是 | 选项标签 |
| `description` | string | 否 | 选项描述 |

### wireCompaction（压缩操作）

```json
{
  "trigger": "auto",
  "messages": 10,
  "summary": "摘要内容",
  "archive": "存档内容"
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `trigger` | string | 否 | 触发方式 |
| `messages` | int | 否 | 消息数量 |
| `summary` | string | 否 | 摘要内容 |
| `archive` | string | 否 | 存档内容 |

### AskAnswer（回答）

```json
{
  "questionId": "q1",
  "selected": ["选项1", "选项2"]
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `questionId` | string | 是 | 问题 ID |
| `selected` | array | 是 | 选中的选项列表 |

---

## 端点总览

### GET 端点

| 端点 | 功能 |
|------|------|
| `GET /` | 返回浏览器客户端首页 |
| `GET /events` | SSE 事件流 |
| `GET /history` | 获取会话消息历史 |
| `GET /context` | 获取上下文窗口使用统计 |
| `GET /checkpoints` | 获取检查点列表 |
| `GET /branches` | 获取分支列表和树形结构 |
| `GET /status` | 获取会话状态快照 |
| `GET /sessions` | 获取已保存会话列表 |
| `GET /skills` | 获取可发现技能列表 |

### POST 端点

| 端点 | 功能 |
|------|------|
| `POST /submit` | 提交用户输入 |
| `POST /cancel` | 取消当前任务 |
| `POST /approve` | 批准工具调用 |
| `POST /plan` | 切换计划模式 |
| `POST /compact` | 压缩会话历史 |
| `POST /new` | 创建新会话 |
| `POST /rewind` | 回退到检查点 |
| `POST /fork` | 创建分支 |
| `POST /summarize` | 执行摘要 |
| `POST /bypass` | 切换绕过模式 |
| `POST /answer` | 回答提问 |
| `POST /resume` | 恢复会话 |
| `POST /forget` | 删除记忆 |

---

## 端点详细说明

### 1. GET /

**功能**：返回浏览器客户端首页

**响应**：
- 状态码：200 OK
- Content-Type: `text/html; charset=utf-8`
- 内容：嵌入式的 index.html

---

### 2. GET /events

**功能**：实时事件流，推送控制器产生的所有事件

**响应**：
- 状态码：200 OK
- Content-Type: `text/event-stream`
- Cache-Control: `no-cache`
- Connection: `keep-alive`

**SSE 格式**：
```
data: {"kind": "turn_started", "text": "..."}

data: {"kind": "tool_dispatch", "tool": {...}}

data: {"kind": "turn_done"}
```

**事件类型**：参考「数据类型定义」中的「事件类型列表」

---

### 3. GET /history

**功能**：获取当前会话的消息历史

**请求体**：无

**响应**：
- 状态码：200 OK
- Content-Type: `application/json`

**响应结构**：
```json
[
  {
    "role": "user",
    "content": "用户输入的内容"
  },
  {
    "role": "assistant",
    "content": "助手回复的内容"
  }
]
```

| 字段 | 类型 | 说明 |
|------|------|------|
| `role` | string | 角色：`user` / `assistant` / `system` |
| `content` | string | 消息内容 |

---

### 4. GET /context

**功能**：获取上下文窗口使用情况统计

**请求体**：无

**响应**：
- 状态码：200 OK
- Content-Type: `application/json`

**响应结构**：
```json
{
  "used": 1234,
  "window": 8192
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| `used` | int | 当前已使用的 tokens 数 |
| `window` | int | 上下文窗口总容量 |

---

### 5. GET /checkpoints

**功能**：获取会话的检查点列表，用于回退操作

**请求体**：无

**响应**：
- 状态码：200 OK
- Content-Type: `application/json`

**响应结构**：
```json
[
  {
    "turn": 0,
    "prompt": "第一个问题",
    "files": 2
  },
  {
    "turn": 1,
    "prompt": "第二个问题",
    "files": 3
  }
]
```

| 字段 | 类型 | 说明 |
|------|------|------|
| `turn` | int | 回合编号 |
| `prompt` | string | 该回合的用户输入 |
| `files` | int | 该回合涉及的文件数量 |

---

### 6. GET /branches

**功能**：获取会话分支列表和树形结构文本

**请求体**：无

**响应**：
- 状态码：200 OK
- Content-Type: `application/json`

**响应结构**：
```json
{
  "branches": ["main", "feature-x"],
  "tree": "main └── feature-x"
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| `branches` | array | 分支名称列表 |
| `tree` | string | 分支树形结构文本表示 |

---

### 7. GET /status

**功能**：获取会话的综合状态快照

**请求体**：无

**响应**：
- 状态码：200 OK
- Content-Type: `application/json`

**响应结构**：
```json
{
  "label": "deepseek-chat",
  "running": false,
  "plan": false,
  "bypass": false,
  "cwd": "/workspace/my-project",
  "used": 1500,
  "window": 8192,
  "cacheHit": 10,
  "cacheMiss": 3,
  "lastUsage": {
    "promptTokens": 100,
    "completionTokens": 50,
    "totalTokens": 150,
    "cacheHitTokens": 20,
    "cacheMissTokens": 80,
    "reasoningTokens": 30,
    "sessionCacheHitTokens": 100,
    "sessionCacheMissTokens": 500,
    "costUsd": 0.001
  },
  "balance": {
    "available": true,
    "infos": [
      {
        "currency": "USD",
        "amount": 100.50
      }
    ]
  },
  "jobs": [
    {
      "id": "job-123",
      "kind": "bash",
      "label": "运行脚本"
    }
  ]
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| `label` | string | 当前模型标签 |
| `running` | bool | 是否正在运行 |
| `plan` | bool | 是否开启计划模式 |
| `bypass` | bool | 是否开启绕过模式 |
| `cwd` | string | 当前工作目录 |
| `used` | int | 已使用 tokens |
| `window` | int | 窗口容量 |
| `cacheHit` | int | 缓存命中次数 |
| `cacheMiss` | int | 缓存未命中次数 |
| `lastUsage` | wireUsage | 最后使用记录（可选） |
| `balance` | object | 余额信息（可选） |
| `jobs` | array | 任务列表（可选） |

#### balance 对象结构

| 字段 | 类型 | 说明 |
|------|------|------|
| `available` | bool | 账户是否可用 |
| `infos` | array | 货币余额列表 |

#### balance.infos 元素结构

| 字段 | 类型 | 说明 |
|------|------|------|
| `currency` | string | 货币类型 |
| `amount` | float | 余额金额 |

#### jobs 元素结构

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | string | 任务 ID |
| `kind` | string | 任务类型：`bash` / `task` |
| `label` | string | 任务标签 |

---

### 8. GET /sessions

**功能**：获取已保存的会话文件列表

**请求体**：无

**响应**：
- 状态码：200 OK
- Content-Type: `application/json`

**响应结构**：
```json
[
  {
    "name": "session-20240101",
    "path": "/data/sessions/session-20240101.jsonl",
    "title": "代码审查任务",
    "turns": 15,
    "current": false
  }
]
```

| 字段 | 类型 | 说明 |
|------|------|------|
| `name` | string | 会话名称 |
| `path` | string | 文件路径 |
| `title` | string | LLM 生成的标题（可选） |
| `turns` | int | 回合数 |
| `current` | bool | 是否为当前会话 |

---

### 9. GET /skills

**功能**：获取可发现的技能列表

**请求体**：无

**响应**：
- 状态码：200 OK
- Content-Type: `application/json`

**响应结构**：
```json
[
  {
    "name": "python",
    "scope": "file",
    "subagent": false,
    "description": "Execute Python code"
  }
]
```

| 字段 | 类型 | 说明 |
|------|------|------|
| `name` | string | 技能名称 |
| `scope` | string | 作用范围 |
| `subagent` | bool | 是否为子代理 |
| `description` | string | 技能描述 |

---

### 10. POST /submit

**功能**：提交用户输入，支持特殊命令

**请求体**：
```json
{
  "input": "分析这段代码的性能问题"
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `input` | string | 是 | 用户输入内容 |

**特殊命令**：
- `/model <ref>` - 切换模型，如 `{"input": "/model deepseek-chat"}`
- `/effort <level>` - 切换推理级别，如 `{"input": "/effort high"}`

**成功响应**：
- 状态码：202 Accepted（正常提交）/ 204 No Content（命令执行成功）
- 响应体：无

**失败响应**：
- 状态码：400 Bad Request（缺少 input）
- 响应体：
```json
{"error": "missing input"}
```

- 状态码：500 Internal Server Error（切换模型失败）
- 响应体：
```json
{"error": "cannot switch model while a turn is running"}
```

---

### 11. POST /cancel

**功能**：取消当前正在运行的任务

**请求体**：无

**成功响应**：
- 状态码：204 No Content
- 响应体：无

---

### 12. POST /approve

**功能**：批准工具调用请求

**请求体**：
```json
{
  "id": "request-123",
  "allow": true,
  "session": false,
  "persist": false
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `id` | string | 是 | 请求 ID |
| `allow` | bool | 是 | 是否允许执行 |
| `session` | bool | 否 | 是否为会话级批准 |
| `persist` | bool | 否 | 是否持久化批准 |

**成功响应**：
- 状态码：204 No Content
- 响应体：无

**失败响应**：
- 状态码：400 Bad Request（缺少 id）
- 响应体：
```json
{"error": "missing id"}
```

---

### 13. POST /plan

**功能**：切换计划模式

**请求体**：
```json
{
  "on": true
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `on` | bool | 是 | 是否开启计划模式 |

**成功响应**：
- 状态码：204 No Content
- 响应体：无

**失败响应**：
- 状态码：400 Bad Request（请求体格式错误）
- 响应体：
```json
{"error": "bad body"}
```

---

### 14. POST /compact

**功能**：压缩会话历史（保留关键信息，减少上下文长度）

**请求体**：无

**成功响应**：
- 状态码：204 No Content
- 响应体：无

**失败响应**：
- 状态码：500 Internal Server Error（压缩失败）
- 响应体：
```json
{"error": "compaction failed"}
```

---

### 15. POST /new

**功能**：创建新会话

**请求体**：无

**成功响应**：
- 状态码：204 No Content
- 响应体：无

**失败响应**：
- 状态码：500 Internal Server Error（创建失败）
- 响应体：
```json
{"error": "failed to create new session"}
```

---

### 16. POST /rewind

**功能**：回退到指定检查点

**请求体**：
```json
{
  "turn": 5,
  "scope": "both"
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `turn` | int | 是 | 目标回合编号（>= 0） |
| `scope` | string | 否 | 回退范围：`code`/`conversation`/`both`（默认 `both`） |

**成功响应**：
- 状态码：204 No Content
- 响应体：无

**失败响应**：
- 状态码：400 Bad Request（缺少 turn）
- 响应体：
```json
{"error": "missing turn"}
```

- 状态码：500 Internal Server Error（回退失败）
- 响应体：
```json
{"error": "rewind failed"}
```

---

### 17. POST /fork

**功能**：从指定检查点创建新分支

**请求体**：
```json
{
  "turn": 3,
  "name": "feature-branch"
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `turn` | int | 是 | 源回合编号（>= 0） |
| `name` | string | 否 | 分支名称 |

**成功响应**：
- 状态码：200 OK
- 响应体：
```json
{
  "path": "/data/sessions/feature-branch.jsonl"
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| `path` | string | 新分支文件路径 |

**失败响应**：
- 状态码：400 Bad Request（缺少 turn）
- 响应体：
```json
{"error": "missing turn"}
```

- 状态码：500 Internal Server Error（创建失败）
- 响应体：
```json
{"error": "fork failed"}
```

---

### 18. POST /summarize

**功能**：对会话进行摘要处理

**请求体**：
```json
{
  "turn": 2,
  "mode": "from"
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `turn` | int | 是 | 目标回合编号（>= 0） |
| `mode` | string | 是 | 摘要模式：`from`（从此处开始）/ `upto`（到此为止） |

**成功响应**：
- 状态码：204 No Content
- 响应体：无

**失败响应**：
- 状态码：400 Bad Request（参数错误）
- 响应体：
```json
{"error": "mode must be 'from' or 'upto'"}
```

- 状态码：500 Internal Server Error（摘要失败）
- 响应体：
```json
{"error": "summarize failed"}
```

---

### 19. POST /bypass

**功能**：切换 YOLO/绕过模式（自动批准所有工具调用）

**请求体**：
```json
{
  "on": true
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `on` | bool | 是 | 是否开启绕过模式 |

**成功响应**：
- 状态码：204 No Content
- 响应体：无

**失败响应**：
- 状态码：400 Bad Request（请求体格式错误）
- 响应体：
```json
{"error": "bad body"}
```

---

### 20. POST /answer

**功能**：回答系统提出的问题

**请求体**：
```json
{
  "id": "ask-123",
  "answers": [
    {
      "questionId": "q1",
      "selected": ["选项1"]
    },
    {
      "questionId": "q2",
      "selected": ["选项A", "选项B"]
    }
  ]
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `id` | string | 是 | 提问请求 ID |
| `answers` | array | 是 | 答案数组（AskAnswer 类型） |

#### answers 元素结构（AskAnswer）

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `questionId` | string | 是 | 问题 ID |
| `selected` | array | 是 | 选中的选项标签列表 |

**成功响应**：
- 状态码：204 No Content
- 响应体：无

**失败响应**：
- 状态码：400 Bad Request（缺少 id）
- 响应体：
```json
{"error": "missing id"}
```

---

### 21. POST /resume

**功能**：从 JSONL 文件恢复会话

**请求体**：
```json
{
  "path": "/data/sessions/backup.jsonl"
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `path` | string | 是 | 会话文件路径 |

**成功响应**：
- 状态码：204 No Content
- 响应体：无

**失败响应**：
- 状态码：400 Bad Request（参数错误或文件不存在）
- 响应体：
```json
{"error": "load session: file not found"}
```

---

### 22. POST /forget

**功能**：删除已保存的记忆

**请求体**：
```json
{
  "name": "memory-key"
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `name` | string | 是 | 记忆名称 |

**成功响应**：
- 状态码：204 No Content
- 响应体：无

**失败响应**：
- 状态码：400 Bad Request（缺少 name）
- 响应体：
```json
{"error": "missing name"}
```

- 状态码：500 Internal Server Error（删除失败）
- 响应体：
```json
{"error": "forget memory failed"}
```

---

## UI 交互流程与布局

### 整体布局结构

```
┌─────────────────────────────────────────────────────────────┐
│                         App                                │
│  ┌──────────────┬──────────────────────────────────────┐   │
│  │   Sidebar    │              Transcript              │   │
│  │  ┌────────┐  │                                      │   │
│  │  │ Brand  │  │  ┌────────────────────────────────┐  │   │
│  │  └────────┘  │  │      Welcome Screen            │  │   │
│  │  ┌────────┐  │  │  - Logo, Title, Tagline        │  │   │
│  │  │ Nav    │  │  │  - Keyboard Hints              │  │   │
│  │  │ Items  │  │  │  - Example Prompts             │  │   │
│  │  └────────┘  │  └────────────────────────────────┘  │   │
│  │  ┌────────┐  │                                      │   │
│  │  │Session │  │  ┌────────────────────────────────┐  │   │
│  │  │  List  │  │  │         Messages               │  │   │
│  │  └────────┘  │  │  - User Message               │  │   │
│  │  ┌────────┐  │  │  - Assistant Message          │  │   │
│  │  │ Status │  │  │  - Tool Cards                 │  │   │
│  │  │Metrics │  │  │  - Approval Requests          │  │   │
│  │  └────────┘  │  │  - Ask Cards                  │  │   │
│  └──────────────┘  │  └────────────────────────────────┘  │   │
│                    │                                      │   │
│  ┌─────────────────┴────────────────────────────────────┐ │   │
│  │                     Footer                            │ │   │
│  │  ┌────────────────────────────────────────────────┐  │ │   │
│  │  │ Toolbar: Auto / Plan / YOLO / Status          │  │ │   │
│  │  └────────────────────────────────────────────────┘  │ │   │
│  │  ┌────────────────────────────────────────────────┐  │ │   │
│  │  │ Composer: Input + Send Button + Stop Button    │  │ │   │
│  │  └────────────────────────────────────────────────┘  │ │   │
│  └─────────────────────────────────────────────────────┘ │   │
└─────────────────────────────────────────────────────────────┘
```

### 布局区域说明

| 区域 | 类名 | 功能说明 |
|------|------|----------|
| **侧边栏** | `sidebar` | 会话管理、导航、状态监控 |
| **会话记录** | `transcript` | 消息展示、工具调用、批准请求 |
| **底部栏** | `footer` | 模式切换、输入框、状态显示 |

#### 侧边栏结构

```
sidebar
├── sidebar__brand      # 品牌标识（Logo + Name）
├── sidebar__nav        # 导航菜单
│   ├── New Session     # 创建新会话
│   ├── Compact         # 压缩会话
│   ├── Rewind          # 回退功能
│   └── Branches        # 分支管理
├── session-list        # 会话列表
└── sidebar__section    # 状态区域
    ├── ctx-bar         # 上下文窗口进度条
    ├── status-metrics  # 指标：Cache / Cost / Balance
    └── status          # 连接状态 + 模型名称
```

#### 会话记录区域组件

| 组件 | 类名 | 功能 |
|------|------|------|
| 用户消息 | `msg msg--user` | 显示用户输入 |
| 助手消息 | `msg msg--assistant` | 显示助手回复 |
| 工具卡片 | `card` | 工具调用详情（调度/结果/进度） |
| 批准请求 | `approval` | 待批准的工具调用 |
| 提问卡片 | `ask` | 交互式问题选择 |
| 压缩提示 | `compaction` | 会话压缩摘要 |
| 阶段提示 | `phase` | 当前处理阶段 |
| 通知 | `notice` | 系统通知消息 |

#### 底部栏结构

```
footer
├── toolbar                 # 工具栏
│   ├── btn-auto            # 自动模式（默认）
│   ├── btn-plan            # 计划模式（只读）
│   ├── btn-bypass          # YOLO模式（自动批准）
│   ├── status              # 连接状态指示器
│   ├── turn-info           # 当前回合耗时/Tokens
│   └── balance-info        # 余额信息
└── composer                # 输入框组件
    ├── composer__input     # 文本输入区
    ├── composer__btn--send # 发送按钮
    └── composer__btn--stop # 停止按钮
```

### 核心交互流程

#### 1. 消息发送流程

```
用户输入 → 按 Enter 或点击 Send → POST /submit → SSE 事件推送
                                               ↓
                           ┌──────────────────────────────┐
                           │  turn_started               │
                           │  reasoning (推理过程)       │
                           │  text / message (回复内容)   │
                           │  tool_dispatch (工具调度)    │
                           │  tool_result (工具结果)      │
                           │  turn_done                  │
                           └──────────────────────────────┘
```

#### 2. 工具调用流程

```
SSE: tool_dispatch → 渲染工具卡片(执行中)
                         ↓
SSE: tool_progress → 更新卡片内容(实时输出)
                         ↓
SSE: tool_result → 更新卡片状态(成功/失败)
```

#### 3. 批准请求流程

```
SSE: approval_request → 显示批准卡片
                           ↓
用户点击 Allow/Deny → POST /approve
                           ↓
工具继续执行或取消
```

#### 4. 回退流程

```
点击 Rewind → GET /checkpoints → 显示回退选择器
                                   ↓
选择检查点 → 选择操作范围 → POST /rewind (或 /fork, /summarize)
                                   ↓
会话状态更新
```

### 键盘快捷键

| 快捷键 | 功能 |
|--------|------|
| `Enter` | 发送消息 |
| `Esc` | 取消当前任务 |
| `Esc × 2` | 打开回退选择器 |
| `/` | 打开斜杠命令菜单 |
| `Shift + Tab` | 切换模式 |
| `Y` | 批准单个请求 |
| `A` | 批准会话级请求 |
| `N` | 拒绝请求 |
| `j/k` 或 `↑/↓` | 在选择器中导航 |

### 斜杠命令列表

| 命令 | 描述 | 对应端点 |
|------|------|----------|
| `/compact` | 压缩会话 | `POST /compact` |
| `/new` | 新会话 | `POST /new` |
| `/resume` | 恢复会话 | `POST /resume` |
| `/rewind` | 回退到检查点 | `GET /checkpoints` → UI |
| `/tree` | 显示分支树 | `GET /branches` |
| `/branch` | 创建分支 | `POST /fork` |
| `/switch` | 切换分支 | `POST /resume` |
| `/model` | 列出/切换模型 | `POST /submit` (命令) |
| `/effort` | 推理级别 | `POST /submit` (命令) |
| `/mcp` | MCP 服务器 | - |
| `/skill` | 技能列表 | `GET /skills` |
| `/hooks` | 钩子管理 | - |
| `/memory` | 显示记忆 | - |
| `/forget` | 删除记忆 | `POST /forget` |
| `/thinking` | 思考级别 | - |
| `/verbose` | 切换推理显示 | - |
| `/help` | 帮助信息 | - |

### 输出功能 UI 布局

#### 消息输出类型

| 输出类型 | 样式类 | 视觉特征 |
|----------|--------|----------|
| 用户消息 | `msg--user` | 灰色尖括号 `›` 前缀，粗体文本 |
| 助手消息 | `msg--assistant` | 普通文本，支持推理折叠 |
| 推理内容 | `reasoning` | 可折叠区域，浅背景，左边框 |
| 工具调度 | `card` (accent) | 橙色主题，旋转图标 |
| 工具成功 | `card` (success) | 绿色主题，勾选图标 |
| 工具失败 | `card` (danger) | 红色主题，叉号图标 |
| 批准请求 | `approval` | 橙色边框，阴影效果 |
| 提问请求 | `ask` | 灰色边框，选项按钮 |
| 系统通知 | `msg--system` | 灰色左边框 |
| 错误消息 | `msg--error` | 红色左边框，红色背景 |
| 阶段提示 | `phase` | 居中，大写，灰色小字 |
| 压缩提示 | `compaction` | 虚线边框，可折叠 |

#### 状态指示

| 状态 | 视觉表现 |
|------|----------|
| 空闲 | 灰色圆点 |
| 思考中 | 橙色圆点 + 脉冲动画 |
| 已连接 | 绿色圆点 |
| 重连中 | 黄色圆点 + 脉冲动画 |
| 断开连接 | 红色圆点 |

---

## 错误处理

| HTTP 状态码 | 含义 | 场景 |
|-------------|------|------|
| 400 Bad Request | 请求参数缺失或格式错误 | input/turn/id/path/name 缺失，参数格式错误 |
| 415 Unsupported Media Type | Content-Type 不正确 | POST 请求未携带 `application/json` |
| 500 Internal Server Error | 服务器内部错误 | 模型切换失败、压缩失败、回退失败等 |

**错误响应格式**：
```json
{
  "error": "错误描述信息"
}
```

---

## 安全注意事项

1. **无认证机制**：服务器默认绑定 localhost，依赖同源策略保护
2. **CSRF 防护**：POST 请求强制要求 `application/json` Content-Type，防止跨站请求
3. **CORS**：生产环境不应启用 CORS，开发环境可通过 `HandlerWithCORS()` 临时启用
4. **建议**：生产部署时应添加认证层和 HTTPS

---

## 使用示例

### JavaScript 客户端示例

```javascript
// 连接事件流
const eventSource = new EventSource('/events');
eventSource.onmessage = (event) => {
  const data = JSON.parse(event.data);
  console.log('Event:', data.kind, data);
};

// 提交消息
const submitResponse = await fetch('/submit', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({ input: '分析这段代码' })
});

// 获取状态
const status = await fetch('/status').then(res => res.json());
console.log('Status:', status);

// 批准工具调用
await fetch('/approve', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({ id: 'req-123', allow: true })
});

// 回退到检查点
await fetch('/rewind', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({ turn: 2, scope: 'both' })
});
```

---

*文档版本：v1.1*  
*生成日期：2026-06-04*