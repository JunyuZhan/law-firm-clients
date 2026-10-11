# Design System Master File

> **LOGIC:** When building a specific page, first check `design-system/pages/[page-name].md`.
> If that file exists, its rules **override** this Master file.
> If not, strictly follow the rules below.

> **Runtime source of truth:** `frontend/src/styles/theme.css` (`:root` / `--lex-*`) and `frontend/src/App.vue` (`ConfigProvider` theme).  
> **改版说明:** 主参考 Stripe（气质与信息层级），品牌色为下表；详见 `REDESIGN-SPEC.md`。  
> **阶段 3**（按页改造）已在实现侧 **完成**，SPEC 文首状态为验收结论。

---

**Project:** Law Firm Clients  
**Generated:** 2026-03-30 22:18:49 · **Tokens synced:** 2026-04-09 · **阶段 4 闸门：** 本地 typecheck / vitest / eslint / playwright smoke 已通过（见 `REDESIGN-SPEC.md` 文首）  
**Category:** Analytics Dashboard · **UI reference:** Lex Elite（典雅法律风）

---

## Global Rules

### Color Palette（Lex Elite）

| Role | Hex | CSS Variable (runtime) |
|------|-----|-------------------------|
| Primary（主色，链接/关键 UI） | `#1a2b4a` | `--le-navy-800`, `--lex-primary`, Ant `colorPrimary` |
| Primary hover | `#24395e` | `--le-navy-700` |
| Secondary / info | `#2f4a75` | `--le-navy-600`, Ant `colorInfo` |
| Accent / 点缀（金色） | `#c9a961` | `--le-gold`, `--lex-accent` |
| Accent deep（金色悬停） | `#a8873f` | `--le-gold-deep` |
| Accent soft（深底金文字） | `#e0c485` | `--le-gold-soft` |
| Page background | `#f8f6f1` | `--le-cream`, `--lex-bg` |
| Card surface | `#ffffff` | `--le-paper`, `--lex-surface-strong` |
| Heading / emphasis text | `#1a2b4a` | `--lex-primary` |
| Body text | `#1a2330` | `--le-ink`, `--lex-text` |
| Muted text | `#6b7787` | `--le-muted` |
| Border | `#e4dfd2` | `--le-line`, `--lex-outline` |
| Sidebar / deep surface | `#0d1830` | `--le-navy-900`, Ant `Layout.siderBg` |

**Color Notes:** 深藏青 `#1a2b4a` 系为品牌主色（庄重、可信），金色 `#c9a961` 作点缀与强调（分隔线、选中态、点缀描边）。主按钮为深藏青底 + 金色悬停描边。状态色：success `#2d6a4f` / warning `#9c6b1a` / error `#a61e2d`。

> **Token 单一来源：** `frontend/src/styles/lex-elite.css`（`--le-*` 基础 token）；语义层由 `theme.css` 衍生为 `--lex-*`；Ant 组件 token 见 `App.vue`。

### Typography

- **Heading Font:** Georgia / Times New Roman / Songti SC（衬线，庄重；`--le-font-serif`）
- **Body Font:** Helvetica Neue / Arial / PingFang SC（无衬线；`--le-font-sans`）
- **Monospace:** SFMono-Regular / JetBrains Mono（`--le-font-mono`）
- **Mood:** legal, elegant, trustworthy, precise

**CSS Import:** 无需外部字体（使用系统衬线与无衬线栈，避免离线环境阻塞页面加载）。

### Radius（圆角，典雅偏方正）

| Token | Value |
|-------|-------|
| `--le-radius-xs` | `2px` |
| `--le-radius-sm` | `4px` |
| `--le-radius-md` | `6px` |
| `--le-radius-lg` | `8px` |

### Spacing Variables

| Token | Value | Usage |
|-------|-------|-------|
| `--le-space-xs` | `4px` / `0.25rem` | Tight gaps |
| `--le-space-sm` | `8px` / `0.5rem` | Icon gaps, inline spacing |
| `--le-space-md` | `16px` / `1rem` | Standard padding |
| `--le-space-lg` | `24px` / `1.5rem` | Section padding |
| `--le-space-xl` | `32px` / `2rem` | Large gaps |
| `--le-space-2xl` | `48px` / `3rem` | Section margins |
| `--le-space-3xl` | `64px` / `4rem` | Hero padding |

### Shadow Depths

| Level | Value | Usage |
|-------|-------|-------|
| `--le-shadow-xs` | `0 1px 2px rgba(13,24,48,0.05)` | 微抬起 |
| `--le-shadow-sm` | `0 2px 8px rgba(13,24,48,0.07)` | 卡片、面板 |
| `--le-shadow-md` | `0 10px 30px rgba(13,24,48,0.08)` | 悬停、下拉 |
| `--le-shadow-lg` | `0 24px 48px rgba(13,24,48,0.14)` | 模态、强调浮层 |

---

## Component Specs

### Buttons

```css
/* Primary Button */
.btn-primary {
  background: #F59E0B;
  color: white;
  padding: 12px 24px;
  border-radius: 8px;
  font-weight: 600;
  transition: all 200ms ease;
  cursor: pointer;
}

.btn-primary:hover {
  opacity: 0.9;
  transform: translateY(-1px);
}

/* Secondary Button */
.btn-secondary {
  background: transparent;
  color: #1E40AF;
  border: 2px solid #1E40AF;
  padding: 12px 24px;
  border-radius: 8px;
  font-weight: 600;
  transition: all 200ms ease;
  cursor: pointer;
}
```

### Cards

```css
.card {
  background: #F8FAFC;
  border-radius: 12px;
  padding: 24px;
  box-shadow: var(--shadow-md);
  transition: all 200ms ease;
  cursor: pointer;
}

.card:hover {
  box-shadow: var(--shadow-lg);
  transform: translateY(-2px);
}
```

### Inputs

```css
.input {
  padding: 12px 16px;
  border: 1px solid #E2E8F0;
  border-radius: 8px;
  font-size: 16px;
  transition: border-color 200ms ease;
}

.input:focus {
  border-color: #1E40AF;
  outline: none;
  box-shadow: 0 0 0 3px #1E40AF20;
}
```

### Modals

```css
.modal-overlay {
  background: rgba(0, 0, 0, 0.5);
  backdrop-filter: blur(4px);
}

.modal {
  background: white;
  border-radius: 16px;
  padding: 32px;
  box-shadow: var(--shadow-xl);
  max-width: 500px;
  width: 90%;
}
```

---

## Style Guidelines

**Style:** Data-Dense Dashboard

**Keywords:** Multiple charts/widgets, data tables, KPI cards, minimal padding, grid layout, space-efficient, maximum data visibility

**Best For:** Business intelligence dashboards, financial analytics, enterprise reporting, operational dashboards, data warehousing

**Key Effects:** Hover tooltips, chart zoom on click, row highlighting on hover, smooth filter animations, data loading spinners

### Page Pattern

- **客户门户（公开页）：** 短标题、少分区、避免英文装饰词与重复说明；详见 `REDESIGN-SPEC.md` 阶段 3。
- **门户列表页：** `AppHeader` 已显示页名（如「我的项目」「文件中心」）时，内容区 **不再加** 同级列表标题（如「项目列表」「文件列表」的 h2），避免与顶栏重复。
- **律所 Admin：** 见下文「文案与信息层级」。

### 文案与信息层级（律所 Admin）

- **顶栏标题：** `AdminLayout` 已显示当前页标题时，内容区 **不重复** 同级页面主标题；`page-intro` 用 **一句** `intro-text` + 右侧操作区即可。
- **统计卡（`stats-grid` 等）：** 以 **标签 + 数字** 为主；第三行说明 **省略**（不写与表格重复的「解释性」段落）。
- **筛选 / 表格区（`panel-head`）：** 保留区块标题；右侧长说明 **改为一句或删除**，让表单与表格优先。
- **表格摘要：** 一行即可（如「共 n 条」），不写业务方法论。
- **技术说明**（API、模板变量、回调地址等）：用 **短列表** 或 `Descriptions`，避免大段散文。

---

## Anti-Patterns (Do NOT Use)

- ❌ Ornate design
- ❌ No filtering

### Additional Forbidden Patterns

- ❌ **Emojis as icons** — Use SVG icons (Heroicons, Lucide, Simple Icons)
- ❌ **Missing cursor:pointer** — All clickable elements must have cursor:pointer
- ❌ **Layout-shifting hovers** — Avoid scale transforms that shift layout
- ❌ **Low contrast text** — Maintain 4.5:1 minimum contrast ratio
- ❌ **Instant state changes** — Always use transitions (150-300ms)
- ❌ **Invisible focus states** — Focus states must be visible for a11y

---

## Pre-Delivery Checklist

Before delivering any UI code, verify:

- [ ] No emojis used as icons (use SVG instead)
- [ ] All icons from consistent icon set (Heroicons/Lucide)
- [ ] `cursor-pointer` on all clickable elements
- [ ] Hover states with smooth transitions (150-300ms)
- [ ] Light mode: text contrast 4.5:1 minimum
- [ ] Focus states visible for keyboard navigation
- [ ] `prefers-reduced-motion` respected
- [ ] Responsive: 375px, 768px, 1024px, 1440px
- [ ] No content hidden behind fixed navbars
- [ ] No horizontal scroll on mobile
