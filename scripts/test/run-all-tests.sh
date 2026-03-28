#!/bin/bash

# 客户服务系统 - 测试运行脚本
# 统一入口，运行所有测试

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(cd "$SCRIPT_DIR/../.." && pwd)"

# 颜色输出
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
NC='\033[0m'

# 测试结果统计
TOTAL_TESTS=0
TOTAL_PASSED=0
TOTAL_FAILED=0

# 打印标题
print_header() {
    echo -e "\n${CYAN}╔═══════════════════════════════════════════════════════════╗${NC}"
    echo -e "${CYAN}║${NC}  客户服务系统 - 测试套件"
    echo -e "${CYAN}╚═══════════════════════════════════════════════════════════╝${NC}\n"
}

# 打印章节
print_section() {
    echo -e "\n${BLUE}═══════════════════════════════════════════════════════════${NC}"
    echo -e "${BLUE}$1${NC}"
    echo -e "${BLUE}═══════════════════════════════════════════════════════════${NC}"
}

# 运行测试脚本
run_test_script() {
    local script_name=$1
    local script_path="$SCRIPT_DIR/$script_name"
    
    if [ ! -f "$script_path" ]; then
        echo -e "${RED}✗ 测试脚本不存在: $script_name${NC}"
        return 1
    fi
    
    if [ ! -x "$script_path" ]; then
        chmod +x "$script_path"
    fi
    
    echo -e "\n${YELLOW}运行测试: $script_name${NC}"
    "$script_path"
    local exit_code=$?
    
    if [ $exit_code -eq 0 ]; then
        echo -e "${GREEN}✓ $script_name 测试通过${NC}"
        TOTAL_PASSED=$((TOTAL_PASSED + 1))
    else
        echo -e "${RED}✗ $script_name 测试失败 (退出码: $exit_code)${NC}"
        TOTAL_FAILED=$((TOTAL_FAILED + 1))
    fi
    
    TOTAL_TESTS=$((TOTAL_TESTS + 1))
    return $exit_code
}

# 快速测试（API基础测试）
run_quick_tests() {
    print_section "快速测试 - API 基础功能"
    echo -e "${YELLOW}说明: 测试认证、基础API，约1-2分钟${NC}"
    
    run_test_script "api-test.sh"
}

# 完整API测试
run_full_api_tests() {
    print_section "完整API测试 - 包含CRUD操作"
    echo -e "${YELLOW}说明: 测试所有API功能，包括创建、更新、删除操作，约2-3分钟${NC}"
    
    run_test_script "full-api-test.sh"
}

# 单元测试（Maven）
run_unit_tests() {
    print_section "单元测试 - Maven Test"
    echo -e "${YELLOW}说明: 运行所有单元测试${NC}"
    
    cd "$PROJECT_DIR/backend"
    if mvn test -Dtest='*Test' -DfailIfNoTests=false 2>&1 | tee /tmp/maven-test.log; then
        echo -e "${GREEN}✓ 单元测试通过${NC}"
        TOTAL_PASSED=$((TOTAL_PASSED + 1))
    else
        echo -e "${RED}✗ 单元测试失败${NC}"
        TOTAL_FAILED=$((TOTAL_FAILED + 1))
    fi
    TOTAL_TESTS=$((TOTAL_TESTS + 1))
}

# 集成测试（Maven）
run_integration_tests() {
    print_section "集成测试 - Maven Integration Test"
    echo -e "${YELLOW}说明: 运行所有集成测试${NC}"
    
    cd "$PROJECT_DIR/backend"
    if mvn test -Dtest='*IntegrationTest' -DfailIfNoTests=false 2>&1 | tee /tmp/maven-integration-test.log; then
        echo -e "${GREEN}✓ 集成测试通过${NC}"
        TOTAL_PASSED=$((TOTAL_PASSED + 1))
    else
        echo -e "${RED}✗ 集成测试失败${NC}"
        TOTAL_FAILED=$((TOTAL_FAILED + 1))
    fi
    TOTAL_TESTS=$((TOTAL_TESTS + 1))
}

# 完整测试套件
run_full_tests() {
    print_section "完整测试套件"
    echo -e "${YELLOW}说明: 运行所有测试（单元测试 + 集成测试 + API测试）${NC}"
    
    run_unit_tests
    run_integration_tests
    run_quick_tests
}

# 打印总结
print_summary() {
    echo -e "\n${CYAN}╔═══════════════════════════════════════════════════════════╗${NC}"
    echo -e "${CYAN}║${NC}  测试总结"
    echo -e "${CYAN}╠═══════════════════════════════════════════════════════════╣${NC}"
    echo -e "${CYAN}║${NC}  总测试套件数: $TOTAL_TESTS"
    echo -e "${CYAN}║${NC}  ${GREEN}通过: $TOTAL_PASSED${NC}"
    echo -e "${CYAN}║${NC}  ${RED}失败: $TOTAL_FAILED${NC}"
    echo -e "${CYAN}╚═══════════════════════════════════════════════════════════╝${NC}"
    
    if [ $TOTAL_FAILED -gt 0 ]; then
        echo -e "\n${RED}部分测试失败，请检查日志${NC}"
        exit 1
    else
        echo -e "\n${GREEN}所有测试通过！${NC}"
        exit 0
    fi
}

# 显示帮助信息
show_help() {
    echo "客户服务系统 - 测试运行脚本"
    echo ""
    echo "使用方法: $0 [选项]"
    echo ""
    echo "选项:"
    echo "  quick       - 快速测试（API基础测试，推荐）"
    echo "  unit        - 单元测试（Maven）"
    echo "  integration - 集成测试（Maven）"
    echo "  api         - API测试（同quick）"
    echo "  full-api    - 完整API测试（包含CRUD操作）"
    echo "  full        - 完整测试套件（所有测试）"
    echo "  help        - 显示此帮助信息"
    echo ""
    echo "示例:"
    echo "  $0 quick        # 快速测试"
    echo "  $0 full-api     # 完整API测试"
    echo "  $0 full         # 完整测试套件"
}

# 主函数
main() {
    print_header
    
    case "${1:-quick}" in
        quick)
            run_quick_tests
            ;;
        unit)
            run_unit_tests
            ;;
        integration)
            run_integration_tests
            ;;
        api)
            run_quick_tests
            ;;
        full-api)
            run_full_api_tests
            ;;
        full)
            run_full_tests
            ;;
        help|--help|-h)
            show_help
            exit 0
            ;;
        *)
            echo -e "${RED}未知选项: $1${NC}"
            show_help
            exit 1
            ;;
    esac
    
    print_summary
}

# 运行主函数
main "$@"
