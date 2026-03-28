#!/bin/bash

# =====================================================
# 客户服务系统 - 数据库初始化脚本（开发环境）
# =====================================================
# 用途：在Docker容器中执行数据库初始化SQL脚本
# 使用方法：
#   1. 重新初始化（删除现有数据并重建）- 推荐：
#      ./init-database.sh --drop
#   2. 首次初始化（数据库为空）：
#      ./init-database.sh
# =====================================================

set -e

# 颜色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 配置
CONTAINER_NAME="client-postgres"
DB_NAME="client_service"
DB_USER="postgres"
DB_PASSWORD="${POSTGRES_PASSWORD:-postgres}"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
DROP_DB=false

# 解析参数
if [ "$1" = "--drop" ] || [ "$1" = "-d" ]; then
    DROP_DB=true
fi

# 检查容器是否运行
if ! docker ps | grep -q "$CONTAINER_NAME"; then
    echo -e "${RED}错误: PostgreSQL 容器 '$CONTAINER_NAME' 未运行${NC}"
    echo ""
    echo "请先启动数据库容器："
    echo "  cd docker"
    echo "  docker compose up -d postgres"
    exit 1
fi

echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}客户服务系统 - 数据库初始化${NC}"
echo -e "${GREEN}========================================${NC}"
echo ""
echo "配置信息:"
echo "  容器名: $CONTAINER_NAME"
echo "  数据库: $DB_NAME"
echo "  用户:   $DB_USER"
echo "  脚本目录: $SCRIPT_DIR"
echo ""

# 删除并重建数据库（开发环境推荐）
if [ "$DROP_DB" = true ]; then
    echo -e "${YELLOW}开发环境：将删除所有现有数据并重新初始化${NC}"
    echo ""
    echo "1. 断开现有连接并删除数据库..."
    docker exec $CONTAINER_NAME psql -U $DB_USER -d postgres -c "SELECT pg_terminate_backend(pid) FROM pg_stat_activity WHERE datname = '$DB_NAME' AND pid <> pg_backend_pid();" > /dev/null 2>&1 || true
    docker exec $CONTAINER_NAME psql -U $DB_USER -d postgres -c "DROP DATABASE IF EXISTS $DB_NAME;" || true
    echo "2. 创建新数据库..."
    docker exec $CONTAINER_NAME psql -U $DB_USER -d postgres -c "CREATE DATABASE $DB_NAME OWNER $DB_USER;"
    echo ""
fi

# 检查数据库是否存在
if ! docker exec $CONTAINER_NAME psql -U $DB_USER -d postgres -lqt | cut -d \| -f 1 | grep -qw "$DB_NAME"; then
    echo "数据库不存在，正在创建..."
    docker exec $CONTAINER_NAME psql -U $DB_USER -d postgres -c "CREATE DATABASE $DB_NAME OWNER $DB_USER;"
fi

# 定义脚本执行顺序
# 注意：生产环境使用 02-init-data.sql，开发环境使用 02-test-data.sql
SCRIPTS=(
    "01-schema.sql"
    "02-test-data.sql"  # 开发环境：包含测试数据
    # "02-init-data.sql"  # 生产环境：仅包含必需数据（取消注释以使用）
)

# 执行脚本
total=${#SCRIPTS[@]}
current=0

echo "开始执行初始化脚本..."
echo ""

for script in "${SCRIPTS[@]}"; do
    current=$((current + 1))
    script_path="$SCRIPT_DIR/$script"
    
    if [ -f "$script_path" ]; then
        echo -e "${GREEN}[$current/$total] 执行: $script${NC}"
        docker exec -i $CONTAINER_NAME psql -U $DB_USER -d $DB_NAME < "$script_path" || {
            echo -e "${RED}错误: $script 执行失败${NC}"
            exit 1
        }
        echo -e "${GREEN}✓ $script 执行成功${NC}"
        echo ""
    else
        echo -e "${YELLOW}警告: $script 不存在，跳过${NC}"
    fi
done

echo ""
echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}数据库初始化完成！${NC}"
echo -e "${GREEN}========================================${NC}"
echo ""
echo "默认管理员账号："
echo "  用户名: admin"
echo "  密码: admin123"
echo ""
echo "验证数据库："
echo "  docker exec -it $CONTAINER_NAME psql -U $DB_USER -d $DB_NAME -c \"\\dt\""
echo "  docker exec -it $CONTAINER_NAME psql -U $DB_USER -d $DB_NAME -c \"SELECT username, real_name FROM admin_user;\""
echo ""
