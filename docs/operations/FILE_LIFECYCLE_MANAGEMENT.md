# 文件生命周期管理文档

> 文档版本：v1.0  
> 创建日期：2026-02-02  
> 适用系统：客户服务系统

## 📋 概述

客户服务系统的文件分为两类：
1. **推送文件**：律所系统推送给客户的文件（如判决书、合同等）
2. **客户上传文件**：客户通过门户上传的文件（如证据材料等）

两类文件有不同的生命周期管理规则。

## 🔄 文件生命周期规则

### 1. 推送文件生命周期

#### 1.1 文件状态流转

```
创建（PUSHED）
  ↓
客户首次下载
  ↓
记录 firstDownloadedAt
  ↓
首次下载后超过N天（可配置，默认7天）
  ↓
管理系统可以删除
  ↓
调用 /api/files/delete
  ↓
删除物理文件和记录（STATUS_DELETED）
```

#### 1.2 删除规则

**规则**：客户首次下载后超过N天，管理系统可以删除推送文件

**配置项**：`client-service.file-lifecycle.pushed-file.delete-after-days`

**默认值**：7天

**判断逻辑**：
```java
// 检查是否可以删除
boolean canDelete = file.getFirstDownloadedAt() != null 
    && LocalDateTime.now().isAfter(
        file.getFirstDownloadedAt().plusDays(deleteAfterDays));
```

**注意事项**：
- 如果文件未下载过（`firstDownloadedAt == null`），不能删除
- 只有推送文件（`fileSource == PUSHED`）才适用此规则
- 管理系统需要主动调用删除接口

#### 1.3 查询可删除文件

**方法**：`FileService.getDeletablePushedFiles(matterId)`

**功能**：查询指定项目（或所有项目）中可删除的推送文件列表

**查询条件**：
- `fileSource == PUSHED`
- `status == ACTIVE`
- `firstDownloadedAt != null`
- `firstDownloadedAt < (当前时间 - deleteAfterDays)`

**使用场景**：
- 管理系统定期查询可删除的文件
- 批量删除过期文件

---

### 2. 客户上传文件生命周期

#### 2.1 文件状态流转

```
客户上传（CLIENT_UPLOAD）
  ↓
状态：ACTIVE（物理文件存在）
  ↓
管理系统同步到管理系统
  ↓
调用 /api/files/cleanup
  ↓
删除物理文件，保留记录
  ↓
状态：CLEANED（仅保留记录）
```

#### 2.2 清理规则

**规则**：管理系统同步客户上传的文件后，客户服务系统清理物理文件，但保留记录

**配置项**：`client-service.file-lifecycle.client-upload-file.cleanup-after-sync`

**默认值**：`true`（启用清理）

**清理逻辑**：
```java
// 1. 验证文件来源（必须是CLIENT_UPLOAD）
if (!ClientFile.SOURCE_CLIENT_UPLOAD.equals(file.getFileSource())) {
    return; // 跳过清理
}

// 2. 删除物理文件
storageStrategy.deleteFile(file.getStoragePath());

// 3. 更新状态为CLEANED（保留记录）
file.setStatus(ClientFile.STATUS_CLEANED);
```

**注意事项**：
- 只清理客户上传的文件（`fileSource == CLIENT_UPLOAD`）
- 清理后文件状态变为`CLEANED`，记录保留在数据库中
- 如果清理功能未启用（`cleanup-after-sync = false`），不会清理文件

---

## 📊 文件状态说明

### 状态值

| 状态 | 说明 | 适用文件类型 |
|------|------|------------|
| `ACTIVE` | 有效，物理文件存在 | 推送文件、客户上传文件 |
| `DELETED` | 已删除，物理文件和记录都已删除 | 推送文件 |
| `CLEANED` | 已清理，物理文件已删除，但记录保留 | 客户上传文件 |

### 状态流转图

```
推送文件：
ACTIVE → DELETED（管理系统删除）

客户上传文件：
ACTIVE → CLEANED（管理系统同步后清理）
```

---

## 🔧 配置说明

### application.yml配置

```yaml
client-service:
  # 文件生命周期配置
  file-lifecycle:
    # 推送文件删除配置
    pushed-file:
      delete-after-days: 7  # 客户首次下载后多少天可以删除（默认7天）
    
    # 客户上传文件清理配置
    client-upload-file:
      cleanup-after-sync: true  # 同步到管理系统后是否清理物理文件（默认true）
```

### 配置项说明

#### 1. `pushed-file.delete-after-days`

**类型**：`int`

**默认值**：`7`

**说明**：推送文件首次下载后多少天可以删除

**示例**：
- `7`：首次下载后7天可以删除
- `30`：首次下载后30天可以删除
- `0`：首次下载后立即可以删除（不推荐）

#### 2. `client-upload-file.cleanup-after-sync`

**类型**：`boolean`

**默认值**：`true`

**说明**：管理系统同步客户上传的文件后，是否清理物理文件

**示例**：
- `true`：同步后清理物理文件，保留记录
- `false`：同步后不清理，保留物理文件

---

## 🔌 接口说明

### 1. 文件删除接口（推送文件）

**接口地址**：`POST /api/files/delete`

**认证方式**：API密钥（Bearer Token）

**请求体**：
```json
{
  "fileId": "CS1234567890123456789",
  "action": "DELETE"
}
```

**功能**：删除推送文件的物理文件和记录

**使用场景**：管理系统删除推送文件时调用

---

### 2. 文件清理接口（客户上传文件）

**接口地址**：`POST /api/files/cleanup`

**认证方式**：API密钥（Bearer Token）

**请求体**：
```json
{
  "fileId": "CS1234567890123456789",
  "action": "CLEANUP"
}
```

**功能**：清理客户上传文件的物理文件，保留记录

**使用场景**：管理系统同步客户上传的文件后调用

---

### 3. 查询可删除文件接口（可选）

**接口地址**：`GET /api/files/deletable?matterId={id}`

**认证方式**：API密钥（Bearer Token）

**查询参数**：
- `matterId`（可选）：项目ID，如果不提供则查询所有项目

**响应**：
```json
{
  "success": true,
  "code": "200",
  "data": [
    {
      "id": "CS1234567890123456789",
      "fileName": "判决书.pdf",
      "firstDownloadedAt": "2026-01-26T10:00:00",
      "fileSource": "PUSHED"
    }
  ]
}
```

**功能**：查询可删除的推送文件列表

**使用场景**：管理系统定期查询和批量删除过期文件

**注意**：此接口当前未实现，如果需要可以添加。

---

## 📝 使用示例

### 示例1：管理系统删除推送文件

```java
// 1. 查询可删除的文件
List<ClientFile> deletableFiles = fileService.getDeletablePushedFiles(matterId);

// 2. 调用客户服务系统删除接口
for (ClientFile file : deletableFiles) {
    FileDeleteRequest request = new FileDeleteRequest();
    request.setFileId(file.getExternalFileId());
    request.setAction("DELETE");
    
    restTemplate.postForObject(
        apiUrl + "/api/files/delete",
        request,
        Result.class
    );
}
```

### 示例2：管理系统同步客户上传文件后清理

```java
// 1. 同步文件到管理系统
Document document = syncClientFileToFolder(clientFile);

// 2. 调用客户服务系统清理接口
FileDeleteRequest request = new FileDeleteRequest();
request.setFileId(clientFile.getExternalFileId());
request.setAction("CLEANUP");

restTemplate.postForObject(
    apiUrl + "/api/files/cleanup",
    request,
    Result.class
);
```

---

## ⚠️ 注意事项

### 1. 文件来源标识

- **推送文件**：`fileSource = PUSHED`
- **客户上传文件**：`fileSource = CLIENT_UPLOAD`

**重要**：文件来源在创建时确定，不能修改。

### 2. 首次下载时间

- 只有推送文件才记录`firstDownloadedAt`
- 客户上传文件不记录此字段
- 首次下载后立即记录，后续下载不再更新

### 3. 文件清理时机

- 客户上传文件同步后立即清理（如果启用）
- 清理是异步的，不影响同步流程
- 清理失败会记录日志，但不影响主流程

### 4. 文件删除时机

- 推送文件删除由管理系统主动触发
- 管理系统需要判断是否可以删除（调用`canDeletePushedFile`或查询接口）
- 删除是同步的，会立即删除物理文件

### 5. 数据一致性

- 文件状态和物理文件应该保持一致
- `ACTIVE`：物理文件存在
- `DELETED`：物理文件已删除
- `CLEANED`：物理文件已删除，但记录保留

---

## 🔍 故障排查

### 问题1：推送文件无法删除

**可能原因**：
1. 文件未下载过（`firstDownloadedAt == null`）
2. 未超过删除期限
3. 文件不是推送文件（`fileSource != PUSHED`）

**排查方法**：
```java
// 检查文件是否可以删除
boolean canDelete = fileService.canDeletePushedFile(fileId);
if (!canDelete) {
    // 检查文件来源
    ClientFile file = fileService.getFileById(fileId);
    log.info("文件来源: {}, 首次下载时间: {}", 
            file.getFileSource(), file.getFirstDownloadedAt());
}
```

### 问题2：客户上传文件清理失败

**可能原因**：
1. 文件不是客户上传的文件（`fileSource != CLIENT_UPLOAD`）
2. 清理功能未启用（`cleanup-after-sync = false`）
3. 物理文件不存在或删除失败

**排查方法**：
- 检查文件来源
- 检查配置项`cleanup-after-sync`
- 查看日志中的错误信息

### 问题3：文件状态不一致

**可能原因**：
- 物理文件已删除但状态未更新
- 状态已更新但物理文件未删除

**排查方法**：
- 检查文件状态和物理文件是否存在
- 查看操作日志
- 手动修复状态

---

## 📚 相关文档

- [回调接口规范](./CALLBACK_API_SPEC.md)
- [TODO设计评估报告](./TODO_DESIGN_REVIEW.md)
- [实现回顾总结](./IMPLEMENTATION_REVIEW.md)
