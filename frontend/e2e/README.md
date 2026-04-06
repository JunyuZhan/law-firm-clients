# Playwright E2E

## 当前范围

这套骨架目前只接入最小 smoke：

- `portal-smoke.spec.ts`
- `admin-shell.spec.ts`

它们的目标不是覆盖全部业务，而是确认：

- 前端可被 Playwright 正常拉起
- 公共配置加载不会阻塞页面渲染
- 后台登录态、壳层和主工作台页面可以通过 mock API 完成基础验收

## 运行方式

```bash
cd frontend

pnpm e2e:install
pnpm e2e:list
pnpm e2e
```

## 设计取舍

- 当前仓库尚未提供稳定的 E2E seed 数据，因此 smoke 用例优先使用 `page.route()` 拦截关键 API。
- 认证态通过写入 `sessionStorage` 模拟，保持与现有 `auth` store 一致。
- 后续扩展时，优先把更多页面 mock 收敛进 `e2e/fixtures.ts`，再逐步拆成按领域的 helper。
