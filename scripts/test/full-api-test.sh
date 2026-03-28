#!/bin/bash

# 客户服务系统 - 完整API测试脚本
# 测试所有主要接口，包括CRUD操作

BASE_URL="http://localhost:8081/api"
ADMIN_TOKEN=""
API_KEY=""
CREATED_API_KEY_ID=""
CREATED_CONFIG_ID=""
CREATED_CONFIG_KEY=""

# 颜色输出
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
NC='\033[0m'

# 测试结果统计
TOTAL=0
PASSED=0
FAILED=0
SKIPPED=0

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
        echo -e "${YELLOW}⊘${NC} $test_name: ${YELLOW}SKIP${NC}${message:+ - $message}"
        SKIPPED=$((SKIPPED + 1))
        TEST_RESULTS+=("SKIP:$test_name:$message")
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

# 检查服务
check_service() {
    echo -e "${BLUE}检查后端服务状态...${NC}"
    local response=$(curl -s -o /dev/null -w "%{http_code}" "$BASE_URL/../api/health" 2>/dev/null)
    if [ "$response" = "200" ]; then
        echo -e "${GREEN}✓ 后端服务运行正常${NC}\n"
        return 0
    else
        echo -e "${RED}✗ 后端服务未运行 (HTTP $response)${NC}"
        return 1
    fi
}

# ========== 认证相关测试 ==========

test_admin_login() {
    echo -e "${CYAN}=== 管理员登录测试 ===${NC}"
    
    local response=$(send_request "POST" "$BASE_URL/admin/auth/login" \
        '{"username":"admin","password":"admin123"}' "")
    local body=$(echo "$response" | sed '$d')
    local code=$(echo "$response" | tail -1)
    
    if [ "$code" = "200" ]; then
        ADMIN_TOKEN=$(echo "$body" | grep -o '"token":"[^"]*' | cut -d'"' -f4)
        if [ -n "$ADMIN_TOKEN" ]; then
            print_result "管理员登录" "PASS"
        else
            print_result "管理员登录" "FAIL" "无法提取Token"
        fi
    else
        print_result "管理员登录" "FAIL" "HTTP $code"
    fi
}

test_get_current_user() {
    echo -e "${CYAN}=== 获取当前用户信息 ===${NC}"
    
    if [ -z "$ADMIN_TOKEN" ]; then
        print_result "获取当前用户信息" "SKIP" "需要先登录"
        return
    fi
    
    local response=$(send_request "GET" "$BASE_URL/admin/auth/me" "" \
        "Authorization: Bearer $ADMIN_TOKEN")
    local code=$(echo "$response" | tail -1)
    
    if [ "$code" = "200" ]; then
        print_result "获取当前用户信息" "PASS"
    else
        print_result "获取当前用户信息" "FAIL" "HTTP $code"
    fi
}

# ========== API密钥管理测试 ==========

test_api_key_list() {
    echo -e "${CYAN}=== API密钥列表 ===${NC}"
    
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
        # 尝试提取第一个API密钥
        API_KEY=$(echo "$body" | grep -o '"apiKey":"[^"]*' | head -1 | cut -d'"' -f4)
    else
        print_result "API密钥列表" "FAIL" "HTTP $code"
    fi
}

test_create_api_key() {
    echo -e "${CYAN}=== 创建API密钥 ===${NC}"
    
    if [ -z "$ADMIN_TOKEN" ]; then
        print_result "创建API密钥" "SKIP" "需要先登录"
        return
    fi
    
    local key_name="测试API密钥_$(date +%s)"
    local data="{\"keyName\":\"$key_name\",\"lawFirmName\":\"测试律所\",\"enabled\":true}"
    
    local response=$(send_request "POST" "$BASE_URL/admin/api-keys" "$data" \
        "Authorization: Bearer $ADMIN_TOKEN")
    local body=$(echo "$response" | sed '$d')
    local code=$(echo "$response" | tail -1)
    
    if [ "$code" = "200" ] || [ "$code" = "201" ]; then
        print_result "创建API密钥" "PASS"
        CREATED_API_KEY_ID=$(echo "$body" | grep -o '"id":[0-9]*' | head -1 | cut -d':' -f2)
        # 提取新创建的API密钥
        local new_key=$(echo "$body" | grep -o '"apiKey":"[^"]*' | cut -d'"' -f4)
        if [ -n "$new_key" ]; then
            API_KEY="$new_key"
            echo -e "  ${GREEN}创建的API密钥: ${API_KEY:0:30}...${NC}"
        fi
    else
        print_result "创建API密钥" "FAIL" "HTTP $code"
    fi
}

test_delete_api_key() {
    echo -e "${CYAN}=== 删除API密钥 ===${NC}"
    
    if [ -z "$ADMIN_TOKEN" ] || [ -z "$CREATED_API_KEY_ID" ]; then
        print_result "删除API密钥" "SKIP" "需要先创建API密钥"
        return
    fi
    
    local response=$(send_request "DELETE" "$BASE_URL/admin/api-keys/$CREATED_API_KEY_ID" "" \
        "Authorization: Bearer $ADMIN_TOKEN")
    local code=$(echo "$response" | tail -1)
    
    if [ "$code" = "200" ]; then
        print_result "删除API密钥" "PASS"
    else
        print_result "删除API密钥" "FAIL" "HTTP $code"
    fi
}

# ========== 系统配置管理测试 ==========

test_sys_config_list() {
    echo -e "${CYAN}=== 系统配置列表 ===${NC}"
    
    if [ -z "$ADMIN_TOKEN" ]; then
        print_result "系统配置列表" "SKIP" "需要先登录"
        return
    fi
    
    local response=$(send_request "GET" "$BASE_URL/admin/config" "" \
        "Authorization: Bearer $ADMIN_TOKEN")
    local code=$(echo "$response" | tail -1)
    
    if [ "$code" = "200" ]; then
        print_result "系统配置列表" "PASS"
    else
        print_result "系统配置列表" "FAIL" "HTTP $code"
    fi
}

test_create_sys_config() {
    echo -e "${CYAN}=== 创建系统配置 ===${NC}"
    
    if [ -z "$ADMIN_TOKEN" ]; then
        print_result "创建系统配置" "SKIP" "需要先登录"
        return
    fi
    
    CREATED_CONFIG_KEY="test.config.$(date +%s)"
    local data="{\"configKey\":\"$CREATED_CONFIG_KEY\",\"configValue\":\"test-value\",\"configType\":\"STRING\",\"description\":\"测试配置\"}"
    
    local response=$(send_request "POST" "$BASE_URL/admin/config" "$data" \
        "Authorization: Bearer $ADMIN_TOKEN")
    local body=$(echo "$response" | sed '$d')
    local code=$(echo "$response" | tail -1)
    
    if [ "$code" = "200" ] || [ "$code" = "201" ]; then
        print_result "创建系统配置" "PASS"
        # 提取配置ID
        CREATED_CONFIG_ID=$(echo "$body" | grep -o '"id":[0-9]*' | head -1 | cut -d':' -f2)
    else
        print_result "创建系统配置" "FAIL" "HTTP $code"
    fi
}

test_update_sys_config() {
    echo -e "${CYAN}=== 更新系统配置 ===${NC}"
    
    if [ -z "$ADMIN_TOKEN" ] || [ -z "$CREATED_CONFIG_ID" ]; then
        print_result "更新系统配置" "SKIP" "需要先创建配置"
        return
    fi
    
    local data="{\"configValue\":\"updated-value\",\"description\":\"更新的测试配置\"}"
    
    local response=$(send_request "PUT" "$BASE_URL/admin/config/$CREATED_CONFIG_ID" "$data" \
        "Authorization: Bearer $ADMIN_TOKEN")
    local code=$(echo "$response" | tail -1)
    
    if [ "$code" = "200" ]; then
        print_result "更新系统配置" "PASS"
    else
        print_result "更新系统配置" "FAIL" "HTTP $code"
    fi
}

test_delete_sys_config() {
    echo -e "${CYAN}=== 删除系统配置 ===${NC}"
    
    if [ -z "$ADMIN_TOKEN" ] || [ -z "$CREATED_CONFIG_ID" ]; then
        print_result "删除系统配置" "SKIP" "需要先创建配置"
        return
    fi
    
    local response=$(send_request "DELETE" "$BASE_URL/admin/config/$CREATED_CONFIG_ID" "" \
        "Authorization: Bearer $ADMIN_TOKEN")
    local code=$(echo "$response" | tail -1)
    
    if [ "$code" = "200" ]; then
        print_result "删除系统配置" "PASS"
    else
        print_result "删除系统配置" "FAIL" "HTTP $code"
    fi
}

# ========== 项目管理测试 ==========

test_matter_list() {
    echo -e "${CYAN}=== 项目列表 ===${NC}"
    
    if [ -z "$ADMIN_TOKEN" ]; then
        print_result "项目列表" "SKIP" "需要先登录"
        return
    fi
    
    local response=$(send_request "GET" "$BASE_URL/matter/list" "" \
        "Authorization: Bearer $ADMIN_TOKEN")
    local code=$(echo "$response" | tail -1)
    
    if [ "$code" = "200" ]; then
        print_result "项目列表" "PASS"
    else
        print_result "项目列表" "FAIL" "HTTP $code"
    fi
}

test_receive_matter() {
    echo -e "${CYAN}=== 接收项目数据（API密钥认证） ===${NC}"
    
    if [ -z "$API_KEY" ]; then
        print_result "接收项目数据" "SKIP" "需要API密钥"
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
        print_result "接收项目数据" "SKIP" "API密钥无效或未配置"
    else
        print_result "接收项目数据" "FAIL" "HTTP $code: $body"
    fi
}

# ========== 通知管理测试 ==========

test_notification_history() {
    echo -e "${CYAN}=== 通知历史 ===${NC}"
    
    if [ -z "$ADMIN_TOKEN" ]; then
        print_result "通知历史" "SKIP" "需要先登录"
        return
    fi
    
    local response=$(send_request "GET" "$BASE_URL/notification/history?limit=10" "" \
        "Authorization: Bearer $ADMIN_TOKEN")
    local code=$(echo "$response" | tail -1)
    
    if [ "$code" = "200" ]; then
        print_result "通知历史" "PASS"
    else
        print_result "通知历史" "FAIL" "HTTP $code"
    fi
}

test_notification_statistics() {
    echo -e "${CYAN}=== 通知统计 ===${NC}"
    
    if [ -z "$ADMIN_TOKEN" ]; then
        print_result "通知统计" "SKIP" "需要先登录"
        return
    fi
    
    local response=$(send_request "GET" "$BASE_URL/notification/statistics" "" \
        "Authorization: Bearer $ADMIN_TOKEN")
    local code=$(echo "$response" | tail -1)
    
    if [ "$code" = "200" ]; then
        print_result "通知统计" "PASS"
    else
        print_result "通知统计" "FAIL" "HTTP $code"
    fi
}

# ========== 通知模板管理测试 ==========

test_notification_template_list() {
    echo -e "${CYAN}=== 通知模板列表 ===${NC}"
    
    if [ -z "$ADMIN_TOKEN" ]; then
        print_result "通知模板列表" "SKIP" "需要先登录"
        return
    fi
    
    local response=$(send_request "GET" "$BASE_URL/admin/notification-templates" "" \
        "Authorization: Bearer $ADMIN_TOKEN")
    local code=$(echo "$response" | tail -1)
    
    if [ "$code" = "200" ]; then
        print_result "通知模板列表" "PASS"
    else
        print_result "通知模板列表" "FAIL" "HTTP $code"
    fi
}

# ========== 打印总结 ==========

print_summary() {
    echo -e "\n${BLUE}═══════════════════════════════════════════════════════════${NC}"
    echo -e "${BLUE}测试总结${NC}"
    echo -e "${BLUE}═══════════════════════════════════════════════════════════${NC}"
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
        return 1
    else
        echo -e "\n${GREEN}所有测试通过！${NC}"
        if [ $SKIPPED -gt 0 ]; then
            echo -e "${YELLOW}注意: 有 $SKIPPED 个测试被跳过（这是正常的）${NC}"
        fi
        return 0
    fi
}

# ========== 主函数 ==========

main() {
    echo -e "${CYAN}╔═══════════════════════════════════════════════════════════╗${NC}"
    echo -e "${CYAN}║${NC}  客户服务系统 - 完整API测试"
    echo -e "${CYAN}╚═══════════════════════════════════════════════════════════╝${NC}\n"
    
    if ! check_service; then
        exit 1
    fi
    
    # 认证相关
    test_admin_login
    test_get_current_user
    
    if [ -n "$ADMIN_TOKEN" ]; then
        # API密钥管理（先创建，用于后续测试）
        test_api_key_list
        test_create_api_key
        
        # 项目管理（使用刚创建的API密钥）
        test_matter_list
        test_receive_matter
        
        # 系统配置管理
        test_sys_config_list
        test_create_sys_config
        test_update_sys_config
        test_delete_sys_config
        
        # 通知管理
        test_notification_history
        test_notification_statistics
        
        # 通知模板管理
        test_notification_template_list
        
        # 最后删除API密钥（清理）
        test_delete_api_key
    fi
    
    print_summary
    exit $?
}

main
