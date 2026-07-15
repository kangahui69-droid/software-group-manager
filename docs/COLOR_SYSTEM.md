# 软件小组管理系统 - 配色方案文档

## 概述

基于 **Tabler Admin Template (Bootstrap 5)** + **MiniMax 设计风格** 的专业后台配色系统。

---

## 主色系

主色为 `#1456f0` (MiniMax Brand Blue)

| 用途 | 变量 | 色值 |
|------|------|------|
| 主色 | `--tblr-primary` | `#1456f0` |
| 轻盈版 (背景) | `--tblr-primary-bg-subtle` | `#eff6ff` |
| 深色版 (悬停) | `--tblr-primary-hover` | `#0f38b8` |
| 更深色 (按下) | `--tblr-primary-active` | `#0a2789` |

### 色值层级

```
主色:     #1456f0  ████████████████
轻盈版:   #eff6ff  ████████████████
深色版:   #0f38b8  ████████████████
更深色:   #0a2789  ████████████████
```

---

## 中性灰度阶梯

| 变量 | 色值 | 用途 |
|------|------|------|
| `--tblr-gray-50` | `#f9fafb` | 最浅背景 |
| `--tblr-gray-100` | `#f3f4f6` | 悬停背景 |
| `--tblr-gray-200` | `#e5e7eb` | 边框 |
| `--tblr-gray-300` | `#d1d5db` | 禁用边框 |
| `--tblr-gray-400` | `#9ca3af` | 占位符 |
| `--tblr-gray-500` | `#6b7280` | 次要文本 |
| `--tblr-gray-600` | `#4b5563` | 正文文本 |
| `--tblr-gray-700` | `#374151` | 深色文本 |
| `--tblr-gray-800` | `#1f2937` | 深色强调 |
| `--tblr-gray-900` | `#111827` | 最深文本 |

---

## 背景色

| 变量 | 色值 | 用途 |
|------|------|------|
| `--tblr-bg-canvas` | `#f8fafc` | 页面画布 (柔和非纯白) |
| `--tblr-bg-surface` | `#ffffff` | 卡片/容器 (纯白) |
| `--tblr-bg-surface-alt` | `#f9fafb` | 卡片头部等 |

### 层级关系

```
┌─────────────────────────────────────┐
│         --tblr-bg-canvas            │  ← 页面底层 (#f8fafc)
│  ┌───────────────────────────────┐   │
│  │      --tblr-bg-surface       │   │  ← 卡片层 (#ffffff)
│  │  ┌─────────────────────────┐ │   │
│  │  │  --tblr-bg-surface-alt │ │   │  ← 卡片头部 (#f9fafb)
│  │  └─────────────────────────┘ │   │
│  └───────────────────────────────┘   │
└─────────────────────────────────────┘
```

---

## 语义色系

### 成功色 (Success)

| 变量 | 色值 |
|------|------|
| `--tblr-success` | `#10b981` |
| `--tblr-success-light` | `#34d399` |
| `--tblr-success-bg-subtle` | `#ecfdf5` |

### 警告色 (Warning)

| 变量 | 色值 |
|------|------|
| `--tblr-warning` | `#f59e0b` |
| `--tblr-warning-light` | `#fbbf24` |
| `--tblr-warning-bg-subtle` | `#fffbeb` |

### 危险色 (Danger)

| 变量 | 色值 |
|------|------|
| `--tblr-danger` | `#ef4444` |
| `--tblr-danger-light` | `#f87171` |
| `--tblr-danger-bg-subtle` | `#fef2f2` |

### 信息色 (Info)

| 变量 | 色值 |
|------|------|
| `--tblr-info` | `#0ea5e9` |
| `--tblr-info-light` | `#38bdf8` |
| `--tblr-info-bg-subtle` | `#f0f9ff` |

---

## CSS 变量完整代码

将以下代码添加到 `design-system.css`:

```css
:root {
    /* ============================================
       主色系
       ============================================ */
    --tblr-primary: #1456f0;
    --tblr-primary-light: #3daeff;
    --tblr-primary-lighter: #93c5fd;
    --tblr-primary-lightest: #dbeafe;
    --tblr-primary-dark: #0f38b8;
    --tblr-primary-darker: #0a2789;
    --tblr-primary-darkest: #061b5a;
    --tblr-primary-hover: #0f38b8;
    --tblr-primary-active: #0a2789;
    --tblr-primary-bg-subtle: #eff6ff;
    --tblr-primary-bg-soft: #dbeafe;
    
    /* ============================================
       中性灰度阶梯
       ============================================ */
    --tblr-gray-50: #f9fafb;
    --tblr-gray-100: #f3f4f6;
    --tblr-gray-200: #e5e7eb;
    --tblr-gray-300: #d1d5db;
    --tblr-gray-400: #9ca3af;
    --tblr-gray-500: #6b7280;
    --tblr-gray-600: #4b5563;
    --tblr-gray-700: #374151;
    --tblr-gray-800: #1f2937;
    --tblr-gray-900: #111827;
    
    /* ============================================
       背景色
       ============================================ */
    --tblr-bg-canvas: #f8fafc;
    --tblr-bg-surface: #ffffff;
    --tblr-bg-surface-alt: #f9fafb;
    --tblr-bg-surface-hover: #f3f4f6;
    
    /* ============================================
       边框色
       ============================================ */
    --tblr-border-color: #e5e7eb;
    --tblr-border-color-light: #f3f4f6;
    --tblr-border-color-dark: #d1d5db;
    --tblr-border: var(--tblr-border-color);
    
    /* ============================================
       文本色
       ============================================ */
    --tblr-body-color: #111827;
    --tblr-text-color: #1f2937;
    --tblr-text-muted: #6b7280;
    --tblr-text-disabled: #9ca3af;
    
    /* ============================================
       语义色
       ============================================ */
    --tblr-success: #10b981;
    --tblr-success-light: #34d399;
    --tblr-success-lighter: #6ee7b7;
    --tblr-success-lightest: #d1fae5;
    --tblr-success-dark: #059669;
    --tblr-success-bg-subtle: #ecfdf5;
    
    --tblr-warning: #f59e0b;
    --tblr-warning-light: #fbbf24;
    --tblr-warning-lighter: #fcd34d;
    --tblr-warning-lightest: #fef3c7;
    --tblr-warning-dark: #d97706;
    --tblr-warning-bg-subtle: #fffbeb;
    
    --tblr-danger: #ef4444;
    --tblr-danger-light: #f87171;
    --tblr-danger-lighter: #fca5a5;
    --tblr-danger-lightest: #fee2e2;
    --tblr-danger-dark: #dc2626;
    --tblr-danger-bg-subtle: #fef2f2;
    
    --tblr-info: #0ea5e9;
    --tblr-info-light: #38bdf8;
    --tblr-info-lighter: #7dd3fc;
    --tblr-info-lightest: #e0f2fe;
    --tblr-info-dark: #0284c7;
    --tblr-info-bg-subtle: #f0f9ff;
    
    --tblr-secondary: #6b7280;
    --tblr-secondary-light: #9ca3af;
    --tblr-secondary-dark: #4b5563;
    --tblr-secondary-bg-subtle: #f9fafb;
    
    --tblr-brand: #1456f0;
    --tblr-brand-light: #3daeff;
    --tblr-brand-dark: #0f38b8;
    --tblr-brand-bg-subtle: #eff6ff;
    
    /* ============================================
       链接色
       ============================================ */
    --tblr-link-color: #1456f0;
    --tblr-link-hover-color: #0f38b8;
    
    /* ============================================
       组件变量
       ============================================ */
    --tblr-card-bg: var(--tblr-bg-surface);
    --tblr-card-border-color: var(--tblr-border-color-light);
    --tblr-card-cap-bg: var(--tblr-bg-surface-alt);
    
    --tblr-modal-bg: var(--tblr-bg-canvas);
    --tblr-modal-border-color: var(--tblr-border-color);
    
    --tblr-dropdown-bg: var(--tblr-bg-surface);
    --tblr-dropdown-border-color: var(--tblr-border-color);
    --tblr-dropdown-link-hover-bg: var(--tblr-gray-100);
    
    --tblr-input-bg: var(--tblr-bg-surface);
    --tblr-input-border-color: var(--tblr-border-color);
    --tblr-input-focus-border-color: var(--tblr-primary);
    --tblr-input-placeholder-color: var(--tblr-gray-500);
    
    --tblr-table-bg: transparent;
    --tblr-table-striped-bg: var(--tblr-gray-50);
    --tblr-table-hover-bg: var(--tblr-gray-50);
    
    /* ============================================
       阴影
       ============================================ */
    --tblr-shadow-sm: 0 1px 2px 0 rgba(0, 0, 0, 0.05);
    --tblr-shadow: 0 1px 3px 0 rgba(0, 0, 0, 0.1), 0 1px 2px -1px rgba(0, 0, 0, 0.1);
    --tblr-shadow-lg: 0 10px 15px -3px rgba(0, 0, 0, 0.1), 0 4px 6px -4px rgba(0, 0, 0, 0.1);
    --tblr-shadow-xl: 0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 8px 10px -6px rgba(0, 0, 0, 0.1);
    --tblr-shadow-brand: 0 0 15px rgba(44, 30, 116, 0.16);
    --tblr-shadow-brand-lg: 0 4px 20px rgba(44, 30, 116, 0.2);
}
```

---

## 设计原则

### 1. 背景层级
- `--tblr-bg-canvas` (#f8fafc) 比 `--tblr-bg-surface` (#ffffff) 柔和
- 形成微妙的层级感，避免纯白刺眼

### 2. 主色应用
- 轻盈版 (`#eff6ff`) 用于背景强调、徽章
- 深色版 (`#0f38b8`) 用于悬停状态

### 3. 圆角系统
| 用途 | 圆角值 |
|------|--------|
| 按钮、输入框 | 8px |
| 卡片、容器 | 13px |
| 大卡片 | 20px |
| 胶囊、徽章 | 9999px |

---

## 使用示例

```html
<!-- 主色按钮 -->
<button style="background: var(--tblr-primary);">主要操作</button>
<button style="background: var(--tblr-primary-hover);">悬停状态</button>

<!-- 背景层级 -->
<div style="background: var(--tblr-bg-canvas);">
    <div style="background: var(--tblr-bg-surface);">
        内容
    </div>
</div>

<!-- 语义色标签 -->
<span style="background: var(--tblr-success-bg-subtle); color: var(--tblr-success);">成功</span>
<span style="background: var(--tblr-warning-bg-subtle); color: var(--tblr-warning);">警告</span>
<span style="background: var(--tblr-danger-bg-subtle); color: var(--tblr-danger);">危险</span>
<span style="background: var(--tblr-info-bg-subtle); color: var(--tblr-info);">信息</span>
```

---

*文档生成时间: 2026-04-13*
*设计系统版本: v1.0*
