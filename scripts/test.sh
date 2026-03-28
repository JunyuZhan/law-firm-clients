#!/bin/bash

# 客户服务系统测试脚本
# 使用方法: ./scripts/test.sh [unit|integration|all]

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"
BACKEND_DIR="$PROJECT_DIR/backend"

# 颜色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 日志函数
log_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

log_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# 运行单元测试
run_unit_tests() {
    log_info "运行单元测试..."
    cd "$BACKEND_DIR"
    mvn test -Dtest='*Test' -DfailIfNoTests=false -DexcludedGroups=''
}

# 运行集成测试
run_integration_tests() {
    log_info "运行集成测试..."
    cd "$BACKEND_DIR"
    mvn test -Dtest='*IntegrationTest' -DfailIfNoTests=false
}

# 运行所有测试
run_all_tests() {
    log_info "运行所有测试..."
    cd "$BACKEND_DIR"
    mvn test
}

# 主函数
main() {
    case "${1:-all}" in
        unit)
            run_unit_tests
            ;;
        integration)
            run_integration_tests
            ;;
        all)
            run_all_tests
            ;;
        *)
            echo "使用方法: $0 [unit|integration|all]"
            echo ""
            echo "命令说明："
            echo "  unit        - 运行单元测试"
            echo "  integration - 运行集成测试"
            echo "  all         - 运行所有测试（默认）"
            exit 1
            ;;
    esac
}

main "$@"
