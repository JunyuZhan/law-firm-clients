# 文件病毒扫描配置指南

## 概述

系统支持文件病毒扫描功能，使用ClamAV作为扫描引擎。病毒扫描功能是可选的，默认禁用。

## 依赖说明

### Maven依赖

系统使用 `xyz.capybara:clamav-client` 作为ClamAV客户端库。如果Maven Central无法下载此依赖，可以：

1. **使用可用版本**：尝试使用 `2.1.2` 或 `2.0.1` 版本
2. **暂时禁用**：如果不需要病毒扫描功能，可以暂时注释掉依赖

### 可选依赖配置

病毒扫描功能已配置为可选依赖：
- 如果 `clamav-client` 依赖不可用，`ClamAVVirusScanner` 不会被加载
- 系统会自动使用 `NoOpVirusScanner`（空操作扫描器）
- 应用可以正常启动和运行

## 安装ClamAV

### Ubuntu/Debian

```bash
sudo apt-get update
sudo apt-get install clamav clamav-daemon
sudo systemctl start clamav-daemon
sudo systemctl enable clamav-daemon
```

### CentOS/RHEL

```bash
sudo yum install epel-release
sudo yum install clamav clamav-update
sudo systemctl start clamav-daemon
sudo systemctl enable clamav-daemon
```

### macOS

```bash
brew install clamav
brew services start clamav
```

## 配置说明

### application.yml配置

```yaml
client-service:
  file:
    virus-scan:
      enabled: true  # 启用病毒扫描
      type: clamav   # 扫描器类型：clamav, none
      clamav:
        host: localhost  # ClamAV守护进程地址
        port: 3310       # ClamAV守护进程端口
        timeout: 5000   # 扫描超时时间（毫秒）
```

### 环境变量配置

```bash
export CLIENT_SERVICE_FILE_VIRUS_SCAN_ENABLED=true
export CLIENT_SERVICE_FILE_VIRUS_SCAN_CLAMAV_HOST=localhost
export CLIENT_SERVICE_FILE_VIRUS_SCAN_CLAMAV_PORT=3310
```

## 使用说明

### 启用病毒扫描

1. 确保ClamAV守护进程正在运行
2. 在 `application.yml` 中设置 `client-service.file.virus-scan.enabled=true`
3. 配置ClamAV连接信息（host、port）

### 禁用病毒扫描

1. 在 `application.yml` 中设置 `client-service.file.virus-scan.enabled=false`
2. 或者设置 `client-service.file.virus-scan.type=none`

### 依赖不可用时的处理

如果 `clamav-client` 依赖无法下载：

1. **方案一**：使用可用版本
   ```xml
   <dependency>
       <groupId>xyz.capybara</groupId>
       <artifactId>clamav-client</artifactId>
       <version>2.1.2</version>  <!-- 或 2.0.1 -->
   </dependency>
   ```

2. **方案二**：暂时注释依赖
   ```xml
   <!--
   <dependency>
       <groupId>xyz.capybara</groupId>
       <artifactId>clamav-client</artifactId>
       <version>2.1.2</version>
   </dependency>
   -->
   ```
   系统会自动使用 `NoOpVirusScanner`，不会影响其他功能。

3. **方案三**：使用其他ClamAV客户端库
   - `com.diluv.clamchowder:ClamChowder`
   - `hu.alphabox:clamav-client:0.1`

## 故障排查

### ClamAV连接失败

1. 检查ClamAV守护进程是否运行：
   ```bash
   sudo systemctl status clamav-daemon
   ```

2. 检查端口是否开放：
   ```bash
   netstat -tlnp | grep 3310
   ```

3. 检查防火墙设置

### 扫描超时

1. 增加超时时间配置
2. 检查ClamAV守护进程性能
3. 检查网络连接

### 依赖下载失败

1. 检查Maven仓库配置
2. 尝试使用其他版本
3. 使用本地Maven仓库缓存

## 性能考虑

- 病毒扫描会增加文件上传时间
- 建议在生产环境启用，开发环境可以禁用
- 扫描超时时间应根据文件大小调整

## 安全建议

1. 定期更新ClamAV病毒库：
   ```bash
   sudo freshclam
   ```

2. 配置自动更新：
   ```bash
   sudo systemctl enable clamav-freshclam
   sudo systemctl start clamav-freshclam
   ```

3. 监控扫描失败情况，及时处理
