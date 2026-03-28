# 版本检查配置模板

> 文档版本：v1.0  
> 更新日期：2026-02-04

## 📋 概述

本文档提供版本检查功能的配置模板和示例，包括 GitHub Releases 配置和自定义版本检查 URL 配置。

---

## 🔧 配置方式

系统支持两种版本检查方式：

### 方式一：GitHub Releases（推荐）

从 GitHub Releases 自动获取最新版本信息。

### 方式二：自定义版本检查 URL

通过自定义 API 端点获取版本信息。

---

## 📝 配置模板

### 1. GitHub Releases 配置

#### 环境变量配置

在 `.env` 文件中添加：

```bash
# GitHub 仓库（格式: owner/repo）
APP_VERSION_GITHUB_REPO=junyuzhan/law-firm
```

#### 配置文件配置

在 `application.yml` 中配置：

```yaml
app:
  version: 1.0.0  # 当前版本
  version-check:
    github-repo: junyuzhan/law-firm  # GitHub 仓库
```

#### 配置示例

```bash
# 示例：配置 GitHub 仓库
APP_VERSION_GITHUB_REPO=junyuzhan/law-firm
```

---

### 2. 自定义版本检查 URL 配置

#### 环境变量配置

在 `.env` 文件中添加：

```bash
# 自定义版本检查 URL
APP_VERSION_CHECK_URL=https://your-server.com/api/version/latest
```

#### 配置文件配置

在 `application.yml` 中配置：

```yaml
app:
  version: 1.0.0  # 当前版本
  version-check:
    url: https://your-server.com/api/version/latest  # 自定义版本检查 URL
```

---

## 📄 API 响应格式模板

### 自定义版本检查 URL 响应格式

自定义版本检查 URL 需要返回以下 JSON 格式：

```json
{
  "version": "1.2.0",
  "releaseNotes": "## 更新内容\n\n- 新增功能 A\n- 修复问题 B\n- 优化性能 C",
  "releaseUrl": "https://github.com/junyuzhan/law-firm/releases/tag/v1.2.0",
  "publishedAt": "2026-02-04T10:00:00Z"
}
```

#### 字段说明

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `version` | String | 是 | 版本号（如：`1.2.0`，会自动去除 `v` 前缀） |
| `releaseNotes` | String | 否 | 发布说明（支持 Markdown） |
| `releaseUrl` | String | 否 | 发布页面 URL |
| `publishedAt` | String | 否 | 发布时间（ISO 8601 格式） |

#### 响应示例

**示例 1：完整响应**

```json
{
  "version": "1.2.0",
  "releaseNotes": "## v1.2.0 更新内容\n\n### 新增功能\n- 新增文件预览功能\n- 新增批量操作功能\n\n### 修复问题\n- 修复登录问题\n- 修复文件上传问题\n\n### 优化\n- 优化页面加载速度\n- 优化移动端体验",
  "releaseUrl": "https://github.com/junyuzhan/law-firm/releases/tag/v1.2.0",
  "publishedAt": "2026-02-04T10:00:00Z"
}
```

**示例 2：最小响应**

```json
{
  "version": "1.2.0"
}
```

---

## 🚀 GitHub Releases 配置指南

### 1. 创建 GitHub Release

1. 登录 GitHub，进入仓库
2. 点击 **Releases** → **Create a new release**
3. 填写 Release 信息：
   - **Tag version**：版本标签（如：`v1.2.0`）
   - **Release title**：发布标题（如：`v1.2.0`）
   - **Description**：发布说明（支持 Markdown）

### 2. Release 标签格式

- ✅ 推荐格式：`v1.2.0`（系统会自动去除 `v` 前缀）
- ✅ 也支持：`1.2.0`（无前缀）
- ❌ 不推荐：`version-1.2.0`（非标准格式）

### 3. Release 说明模板

```markdown
## v1.2.0 更新内容

### 🎉 新增功能
- 新增文件预览功能
- 新增批量操作功能

### 🐛 修复问题
- 修复登录问题
- 修复文件上传问题

### ⚡ 性能优化
- 优化页面加载速度
- 优化移动端体验

### 📝 其他
- 更新依赖包
- 改进文档
```

---

## 🔨 自定义版本检查 API 实现示例

### Node.js 示例

```javascript
// server.js
const express = require('express');
const app = express();

app.get('/api/version/latest', (req, res) => {
  res.json({
    version: '1.2.0',
    releaseNotes: '## 更新内容\n\n- 新增功能 A\n- 修复问题 B',
    releaseUrl: 'https://github.com/junyuzhan/law-firm/releases/tag/v1.2.0',
    publishedAt: '2026-02-04T10:00:00Z'
  });
});

app.listen(3000, () => {
  console.log('Version check API running on port 3000');
});
```

### Python Flask 示例

```python
# app.py
from flask import Flask, jsonify
from datetime import datetime

app = Flask(__name__)

@app.route('/api/version/latest')
def get_latest_version():
    return jsonify({
        'version': '1.2.0',
        'releaseNotes': '## 更新内容\n\n- 新增功能 A\n- 修复问题 B',
        'releaseUrl': 'https://github.com/junyuzhan/law-firm/releases/tag/v1.2.0',
        'publishedAt': datetime.now().isoformat() + 'Z'
    })

if __name__ == '__main__':
    app.run(port=3000)
```

### Spring Boot 示例

```java
@RestController
@RequestMapping("/api/version")
public class VersionController {
    
    @GetMapping("/latest")
    public Map<String, Object> getLatestVersion() {
        Map<String, Object> result = new HashMap<>();
        result.put("version", "1.2.0");
        result.put("releaseNotes", "## 更新内容\n\n- 新增功能 A\n- 修复问题 B");
        result.put("releaseUrl", "https://github.com/junyuzhan/law-firm/releases/tag/v1.2.0");
        result.put("publishedAt", "2026-02-04T10:00:00Z");
        return result;
    }
}
```

---

## ⚙️ 版本号格式规范

### 语义化版本号（SemVer）

推荐使用语义化版本号格式：`主版本号.次版本号.修订号`

- **主版本号**：不兼容的 API 修改
- **次版本号**：向下兼容的功能性新增
- **修订号**：向下兼容的问题修正

### 版本号示例

- ✅ `1.0.0` - 标准格式
- ✅ `1.2.3` - 标准格式
- ✅ `v1.2.3` - 带前缀（系统会自动去除）
- ✅ `1.2.3-beta` - 预发布版本
- ❌ `1.2` - 缺少修订号（不推荐）
- ❌ `version-1.2.3` - 非标准格式

### 版本比较规则

系统会自动比较版本号：

- `1.2.0` > `1.1.0` ✅
- `1.2.0` = `1.2.0` ✅
- `1.2.0` < `1.3.0` ✅
- `1.2.0-beta` < `1.2.0` ✅（预发布版本）

---

## 🔍 配置验证

### 检查配置是否正确

1. **检查环境变量**

```bash
# 检查 GitHub 仓库配置
echo $APP_VERSION_GITHUB_REPO

# 检查自定义 URL 配置
echo $APP_VERSION_CHECK_URL
```

2. **测试版本检查 API**

```bash
# 测试 GitHub Releases API
curl https://api.github.com/repos/junyuzhan/law-firm/releases/latest

# 测试自定义版本检查 API
curl https://your-server.com/api/version/latest
```

3. **在管理后台检查**

- 登录管理后台
- 进入 **系统维护** → **系统升级**
- 点击 **检查更新** 按钮
- 查看版本信息是否正确显示

---

## 📋 完整配置示例

### Docker 环境变量配置

```bash
# .env 文件
APP_VERSION_GITHUB_REPO=junyuzhan/law-firm
# 或者使用自定义 URL
# APP_VERSION_CHECK_URL=https://api.example.com/version/latest
```

### application.yml 配置

```yaml
app:
  version: 1.0.0
  version-check:
    # 方式一：GitHub 仓库
    github-repo: junyuzhan/law-firm
    # 方式二：自定义 URL（优先级高于 GitHub）
    # url: https://api.example.com/version/latest
```

---

## ⚠️ 注意事项

1. **优先级**：自定义 URL 优先级高于 GitHub 仓库
2. **版本号格式**：建议使用语义化版本号（SemVer）
3. **GitHub API 限制**：GitHub API 有速率限制，但检查频率不高，通常不受影响
4. **网络访问**：确保服务器可以访问 GitHub API 或自定义版本检查 URL
5. **HTTPS**：生产环境建议使用 HTTPS

---

## 🐛 常见问题

### Q1: 配置后无法获取版本信息？

**A**: 检查以下几点：
1. 环境变量是否正确配置
2. 服务器是否可以访问 GitHub API 或自定义 URL
3. GitHub 仓库名称格式是否正确（`owner/repo`）
4. 查看后端日志，确认错误信息

### Q2: 版本比较不正确？

**A**: 
1. 确保版本号格式正确（如：`1.2.0`）
2. 检查当前版本配置（`app.version`）
3. 系统会自动去除版本号前的 `v` 前缀

### Q3: 如何禁用版本检查？

**A**: 
- 不配置 `APP_VERSION_GITHUB_REPO` 和 `APP_VERSION_CHECK_URL`
- 系统会显示"未配置 GitHub 仓库"提示

### Q4: 自定义 URL 返回格式错误？

**A**: 
- 确保返回的 JSON 格式正确
- 至少包含 `version` 字段
- 检查 Content-Type 是否为 `application/json`

---

## 📚 相关文档

- [系统维护操作手册](./OPERATIONS_MANUAL.md)
- [系统配置指南](../guides/SYSTEM_CONFIG_GUIDE.md)
- [部署指南](../guides/QUICKSTART.md)

---

## 📝 更新日志

- **v1.0** (2026-02-04)：初始版本，添加版本检查配置模板
