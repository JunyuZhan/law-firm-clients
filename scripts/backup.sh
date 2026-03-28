#!/bin/bash

# 客户服务系统备份脚本
# 使用方法: ./scripts/backup.sh

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"
BACKUP_DIR="$PROJECT_DIR/backups"
TIMESTAMP=$(date +%Y%m%d_%H%M%S)
BACKUP_NAME="client-service-backup-$TIMESTAMP"

# 创建备份目录
mkdir -p "$BACKUP_DIR/$BACKUP_NAME"

echo "开始备份客户服务系统..."
echo "备份目录: $BACKUP_DIR/$BACKUP_NAME"

# 备份数据库
echo "备份数据库..."
cd "$PROJECT_DIR/docker"
if docker-compose ps postgres | grep -q "Up"; then
    docker-compose exec -T postgres \
        pg_dump -U postgres client_service > "$BACKUP_DIR/$BACKUP_NAME/database.sql"
    echo "数据库备份完成"
else
    echo "警告: PostgreSQL容器未运行，跳过数据库备份"
fi

# 备份文件存储
echo "备份文件存储..."
if docker-compose ps app | grep -q "Up"; then
    if docker cp client-backend:/data/client-service/files "$BACKUP_DIR/$BACKUP_NAME/files" 2>/dev/null; then
        echo "文件存储备份完成"
    else
        echo "警告: 文件存储目录为空或不存在"
    fi
else
    echo "警告: 应用容器未运行，跳过文件存储备份"
fi

# 备份配置文件
echo "备份配置文件..."
cp "$PROJECT_DIR/docker/.env" "$BACKUP_DIR/$BACKUP_NAME/.env" 2>/dev/null || echo ".env文件不存在"

# 创建压缩包
echo "创建压缩包..."
cd "$BACKUP_DIR"
tar -czf "$BACKUP_NAME.tar.gz" "$BACKUP_NAME"
rm -rf "$BACKUP_NAME"

echo "备份完成: $BACKUP_DIR/$BACKUP_NAME.tar.gz"
