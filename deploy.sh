#!/bin/bash
# =====================================================
# 客户服务系统一键部署脚本
# =====================================================
# 使用方法：
#   chmod +x deploy.sh
#   ./deploy.sh [选项]
#
# 选项：
#   --init      首次部署，生成配置文件
#   --build     强制重新构建镜像
#   --down      停止并删除所有容器
#   --logs      查看日志
#   --status    查看服务状态
#   --help      显示帮助信息
# =====================================================

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 脚本目录
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
DOCKER_DIR="$SCRIPT_DIR/docker"

# 从 docker/.env 读取 NGINX_PORT，供健康检查与访问提示使用（与 compose 暴露端口一致）
load_nginx_port_from_docker_env() {
    NGINX_PORT=""
    local env_file="$DOCKER_DIR/.env"
    if [[ -f "$env_file" ]]; then
        local line
        while IFS= read -r line || [[ -n "$line" ]]; do
            [[ "$line" =~ ^[[:space:]]*# ]] && continue
            [[ "$line" =~ ^[[:space:]]*NGINX_PORT= ]] || continue
            NGINX_PORT="${line#*=}"
            NGINX_PORT="${NGINX_PORT%$'\r'}"
            NGINX_PORT="${NGINX_PORT//\"/}"
            NGINX_PORT="${NGINX_PORT//\'/}"
            break
        done < "$env_file"
    fi
    export NGINX_PORT="${NGINX_PORT:-80}"
}

# 日志函数
log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

log_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# 显示帮助信息
show_help() {
    echo "客户服务系统部署脚本"
    echo ""
    echo "使用方法: $0 [选项]"
    echo ""
    echo "部署选项:"
    echo "  --init      首次部署，生成配置文件并启动"
    echo "  --build     强制重新构建镜像并启动"
    echo "  --quick     快速更新（重建镜像并重启，不备份）"
    echo "  --upgrade   升级系统（备份 → 拉取代码 → 重建 → 重启）"
    echo "  --backup    仅备份数据（数据库 + 文件 + 配置）"
    echo ""
    echo "运维选项:"
    echo "  --status    查看服务状态和健康检查"
    echo "  --logs      查看日志（Ctrl+C 退出）"
    echo "  --restart   重启所有服务"
    echo "  --stop      停止服务（保留容器）"
    echo "  --down      停止并删除所有容器"
    echo "  --check     检查并自动补齐配置（不启动服务）"
    echo ""
    echo "其他:"
    echo "  --help      显示此帮助信息"
    echo ""
    echo "示例:"
    echo "  $0 --init       # 首次部署"
    echo "  $0              # 启动服务"
    echo "  $0 --upgrade    # 升级到最新版本"
    echo "  $0 --backup     # 备份数据"
    echo "  $0 --build      # 重新构建并启动"
}

# 检查依赖
check_dependencies() {
    log_info "检查系统依赖..."
    
    # 检查 Docker
    if ! command -v docker &> /dev/null; then
        log_error "Docker 未安装，请先安装 Docker"
        echo "安装指南: https://docs.docker.com/get-docker/"
        exit 1
    fi
    
    # 检查 Docker Compose
    if ! docker compose version &> /dev/null; then
        log_error "Docker Compose 未安装或版本过低"
        echo "请安装 Docker Compose V2"
        exit 1
    fi
    
    # 检查 Docker 服务
    if ! docker info &> /dev/null; then
        log_error "Docker 服务未运行，请启动 Docker"
        exit 1
    fi
    
    log_success "依赖检查通过"
}

# 生成随机密钥（仅包含字母数字，避免特殊字符导致 .env 解析问题）
generate_secret() {
    local length="${1:-32}"
    # 使用 base64 但移除特殊字符 +/=，只保留字母数字
    openssl rand -base64 "$((length * 2))" 2>/dev/null | tr -dc 'a-zA-Z0-9' | head -c "$length"
}

# 需要自动生成的安全配置（配置名=默认值模式）
# 如果配置为空或匹配默认值模式，则自动生成
SECURE_CONFIGS=(
    "POSTGRES_PASSWORD:your_secure_password_here:24"
    "JWT_SECRET:your_very_long_and_secure_jwt_secret_key_at_least_64_characters:64"
    "ONLYOFFICE_JWT_SECRET:your_onlyoffice_jwt_secret_at_least_32_chars:32"
    "MINIO_ACCESS_KEY:your_minio_access_key:16"
    "MINIO_SECRET_KEY:your_minio_secret_key:32"
)

# 检查配置值是否需要生成（为空或匹配默认值）
needs_generation() {
    local value="$1"
    local default_pattern="$2"
    
    # 为空
    if [ -z "$value" ]; then
        return 0
    fi
    
    # 匹配默认值模式
    if [ "$value" = "$default_pattern" ]; then
        return 0
    fi
    
    # 包含 "your_" 或 "example" 等占位符
    if [[ "$value" == *"your_"* ]] || [[ "$value" == *"example"* ]] || [[ "$value" == *"changeme"* ]]; then
        return 0
    fi
    
    return 1
}

# 从 .env 文件安全读取配置值
get_env_value() {
    local env_file="$1"
    local key="$2"
    grep "^${key}=" "$env_file" 2>/dev/null | cut -d'=' -f2- | head -1
}

# 验证并自动补齐配置
validate_and_fix_config() {
    local env_file="$DOCKER_DIR/.env"
    
    if [ ! -f "$env_file" ]; then
        return 1
    fi
    
    log_info "检查安全配置..."
    
    local needs_update=false
    local fixed_items=()
    
    for config in "${SECURE_CONFIGS[@]}"; do
        IFS=':' read -r key default_pattern secret_len <<< "$config"
        
        # 安全读取当前值（不使用 source）
        current_value=$(get_env_value "$env_file" "$key")
        
        # 检查是否需要生成
        if needs_generation "$current_value" "$default_pattern"; then
            # 生成新密钥
            new_value=$(generate_secret "$secret_len" | tr -d '\n')
            
            # 更新文件
            if grep -q "^${key}=" "$env_file"; then
                # 使用 sed 更新（兼容 macOS 和 Linux）
                if [[ "$OSTYPE" == "darwin"* ]]; then
                    sed -i '' "s|^${key}=.*|${key}=${new_value}|" "$env_file"
                else
                    sed -i "s|^${key}=.*|${key}=${new_value}|" "$env_file"
                fi
            else
                # 追加配置
                echo "${key}=${new_value}" >> "$env_file"
            fi
            
            needs_update=true
            fixed_items+=("$key")
        fi
    done
    
    if [ "$needs_update" = true ]; then
        log_success "已自动生成安全密钥："
        for item in "${fixed_items[@]}"; do
            echo "  - $item"
        done
        echo ""
    else
        log_success "安全配置检查通过"
    fi
    
    return 0
}

# 从模板同步缺失的配置项
sync_config_from_template() {
    local env_file="$DOCKER_DIR/.env"
    local template_file="$DOCKER_DIR/.env.example"
    
    if [ ! -f "$template_file" ]; then
        return 0
    fi
    
    if [ ! -f "$env_file" ]; then
        return 1
    fi
    
    log_info "同步配置模板..."
    
    local added_items=()
    
    # 读取模板中的所有配置键
    while IFS= read -r line || [ -n "$line" ]; do
        # 跳过注释和空行
        if [[ "$line" =~ ^[[:space:]]*# ]] || [[ -z "${line// }" ]]; then
            continue
        fi
        
        # 提取键名
        if [[ "$line" =~ ^([A-Z_][A-Z0-9_]*)= ]]; then
            key="${BASH_REMATCH[1]}"
            
            # 检查 .env 中是否存在此键
            if ! grep -q "^${key}=" "$env_file"; then
                # 从模板获取默认值
                default_value="${line#*=}"
                
                # 追加到 .env
                echo "${key}=${default_value}" >> "$env_file"
                added_items+=("$key")
            fi
        fi
    done < "$template_file"
    
    if [ ${#added_items[@]} -gt 0 ]; then
        log_success "已从模板同步缺失配置："
        for item in "${added_items[@]}"; do
            echo "  - $item"
        done
        echo ""
    fi
    
    return 0
}

# 替换单个配置值（如果是模板占位符则替换，否则保留）
# 参数: $1=文件路径, $2=配置键, $3=模板占位符, $4=新值
replace_if_placeholder() {
    local file="$1"
    local key="$2"
    local placeholder="$3"
    local new_value="$4"
    
    local current_value=$(get_env_value "$file" "$key")
    
    # 如果当前值与模板占位符一致，或为空，则替换
    if [ "$current_value" = "$placeholder" ] || [ -z "$current_value" ]; then
        if [[ "$OSTYPE" == "darwin"* ]]; then
            sed -i '' "s|^${key}=.*|${key}=${new_value}|" "$file"
        else
            sed -i "s|^${key}=.*|${key}=${new_value}|" "$file"
        fi
        echo "generated"
    else
        echo "preserved"
    fi
}

# 初始化配置文件
init_config() {
    log_info "初始化配置文件..."
    
    local template_file="$DOCKER_DIR/.env.example"
    local env_file="$DOCKER_DIR/.env"
    local old_env_file=""
    
    if [ ! -f "$template_file" ]; then
        log_error "模板文件不存在: $template_file"
        exit 1
    fi
    
    # 检查是否已有配置
    if [ -f "$env_file" ]; then
        log_warn ".env 文件已存在，将智能合并配置"
        echo "  - 已设置的密钥将保留"
        echo "  - 新增配置项将从模板补充"
        echo "  - 模板占位符将自动生成新值"
        echo ""
        read -p "是否继续？(y/N): " confirm
        if [ "$confirm" != "y" ] && [ "$confirm" != "Y" ]; then
            log_info "跳过配置文件生成"
            return
        fi
        # 备份并保存旧配置路径
        old_env_file="$env_file.bak.$(date +%Y%m%d%H%M%S)"
        cp "$env_file" "$old_env_file"
        log_info "已备份现有配置: $old_env_file"
    fi
    
    # 总是从模板重新生成，确保结构完整
    cp "$template_file" "$env_file"
    
    # 如果有旧配置，恢复已设置的值
    if [ -n "$old_env_file" ] && [ -f "$old_env_file" ]; then
        log_info "正在合并现有配置..."
        # 读取旧配置中的所有键值对，恢复非占位符的值
        while IFS= read -r line || [ -n "$line" ]; do
            # 跳过注释和空行
            if [[ "$line" =~ ^[[:space:]]*# ]] || [[ -z "${line// }" ]]; then
                continue
            fi
            # 提取键值对
            if [[ "$line" =~ ^([A-Z_][A-Z0-9_]*)=(.*)$ ]]; then
                local key="${BASH_REMATCH[1]}"
                local value="${BASH_REMATCH[2]}"
                # 如果值不是占位符且不为空，恢复到新文件
                if [ -n "$value" ] && \
                   [ "$value" != "your_secure_password_here" ] && \
                   [ "$value" != "your_very_long_and_secure_jwt_secret_key_at_least_64_characters" ] && \
                   [ "$value" != "your_onlyoffice_jwt_secret_at_least_32_chars" ] && \
                   [ "$value" != "your_minio_access_key" ] && \
                   [ "$value" != "your_minio_secret_key" ]; then
                    # 更新新文件中的值
                    if [[ "$OSTYPE" == "darwin"* ]]; then
                        sed -i '' "s|^${key}=.*|${key}=${value}|" "$env_file"
                    else
                        sed -i "s|^${key}=.*|${key}=${value}|" "$env_file"
                    fi
                fi
            fi
        done < "$old_env_file"
    fi
    
    # 生成新的安全密钥
    local NEW_DB_PASSWORD=$(generate_secret 24)
    local NEW_JWT_SECRET=$(generate_secret 64)
    local NEW_ONLYOFFICE_SECRET=$(generate_secret 32)
    local NEW_MINIO_ACCESS=$(generate_secret 16)
    local NEW_MINIO_SECRET=$(generate_secret 32)
    
    # 定义需要处理的密钥配置
    local generated_keys=()
    local preserved_keys=()
    
    # 处理 POSTGRES_PASSWORD
    local result=$(replace_if_placeholder "$env_file" "POSTGRES_PASSWORD" "your_secure_password_here" "$NEW_DB_PASSWORD")
    if [ "$result" = "generated" ]; then
        generated_keys+=("POSTGRES_PASSWORD")
    else
        preserved_keys+=("POSTGRES_PASSWORD")
    fi
    
    # 处理 JWT_SECRET
    result=$(replace_if_placeholder "$env_file" "JWT_SECRET" "your_very_long_and_secure_jwt_secret_key_at_least_64_characters" "$NEW_JWT_SECRET")
    if [ "$result" = "generated" ]; then
        generated_keys+=("JWT_SECRET")
    else
        preserved_keys+=("JWT_SECRET")
    fi
    
    # 处理 ONLYOFFICE_JWT_SECRET
    result=$(replace_if_placeholder "$env_file" "ONLYOFFICE_JWT_SECRET" "your_onlyoffice_jwt_secret_at_least_32_chars" "$NEW_ONLYOFFICE_SECRET")
    if [ "$result" = "generated" ]; then
        generated_keys+=("ONLYOFFICE_JWT_SECRET")
    else
        preserved_keys+=("ONLYOFFICE_JWT_SECRET")
    fi
    
    # 处理 MINIO_ACCESS_KEY
    result=$(replace_if_placeholder "$env_file" "MINIO_ACCESS_KEY" "your_minio_access_key" "$NEW_MINIO_ACCESS")
    if [ "$result" = "generated" ]; then
        generated_keys+=("MINIO_ACCESS_KEY")
    else
        preserved_keys+=("MINIO_ACCESS_KEY")
    fi
    
    # 处理 MINIO_SECRET_KEY
    result=$(replace_if_placeholder "$env_file" "MINIO_SECRET_KEY" "your_minio_secret_key" "$NEW_MINIO_SECRET")
    if [ "$result" = "generated" ]; then
        generated_keys+=("MINIO_SECRET_KEY")
    else
        preserved_keys+=("MINIO_SECRET_KEY")
    fi
    
    # 更新生成时间戳（替换模板第一行注释）
    if [[ "$OSTYPE" == "darwin"* ]]; then
        sed -i '' "1s|.*|# 客户服务系统环境变量配置 - 生成时间: $(date '+%Y-%m-%d %H:%M:%S')|" "$env_file"
    else
        sed -i "1s|.*|# 客户服务系统环境变量配置 - 生成时间: $(date '+%Y-%m-%d %H:%M:%S')|" "$env_file"
    fi

    log_success "配置文件已生成: $env_file"
    
    # 显示结果
    if [ ${#generated_keys[@]} -gt 0 ]; then
        echo ""
        echo "已自动生成的安全密钥："
        for key in "${generated_keys[@]}"; do
            echo "  - $key"
        done
    fi
    
    if [ ${#preserved_keys[@]} -gt 0 ]; then
        echo ""
        echo "已保留的现有配置："
        for key in "${preserved_keys[@]}"; do
            echo "  - $key"
        done
    fi
    
    echo ""
    log_warn "请根据实际情况修改以下配置："
    echo "  docker/.env:"
    echo "    - BASE_URL - 系统外部访问地址"
    echo "    - LAW_FIRM_CALLBACK_URL - 律所系统回调地址"
    echo "    - CALLBACK_API_KEY - 回调 API 密钥"
    echo "  frontend/.env.production:"
    echo "    - VITE_LAW_FIRM_NAME - 律所名称"
    echo "    - VITE_APP_SLOGAN - 首页标语"
}

# 修复 volume 权限（非 root 用户运行容器需要）
fix_volume_permissions() {
    log_info "检查并修复 volume 权限..."
    
    # 获取 volume 名称前缀（基于 compose 项目名）
    local project_name=$(docker compose config --format json 2>/dev/null | grep -o '"name":"[^"]*"' | head -1 | cut -d'"' -f4)
    if [ -z "$project_name" ]; then
        project_name="client"
    fi
    
    # 需要修复权限的 volumes
    local volumes=(
        "${project_name}-app-logs"
        "${project_name}-file-storage"
    )
    
    for vol in "${volumes[@]}"; do
        if docker volume inspect "$vol" &>/dev/null; then
            # 使用 alpine 容器修复权限（UID 1000 = appuser）
            docker run --rm -v "$vol:/data" alpine chown -R 1000:1000 /data 2>/dev/null || true
        fi
    done
    
    log_success "volume 权限检查完成"
}

# 构建镜像
build_images() {
    log_info "构建 Docker 镜像..."
    cd "$DOCKER_DIR"
    docker compose build --no-cache
    log_success "镜像构建完成"
}

# 启动服务
start_services() {
    log_info "启动服务..."
    cd "$DOCKER_DIR"
    
    # 检查 .env 文件
    if [ ! -f ".env" ]; then
        log_error ".env 文件不存在，请先运行 --init 初始化配置"
        exit 1
    fi
    
    # 同步模板中缺失的配置项
    sync_config_from_template
    
    # 验证并自动补齐安全配置
    validate_and_fix_config
    
    # 修复 volume 权限（非 root 用户运行需要）
    fix_volume_permissions
    
    # 启动
    if [ "$BUILD_FLAG" = "true" ]; then
        docker compose up -d --build
    else
        docker compose up -d
    fi
    
    log_success "服务启动完成"
}

# 停止服务
stop_services() {
    log_info "停止服务..."
    cd "$DOCKER_DIR"
    docker compose down
    log_success "服务已停止"
}

# 重启服务
restart_services() {
    log_info "重启服务..."
    cd "$DOCKER_DIR"
    docker compose restart
    log_success "服务已重启"
}

# 查看日志
show_logs() {
    cd "$DOCKER_DIR"
    docker compose logs -f
}

# 备份数据
backup_data() {
    log_info "开始备份数据..."
    
    local BACKUP_DIR="$SCRIPT_DIR/backups"
    local TIMESTAMP=$(date +%Y%m%d_%H%M%S)
    local BACKUP_NAME="backup-$TIMESTAMP"
    
    mkdir -p "$BACKUP_DIR/$BACKUP_NAME"
    
    cd "$DOCKER_DIR"
    
    # 备份数据库
    log_info "备份数据库..."
    if docker compose ps postgres 2>/dev/null | grep -q "Up\|running"; then
        docker compose exec -T postgres pg_dump -U postgres client_service > "$BACKUP_DIR/$BACKUP_NAME/database.sql"
        log_success "数据库备份完成"
    else
        log_warn "PostgreSQL 容器未运行，跳过数据库备份"
    fi
    
    # 备份文件存储
    log_info "备份文件存储..."
    if docker cp client-service-backend:/data/client-service/files "$BACKUP_DIR/$BACKUP_NAME/files" 2>/dev/null; then
        log_success "文件存储备份完成"
    else
        log_warn "文件存储目录为空或不存在"
    fi
    
    # 备份配置
    cp "$DOCKER_DIR/.env" "$BACKUP_DIR/$BACKUP_NAME/.env" 2>/dev/null || true
    
    # 压缩
    cd "$BACKUP_DIR"
    tar -czf "$BACKUP_NAME.tar.gz" "$BACKUP_NAME"
    rm -rf "$BACKUP_NAME"
    
    log_success "备份完成: $BACKUP_DIR/$BACKUP_NAME.tar.gz"
    echo ""
    echo "备份文件列表："
    ls -lh "$BACKUP_DIR"/*.tar.gz 2>/dev/null | tail -5
}

# 升级系统
upgrade_system() {
    log_info "开始升级系统..."
    echo ""
    
    # 检测当前分支
    cd "$SCRIPT_DIR"
    CURRENT_BRANCH=$(git rev-parse --abbrev-ref HEAD 2>/dev/null || echo "main")
    log_info "当前分支: $CURRENT_BRANCH"
    
    # 1. 备份
    log_warn "步骤 1/4: 备份当前数据"
    backup_data
    
    # 2. 拉取最新代码
    log_info "步骤 2/4: 拉取最新代码..."
    if git pull origin "$CURRENT_BRANCH"; then
        log_success "代码更新完成"
    else
        log_error "代码更新失败，请手动处理 git 冲突"
        exit 1
    fi
    
    # 3. 重新构建
    log_info "步骤 3/4: 重新构建镜像..."
    cd "$DOCKER_DIR"
    docker compose build --no-cache
    
    # 4. 重启服务
    log_info "步骤 4/4: 重启服务..."
    docker compose up -d
    
    # 等待服务就绪
    wait_for_services
    
    log_success "升级完成！"
    show_status
}

# 查看状态
show_status() {
    load_nginx_port_from_docker_env
    log_info "服务状态:"
    cd "$DOCKER_DIR"
    echo ""
    docker compose ps
    echo ""
    
    # 健康检查
    log_info "健康检查:"
    echo ""
    
    # 检查 Nginx
    if curl -sf http://localhost:${NGINX_PORT:-80}/health > /dev/null 2>&1; then
        echo -e "  Nginx:    ${GREEN}✓ 正常${NC}"
    else
        echo -e "  Nginx:    ${RED}✗ 异常${NC}"
    fi
    
    # 检查后端
    if docker exec client-service-backend curl -sf http://localhost:8081/api/health > /dev/null 2>&1; then
        echo -e "  后端:     ${GREEN}✓ 正常${NC}"
    else
        echo -e "  后端:     ${RED}✗ 异常${NC}"
    fi
    
    # 检查数据库
    if docker exec client-service-postgres pg_isready > /dev/null 2>&1; then
        echo -e "  数据库:   ${GREEN}✓ 正常${NC}"
    else
        echo -e "  数据库:   ${RED}✗ 异常${NC}"
    fi
    
    # 检查 Redis
    if docker exec client-service-redis redis-cli ping > /dev/null 2>&1; then
        echo -e "  Redis:    ${GREEN}✓ 正常${NC}"
    else
        echo -e "  Redis:    ${RED}✗ 异常${NC}"
    fi
    
    echo ""
}

# 等待服务就绪
wait_for_services() {
    load_nginx_port_from_docker_env
    log_info "等待服务就绪..."
    
    local max_attempts=30
    local attempt=0
    
    while [ $attempt -lt $max_attempts ]; do
        if curl -sf http://localhost:${NGINX_PORT:-80}/health > /dev/null 2>&1; then
            log_success "服务已就绪"
            return 0
        fi
        
        attempt=$((attempt + 1))
        echo -n "."
        sleep 2
    done
    
    echo ""
    log_warn "服务启动超时，请检查日志"
    return 1
}

# 显示访问信息
show_access_info() {
    load_nginx_port_from_docker_env
    echo ""
    echo "================================================="
    echo -e "${GREEN}部署完成！${NC}"
    echo "================================================="
    echo ""
    echo "访问地址:"
    echo "  客户门户: http://localhost:${NGINX_PORT:-80}/"
    echo "  管理后台: http://localhost:${NGINX_PORT:-80}/admin"
    echo ""
    echo "常用命令:"
    echo "  查看状态: $0 --status"
    echo "  查看日志: $0 --logs"
    echo "  停止服务: $0 --down"
    echo "  重启服务: $0 --restart"
    echo ""
    echo "配置文件: $DOCKER_DIR/.env"
    echo "================================================="
}

# 主函数
main() {
    BUILD_FLAG="false"
    
    case "${1:-}" in
        --help|-h)
            show_help
            exit 0
            ;;
        --init)
            check_dependencies
            init_config
            start_services
            wait_for_services
            show_access_info
            ;;
        --build)
            check_dependencies
            BUILD_FLAG="true"
            start_services
            wait_for_services
            show_status
            ;;
        --check)
            cd "$DOCKER_DIR"
            if [ ! -f ".env" ]; then
                log_error ".env 文件不存在，请先运行 --init 初始化配置"
                exit 1
            fi
            sync_config_from_template
            validate_and_fix_config
            log_success "配置检查完成"
            ;;
        --stop)
            log_info "停止服务..."
            cd "$DOCKER_DIR"
            docker compose stop
            log_success "服务已停止"
            ;;
        --down)
            stop_services
            ;;
        --restart)
            restart_services
            ;;
        --quick)
            check_dependencies
            log_info "快速更新..."
            cd "$DOCKER_DIR"
            docker compose build --no-cache
            docker compose up -d
            wait_for_services
            show_status
            ;;
        --backup)
            backup_data
            ;;
        --upgrade)
            check_dependencies
            upgrade_system
            ;;
        --logs)
            show_logs
            ;;
        --status)
            show_status
            ;;
        "")
            check_dependencies
            start_services
            wait_for_services
            show_status
            ;;
        *)
            log_error "未知选项: $1"
            echo ""
            show_help
            exit 1
            ;;
    esac
}

# 执行主函数
main "$@"
