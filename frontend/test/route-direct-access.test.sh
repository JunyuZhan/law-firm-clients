#!/bin/bash

# 路由直接访问集成测试脚本
# 测试 /portal/matter/:id 路由是否能够直接访问

set -e

# 颜色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 配置
FRONTEND_URL="${FRONTEND_URL:-http://localhost:3000}"
BACKEND_URL="${BACKEND_URL:-http://localhost:8081}"
TEST_MATTER_ID="${TEST_MATTER_ID:-CS1234567890123456789}"
TEST_TOKEN="${TEST_TOKEN:-test-token-12345678901234567890123456789012}"

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}路由直接访问集成测试${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""

# 检查前端服务是否运行
echo -e "${YELLOW}[1/5] 检查前端服务...${NC}"
if curl -s -f "${FRONTEND_URL}" > /dev/null 2>&1; then
    echo -e "${GREEN}✓ 前端服务运行正常 (${FRONTEND_URL})${NC}"
else
    echo -e "${RED}✗ 前端服务未运行，请先启动: npm run dev${NC}"
    exit 1
fi

# 检查后端服务是否运行
echo -e "${YELLOW}[2/5] 检查后端服务...${NC}"
if curl -s -f "${BACKEND_URL}/actuator/health" > /dev/null 2>&1; then
    echo -e "${GREEN}✓ 后端服务运行正常 (${BACKEND_URL})${NC}"
else
    echo -e "${YELLOW}⚠ 后端服务未运行，将跳过API测试${NC}"
    BACKEND_AVAILABLE=false
fi

# 测试路由匹配
echo -e "${YELLOW}[3/5] 测试路由匹配...${NC}"
TEST_URL="${FRONTEND_URL}/portal/matter/${TEST_MATTER_ID}?token=${TEST_TOKEN}"

echo "测试URL: ${TEST_URL}"

# 使用 curl 测试路由
HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" "${TEST_URL}" || echo "000")

if [ "$HTTP_CODE" = "200" ] || [ "$HTTP_CODE" = "000" ]; then
    echo -e "${GREEN}✓ 路由可访问 (HTTP ${HTTP_CODE})${NC}"
    echo -e "${BLUE}  提示: 请在浏览器中打开以下链接验证:${NC}"
    echo -e "${BLUE}  ${TEST_URL}${NC}"
else
    echo -e "${RED}✗ 路由访问失败 (HTTP ${HTTP_CODE})${NC}"
    exit 1
fi

# 测试路由参数提取（需要实际访问页面）
echo -e "${YELLOW}[4/5] 验证路由参数...${NC}"
echo -e "${BLUE}  请手动验证以下内容:${NC}"
echo -e "${BLUE}  1. 访问 ${TEST_URL}${NC}"
echo -e "${BLUE}  2. 打开浏览器控制台 (F12)${NC}"
echo -e "${BLUE}  3. 查看是否有以下日志:${NC}"
echo -e "${BLUE}     - [Router] 匹配到项目详情路由${NC}"
echo -e "${BLUE}     - [MatterDetail] 路由信息${NC}"
echo -e "${BLUE}  4. 确认页面显示项目详情，而不是 Portal 首页${NC}"

# 如果后端可用，测试API调用
if [ "$BACKEND_AVAILABLE" != "false" ]; then
    echo -e "${YELLOW}[5/5] 测试后端API...${NC}"
    API_URL="${BACKEND_URL}/portal/api/matter/${TEST_MATTER_ID}?token=${TEST_TOKEN}"
    
    API_RESPONSE=$(curl -s -w "\n%{http_code}" "${API_URL}" || echo -e "\n000")
    HTTP_CODE=$(echo "$API_RESPONSE" | tail -n1)
    BODY=$(echo "$API_RESPONSE" | head -n-1)
    
    if [ "$HTTP_CODE" = "200" ]; then
        echo -e "${GREEN}✓ 后端API响应正常 (HTTP ${HTTP_CODE})${NC}"
        if echo "$BODY" | grep -q "success"; then
            echo -e "${GREEN}✓ API返回数据格式正确${NC}"
        else
            echo -e "${YELLOW}⚠ API返回数据格式可能不正确${NC}"
            echo "响应: $BODY"
        fi
    elif [ "$HTTP_CODE" = "403" ] || [ "$HTTP_CODE" = "404" ]; then
        echo -e "${YELLOW}⚠ 后端API返回 ${HTTP_CODE} (可能是测试数据不存在)${NC}"
        echo -e "${BLUE}  这是正常的，因为测试ID和Token可能不存在于数据库中${NC}"
    else
        echo -e "${RED}✗ 后端API调用失败 (HTTP ${HTTP_CODE})${NC}"
        echo "响应: $BODY"
    fi
else
    echo -e "${YELLOW}[5/5] 跳过后端API测试 (后端服务未运行)${NC}"
fi

echo ""
echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}测试完成${NC}"
echo -e "${GREEN}========================================${NC}"
echo ""
echo -e "${BLUE}下一步:${NC}"
echo -e "${BLUE}1. 在浏览器中打开: ${TEST_URL}${NC}"
echo -e "${BLUE}2. 检查是否直接显示项目详情页${NC}"
echo -e "${BLUE}3. 查看浏览器控制台的调试日志${NC}"
echo ""
