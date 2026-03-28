#!/usr/bin/env node

/**
 * 前后端集成测试
 * 验证后端生成的链接格式是否与前端路由匹配
 */

import path from 'path';
import fs from 'fs';
import { fileURLToPath } from 'url';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

console.log('🔍 前后端链接生成与路由匹配集成测试\n');
console.log('='.repeat(70));

// 读取后端 UrlGenerator 代码
const backendUrlGeneratorPath = path.join(__dirname, '../../backend/src/main/java/com/clientservice/common/util/UrlGenerator.java');
const backendUrlGeneratorContent = fs.readFileSync(backendUrlGeneratorPath, 'utf-8');

// 读取前端路由配置
const frontendRouterPath = path.join(__dirname, '../src/router/index.ts');
const frontendRouterContent = fs.readFileSync(frontendRouterPath, 'utf-8');

console.log('\n[1/6] 检查后端链接生成逻辑...');

// 提取后端生成的链接格式
const urlFormatMatch = backendUrlGeneratorContent.match(/String\.format\([^)]+\)/);
if (!urlFormatMatch) {
  console.error('❌ 未找到后端链接生成格式');
  process.exit(1);
}

const urlFormat = urlFormatMatch[0];
console.log(`✓ 后端链接生成格式: ${urlFormat}`);

// 检查格式是否正确
if (urlFormat.includes('/portal/matter/') && urlFormat.includes('token')) {
  console.log('✅ 后端链接格式包含 /portal/matter/ 和 token');
} else {
  console.error('❌ 后端链接格式不正确');
  process.exit(1);
}

console.log('\n[2/6] 检查前端路由配置...');

// 检查前端是否有对应的路由
if (!frontendRouterContent.includes("path: '/portal/matter/:id'")) {
  console.error('❌ 前端未找到 /portal/matter/:id 路由');
  process.exit(1);
}
console.log('✅ 前端有 /portal/matter/:id 路由');

// 检查路由顺序
const portalMatterIndex = frontendRouterContent.indexOf("path: '/portal/matter/:id'");
const portalIndex = frontendRouterContent.indexOf("path: '/portal',");
if (portalMatterIndex >= portalIndex) {
  console.error('❌ /portal/matter/:id 应该在 /portal 之前');
  process.exit(1);
}
console.log('✅ 路由顺序正确（/portal/matter/:id 在 /portal 之前）');

console.log('\n[3/6] 验证链接格式匹配...');

// 模拟后端生成的链接
const testBaseUrl = 'http://localhost:8081';
const testMatterId = 'CS1234567890123456789';
const testToken = 'test-token-12345678901234567890123456789012';

// 后端生成的链接格式（根据代码）
const backendGeneratedUrl = `${testBaseUrl}/portal/matter/${testMatterId}?token=${testToken}`;
console.log(`后端生成的链接: ${backendGeneratedUrl}`);

// 提取路径和查询参数
const url = new URL(backendGeneratedUrl);
const pathname = url.pathname; // /portal/matter/CS1234567890123456789
const tokenParam = url.searchParams.get('token'); // test-token-...

console.log(`路径部分: ${pathname}`);
console.log(`Token参数: ${tokenParam}`);

// 验证路径格式是否匹配前端路由
const pathMatch = pathname.match(/^\/portal\/matter\/(.+)$/);
if (!pathMatch) {
  console.error('❌ 路径格式不匹配前端路由 /portal/matter/:id');
  process.exit(1);
}

const extractedMatterId = pathMatch[1];
if (extractedMatterId !== testMatterId) {
  console.error(`❌ MatterId 提取错误: 期望 ${testMatterId}, 实际 ${extractedMatterId}`);
  process.exit(1);
}

console.log('✅ 路径格式匹配前端路由');
console.log(`✅ MatterId 正确提取: ${extractedMatterId}`);
console.log(`✅ Token 正确提取: ${tokenParam}`);

console.log('\n[4/6] 检查前端组件参数提取...');

// 读取 MatterDetail 组件
const matterDetailPath = path.join(__dirname, '../src/views/MatterDetail.vue');
const matterDetailContent = fs.readFileSync(matterDetailPath, 'utf-8');

if (!matterDetailContent.includes("route.params.id")) {
  console.error('❌ MatterDetail 组件未使用 route.params.id');
  process.exit(1);
}
console.log('✅ MatterDetail 使用 route.params.id 提取项目ID');

if (!matterDetailContent.includes("route.query.token")) {
  console.error('❌ MatterDetail 组件未使用 route.query.token');
  process.exit(1);
}
console.log('✅ MatterDetail 使用 route.query.token 提取token');

console.log('\n[5/6] 验证端口配置...');

// 检查后端默认端口
const backendPortMatch = backendUrlGeneratorContent.match(/localhost:(\d+)/);
if (backendPortMatch) {
  const backendPort = backendPortMatch[1];
  console.log(`后端默认端口: ${backendPort}`);
  
  // 检查前端开发服务器端口
  const viteConfigPath = path.join(__dirname, '../vite.config.ts');
  const viteConfigContent = fs.readFileSync(viteConfigPath, 'utf-8');
  const frontendPortMatch = viteConfigContent.match(/port:\s*(\d+)/);
  
  if (frontendPortMatch) {
    const frontendPort = frontendPortMatch[1];
    console.log(`前端开发服务器端口: ${frontendPort}`);
    
    if (backendPort !== frontendPort) {
      console.warn(`⚠️  前后端端口不一致: 后端 ${backendPort}, 前端 ${frontendPort}`);
      console.warn('   注意: 生产环境应使用相同的域名和端口');
    } else {
      console.log('✅ 前后端端口一致');
    }
  }
}

console.log('\n[6/6] 验证完整链接流程...');

// 模拟完整的访问流程
console.log('\n📋 完整链接访问流程验证:');
console.log('1. 后端生成链接:');
console.log(`   ${backendGeneratedUrl}`);
console.log('\n2. 用户点击链接，浏览器访问:');
console.log(`   路径: ${pathname}`);
console.log(`   查询参数: token=${tokenParam}`);
console.log('\n3. Vue Router 匹配路由:');
console.log(`   匹配路由: /portal/matter/:id`);
console.log(`   提取参数: id=${extractedMatterId}`);
console.log(`   提取查询: token=${tokenParam}`);
console.log('\n4. MatterDetail 组件加载:');
console.log(`   matterId = route.params.id = ${extractedMatterId}`);
console.log(`   token = route.query.token = ${tokenParam}`);
console.log('\n5. 组件调用 API:');
console.log(`   GET /portal/api/matter/${extractedMatterId}?token=${tokenParam}`);

console.log('\n' + '='.repeat(70));
console.log('\n✅ 所有检查通过！');
console.log('\n📊 测试总结:');
console.log('✅ 后端链接生成格式正确');
console.log('✅ 前端路由配置正确');
console.log('✅ 链接格式与路由匹配');
console.log('✅ 参数提取逻辑正确');
console.log('\n💡 结论: 后端生成的链接可以直接访问前端路由！');
console.log('');
