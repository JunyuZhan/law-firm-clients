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
**Category:** Analytics Dashboard · **UI reference:** Stripe-inspired

---

## Global Rules

### Color Palette

| Role | Hex | CSS Variable (runtime) |
|------|-----|-------------------------|
| Primary (links / key UI) | `#1E40AF` | `--lex-primary-soft`, Ant `colorPrimary` |
| Secondary | `#3B82F6` | Ant `colorInfo` |
| CTA / primary actions | `#F59E0B` | `--lex-accent`, `.ant-btn-primary` |
| Page background | `#F8FAFC` | `--lex-bg` |
| Heading / emphasis text | `#1E3A8A` | `--lex-primary` |
| Body text | `#0F172A` | `--lex-text` |
| Border | `#E2E8F0` | `--lex-outline` |

**Color Notes:** Blue for brand + focus；琥珀用于主按钮与强调（Stripe 式清晰层级，非 Stripe 紫）。

### Typography

- **Heading Font:** Fira Sans（字重 600，略紧字距）
- **Body Font:** Fira Sans
- **Monospace:** Fira Code（代码、等宽数据）
- **Mood:** dashboard, data, B2B, trustworthy, precise
- **Google Fonts:** [Fira Code + Fira Sans](https://fonts.google.com/share?selection.family=Fira+Code:wght@400;500;600;700|Fira+Sans:wght@300;400;500;600;700)

**CSS Import:**
```css
@import url('https://fonts.googleapis.com/css2?family=Fira+Code:wght@400;500;600;700&family=Fira+Sans:wght@300;400;500;600;700&display=swap');
```

### Spacing Variables

| Token | Value | Usage |
|-------|-------|-------|
| `--space-xs` | `4px` / `0.25rem` | Tight gaps |
| `--space-sm` | `8px` / `0.5rem` | Icon gaps, inline spacing |
| `--space-md` | `16px` / `1rem` | Standard padding |
| `--space-lg` | `24px` / `1.5rem` | Section padding |
| `--space-xl` | `32px` / `2rem` | Large gaps |
| `--space-2xl` | `48px` / `3rem` | Section margins |
| `--space-3xl` | `64px` / `4rem` | Hero padding |

### Shadow Depths

| Level | Value | Usage |
|-------|-------|-------|
| `--shadow-xs` | `0 1px 2px rgba(15,23,42,0.04)` | 微抬起 |
| `--shadow-sm` | 见 `theme.css` `--lex-panel-shadow` | 卡片、面板 |
| `--shadow-md` | `0 8px 24px rgba(15,23,42,0.08)` | 悬停、下拉 |
| `--shadow-lg` | `0 20px 40px rgba(15,23,42,0.1)` | 模态、强调浮层 |

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
