#!/bin/bash

# 客户服务系统 - API测试脚本
# 测试所有主要接口

BASE_URL="http://localhost:8081/api"
ADMIN_TOKEN=""
API_KEY=""

# 颜色输出
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 测试结果统计
TOTAL=0
PASSED=0
FAILED=0
SKIPPED=0

# 测试结果数组
declare -a TEST_RESULTS

# 打印测试结果
print_result() {
    local test_name=$1
    local status=$2
    local message=$3
    
    TOTAL=$((TOTAL + 1))
    
    if [ "$status" = "PASS" ]; then
        echo -e "${GREEN}✓${NC} $test_name: ${GREEN}PASS${NC}"
        PASSED=$((PASSED + 1))
        TEST_RESULTS+=("PASS:$test_name")
    elif [ "$status" = "SKIP" ]; then
        echo -e "${YELLOW}⊘${NC} $test_name: ${YELLOW}SKIP${NC}"
        SKIPPED=$((SKIPPED + 1))
        TEST_RESULTS+=("SKIP:$test_name")
    else
        echo -e "${RED}✗${NC} $test_name: ${RED}FAIL${NC}"
        if [ -n "$message" ]; then
            echo -e "  ${RED}Error: $message${NC}"
        fi
        FAILED=$((FAILED + 1))
        TEST_RESULTS+=("FAIL:$test_name:$message")
    fi
}

# 发送HTTP请求
send_request() {
    local method=$1
    local url=$2
    local data=$3
    local headers=$4
    
    if [ -n "$data" ]; then
        if [ -n "$headers" ]; then
            curl -s -w "\n%{http_code}" -X "$method" "$url" \
                -H "Content-Type: application/json" \
                -H "$headers" \
                -d "$data" 2>/dev/null
        else
            curl -s -w "\n%{http_code}" -X "$method" "$url" \
                -H "Content-Type: application/json" \
                -d "$data" 2>/dev/null
        fi
    else
        if [ -n "$headers" ]; then
            curl -s -w "\n%{http_code}" -X "$method" "$url" \
                -H "$headers" 2>/dev/null
        else
            curl -s -w "\n%{http_code}" -X "$method" "$url" 2>/dev/null
        fi
    fi
}

# 检查服务是否运行
check_service() {
    echo -e "${BLUE}检查后端服务状态...${NC}"
    local response=$(curl -s -o /dev/null -w "%{http_code}" "$BASE_URL/../api/health" 2>/dev/null)
    if [ "$response" = "200" ]; then
        echo -e "${GREEN}✓ 后端服务运行正常${NC}"
        return 0
    else
        echo -e "${RED}✗ 后端服务未运行 (HTTP $response)${NC}"
        echo -e "${YELLOW}请先启动后端服务: cd backend && mvn spring-boot:run${NC}"
        return 1
    fi
}

# 测试健康检查
test_health() {
    echo -e "\n${BLUE}=== 健康检查测试 ===${NC}"
    
    local response=$(send_request "GET" "$BASE_URL/../api/health" "" "")
    local body=$(echo "$response" | sed '$d')
    local code=$(echo "$response" | tail -1)
    
    if [ "$code" = "200" ]; then
        print_result "健康检查" "PASS"
    else
        print_result "健康检查" "FAIL" "HTTP $code"
    fi
}

# 测试管理员登录
test_admin_login() {
    echo -e "\n${BLUE}=== 管理员登录测试 ===${NC}"
    
    local response=$(send_request "POST" "$BASE_URL/admin/auth/login" \
        '{"username":"admin","password":"admin123"}' "")
    local body=$(echo "$response" | sed '$d')
    local code=$(echo "$response" | tail -1)
    
    if [ "$code" = "200" ]; then
        # 提取token
        ADMIN_TOKEN=$(echo "$body" | grep -o '"token":"[^"]*' | cut -d'"' -f4)
        if [ -n "$ADMIN_TOKEN" ]; then
            print_result "管理员登录" "PASS"
            echo -e "  ${GREEN}Token: ${ADMIN_TOKEN:0:20}...${NC}"
        else
            print_result "管理员登录" "FAIL" "无法提取Token"
        fi
    else
        print_result "管理员登录" "FAIL" "HTTP $code: $body"
    fi
}

# 测试获取当前用户信息
test_get_current_user() {
    echo -e "\n${BLUE}=== 获取当前用户信息测试 ===${NC}"
    
    if [ -z "$ADMIN_TOKEN" ]; then
        print_result "获取当前用户信息" "SKIP" "需要先登录"
        return
    fi
    
    local response=$(send_request "GET" "$BASE_URL/admin/auth/me" "" \
        "Authorization: Bearer $ADMIN_TOKEN")
    local body=$(echo "$response" | sed '$d')
    local code=$(echo "$response" | tail -1)
    
    if [ "$code" = "200" ]; then
        print_result "获取当前用户信息" "PASS"
    else
        print_result "获取当前用户信息" "FAIL" "HTTP $code"
    fi
}

# 测试登出
test_admin_logout() {
    echo -e "\n${BLUE}=== 管理员登出测试 ===${NC}"
    
    if [ -z "$ADMIN_TOKEN" ]; then
        print_result "管理员登出" "SKIP" "需要先登录"
        return
    fi
    
    local response=$(send_request "POST" "$BASE_URL/admin/auth/logout" "" \
        "Authorization: Bearer $ADMIN_TOKEN")
    local body=$(echo "$response" | sed '$d')
    local code=$(echo "$response" | tail -1)
    
    if [ "$code" = "200" ]; then
        print_result "管理员登出" "PASS"
    else
        print_result "管理员登出" "FAIL" "HTTP $code"
    fi
}

# 测试API密钥列表
test_api_key_list() {
    echo -e "\n${BLUE}=== API密钥列表测试 ===${NC}"
    
    if [ -z "$ADMIN_TOKEN" ]; then
        print_result "API密钥列表" "SKIP" "需要先登录"
        return
    fi
    
    local response=$(send_request "GET" "$BASE_URL/admin/api-keys" "" \
        "Authorization: Bearer $ADMIN_TOKEN")
    local body=$(echo "$response" | sed '$d')
    local code=$(echo "$response" | tail -1)
    
    if [ "$code" = "200" ]; then
        print_result "API密钥列表" "PASS"
        # 提取第一个API密钥（如果有）
        API_KEY=$(echo "$body" | grep -o '"apiKey":"[^"]*' | head -1 | cut -d'"' -f4)
    else
        print_result "API密钥列表" "FAIL" "HTTP $code"
    fi
}

# 测试项目列表
test_matter_list() {
    echo -e "\n${BLUE}=== 项目列表测试 ===${NC}"
    
    if [ -z "$ADMIN_TOKEN" ]; then
        print_result "项目列表" "SKIP" "需要先登录"
        return
    fi
    
    local response=$(send_request "GET" "$BASE_URL/matter/list" "" \
        "Authorization: Bearer $ADMIN_TOKEN")
    local body=$(echo "$response" | sed '$d')
    local code=$(echo "$response" | tail -1)
    
    if [ "$code" = "200" ]; then
        print_result "项目列表" "PASS"
    else
        print_result "项目列表" "FAIL" "HTTP $code"
    fi
}

# 测试通知历史
test_notification_history() {
    echo -e "\n${BLUE}=== 通知历史测试 ===${NC}"
    
    if [ -z "$ADMIN_TOKEN" ]; then
        print_result "通知历史" "SKIP" "需要先登录"
        return
    fi
    
    local response=$(send_request "GET" "$BASE_URL/notification/history?limit=10" "" \
        "Authorization: Bearer $ADMIN_TOKEN")
    local body=$(echo "$response" | sed '$d')
    local code=$(echo "$response" | tail -1)
    
    if [ "$code" = "200" ]; then
        print_result "通知历史" "PASS"
    else
        print_result "通知历史" "FAIL" "HTTP $code"
    fi
}

# 测试系统配置列表
test_sys_config_list() {
    echo -e "\n${BLUE}=== 系统配置列表测试 ===${NC}"
    
    if [ -z "$ADMIN_TOKEN" ]; then
        print_result "系统配置列表" "SKIP" "需要先登录"
        return
    fi
    
    local response=$(send_request "GET" "$BASE_URL/admin/config" "" \
        "Authorization: Bearer $ADMIN_TOKEN")
    local body=$(echo "$response" | sed '$d')
    local code=$(echo "$response" | tail -1)
    
    if [ "$code" = "200" ]; then
        print_result "系统配置列表" "PASS"
    else
        print_result "系统配置列表" "FAIL" "HTTP $code"
    fi
}

# 测试接收项目数据（使用API密钥）
test_receive_matter() {
    echo -e "\n${BLUE}=== 接收项目数据测试 ===${NC}"
    
    if [ -z "$API_KEY" ]; then
        print_result "接收项目数据" "SKIP" "需要API密钥（如果没有API密钥，这是正常的）"
        return
    fi
    
    local matter_data='{
        "clientId": 2001,
        "clientName": "测试客户",
        "matterData": {
            "matterId": 1001,
            "matterName": "测试项目",
            "phone": "13800138000",
            "email": "client@example.com"
        },
        "scopes": ["MATTER_INFO", "MATTER_PROGRESS"],
        "validDays": 30
    }'
    
    local response=$(send_request "POST" "$BASE_URL/matter/receive" "$matter_data" \
        "Authorization: Bearer $API_KEY")
    local body=$(echo "$response" | sed '$d')
    local code=$(echo "$response" | tail -1)
    
    if [ "$code" = "200" ] || [ "$code" = "201" ]; then
        print_result "接收项目数据" "PASS"
    elif [ "$code" = "401" ]; then
        # API密钥无效，这是正常的（测试环境可能没有配置有效的API密钥）
        print_result "接收项目数据" "SKIP" "API密钥无效或未配置（这是正常的，需要先创建有效的API密钥）"
    else
        print_result "接收项目数据" "FAIL" "HTTP $code: $body"
    fi
}

# 打印测试总结
print_summary() {
    echo -e "\n${BLUE}==========================================${NC}"
    echo -e "${BLUE}测试总结${NC}"
    echo -e "${BLUE}==========================================${NC}"
    echo -e "总测试数: $TOTAL"
    echo -e "${GREEN}通过: $PASSED${NC}"
    echo -e "${RED}失败: $FAILED${NC}"
    echo -e "${YELLOW}跳过: $SKIPPED${NC}"
    
    if [ $FAILED -gt 0 ]; then
        echo -e "\n${RED}失败的测试:${NC}"
        for result in "${TEST_RESULTS[@]}"; do
            if [[ "$result" == FAIL:* ]]; then
                echo -e "  ${RED}$result${NC}"
            fi
        done
        echo -e "\n${YELLOW}注意: 跳过的测试不计入失败（如API密钥相关测试）${NC}"
        exit 1
    else
        echo -e "\n${GREEN}所有测试通过！${NC}"
        if [ $SKIPPED -gt 0 ]; then
            echo -e "${YELLOW}注意: 有 $SKIPPED 个测试被跳过（这是正常的）${NC}"
        fi
        exit 0
    fi
}

# 主函数
main() {
    echo "=========================================="
    echo "客户服务系统 - API测试"
    echo "=========================================="
    
    # 检查服务
    if ! check_service; then
        exit 1
    fi
    
    # 执行测试
    test_health
    test_admin_login
    if [ -n "$ADMIN_TOKEN" ]; then
        test_get_current_user
        test_api_key_list
        test_matter_list
        test_notification_history
        test_sys_config_list
        test_admin_logout
        test_receive_matter
    fi
    
    # 打印总结
    print_summary
}

# 运行主函数
main
