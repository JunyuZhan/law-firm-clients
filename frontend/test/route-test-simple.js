#!/usr/bin/env node

/**
 * 简单的路由测试脚本
 * 验证路由匹配逻辑，无需安装额外依赖
 */

import path from 'path';
import fs from 'fs';
import { fileURLToPath } from 'url';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

console.log('🔍 路由集成测试 - 验证 /portal/matter/:id 直接访问\n');
console.log('='.repeat(60));

// 读取路由配置文件
const routerPath = path.join(__dirname, '../src/router/index.ts');
const routerContent = fs.readFileSync(routerPath, 'utf-8');

console.log('\n[1/4] 检查路由配置文件...');
console.log(`✓ 路由文件: ${routerPath}`);

// 检查路由顺序
console.log('\n[2/4] 验证路由顺序...');
const portalMatterIndex = routerContent.indexOf("path: '/portal/matter/:id'");
const portalIndex = routerContent.indexOf("path: '/portal',");

if (portalMatterIndex === -1) {
  console.error('❌ 未找到 /portal/matter/:id 路由定义');
  process.exit(1);
}

if (portalIndex === -1) {
  console.error('❌ 未找到 /portal 路由定义');
  process.exit(1);
}

if (portalMatterIndex < portalIndex) {
  console.log('✅ /portal/matter/:id 在 /portal 之前（正确顺序）');
} else {
  console.error('❌ /portal/matter/:id 应该在 /portal 之前');
  console.error('   当前顺序可能导致路由匹配错误');
  process.exit(1);
}

// 检查路由名称
console.log('\n[3/4] 验证路由配置...');
if (routerContent.includes("name: 'PortalMatterDetail'")) {
  console.log('✅ 路由名称正确: PortalMatterDetail');
} else {
  console.error('❌ 未找到路由名称 PortalMatterDetail');
  process.exit(1);
}

if (routerContent.includes("component: () => import('@/views/MatterDetail.vue')")) {
  console.log('✅ 组件引用正确: MatterDetail.vue');
} else {
  console.error('❌ 组件引用可能不正确');
}

// 检查 MatterDetail 组件中的参数提取
console.log('\n[4/4] 验证组件参数提取...');
const matterDetailPath = path.join(__dirname, '../src/views/MatterDetail.vue');
const matterDetailContent = fs.readFileSync(matterDetailPath, 'utf-8');

if (matterDetailContent.includes("route.params.id")) {
  console.log('✅ MatterDetail 组件使用 route.params.id 获取项目ID');
} else {
  console.error('❌ MatterDetail 组件未找到 route.params.id');
  process.exit(1);
}

if (matterDetailContent.includes("route.query.token")) {
  console.log('✅ MatterDetail 组件使用 route.query.token 获取token');
} else {
  console.error('❌ MatterDetail 组件未找到 route.query.token');
  process.exit(1);
}

if (matterDetailContent.includes("onMounted")) {
  console.log('✅ MatterDetail 组件在 onMounted 时加载数据');
} else {
  console.warn('⚠️  MatterDetail 组件可能未在 onMounted 时加载数据');
}

// 检查调试日志
if (routerContent.includes("console.log('[Router] 匹配到项目详情路由'")) {
  console.log('✅ 路由守卫包含调试日志');
} else {
  console.warn('⚠️  路由守卫可能缺少调试日志');
}

if (matterDetailContent.includes("console.log('[MatterDetail]")) {
  console.log('✅ MatterDetail 组件包含调试日志');
} else {
  console.warn('⚠️  MatterDetail 组件可能缺少调试日志');
}

console.log('\n' + '='.repeat(60));
console.log('\n✅ 所有静态检查通过！');
console.log('\n📋 下一步测试步骤:');
console.log('1. 启动前端服务: npm run dev');
console.log('2. 获取一个真实的项目访问链接（从后端API）');
console.log('3. 在浏览器中直接访问该链接');
console.log('4. 检查是否直接显示项目详情页（不是 Portal 首页）');
console.log('5. 打开浏览器控制台（F12），查看路由匹配日志');
console.log('\n💡 测试链接格式: http://localhost:3000/portal/matter/{matterId}?token={token}');
console.log('');
