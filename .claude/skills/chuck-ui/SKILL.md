---
name: chuck-ui
description: Cyberpunk Bento Grid & Glassmorphism design system for building high-tech dashboards and data visualization platforms. Use when creating futuristic UI components, cyberpunk-themed interfaces, glass panels, neon-styled elements, bento grid layouts, or dark tech dashboards. Triggers on requests for cyberpunk design, glassmorphism cards, neon buttons, CRT/scanline effects, or futuristic data visualization UIs.
---

# CHUCK_UI Design System

Cyberpunk Bento Grid & Glassmorphism design system for high-tech dashboards.

## Design Principles

1. **Immersive Depth**: Use glassmorphism + neon glow for depth. Avoid flat design.
2. **Grid Consistency**: Strict Bento Grid (seamless rectangular modules).
3. **Visual Hierarchy**: Importance = neon brightness + glow intensity.
4. **Alive Interface**: Always include subtle animations (chart motion, cursor blink, scanlines).

## Color System

### Backgrounds
| Variable | Value | Usage |
|----------|-------|-------|
| `--bg-core` | `#050510` | Page background (deepest void) |
| `--card-bg` | `rgba(20, 20, 35, 0.6)` | Glass panel background |
| `--card-hover` | `rgba(20, 20, 35, 0.8)` | Card hover state |

### Neon Accents
| Variable | Value | Meaning |
|----------|-------|---------|
| `--neon-blue` | `#00f3ff` | Primary info, borders, titles |
| `--neon-pink` | `#bc13fe` | Alert, emphasis, active state |
| `--neon-green` | `#0aff0a` | Success, positive, online |
| `--neon-yellow` | `#fefe00` | Warning, attention needed |

### Text
| Variable | Value | Usage |
|----------|-------|-------|
| `--text-main` | `#e0e0e0` | Body text (avoid pure white) |
| `--text-dim` | `#8888aa` | Captions, labels, inactive |

## Typography

### Font Families
- **Headings**: `'Orbitron', sans-serif` — Mechanical, angular, uppercase preferred
- **Body**: `'Rajdhani', sans-serif` — Readable yet technical
- **Code/Logs**: `'Courier New', monospace` — Terminal aesthetic

### Text Styles
- **H1**: 32px / Bold / letter-spacing: 2px
- **H2**: 18px / Medium / `--neon-blue` / bottom border required
- **Body**: 16px / Regular / `--text-main`
- **Caption**: 12px / Light / `--text-dim`

## Core Components

### Glass Card (Bento Panel)

```css
.card {
  background: var(--card-bg);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border: 1px solid rgba(0, 243, 255, 0.3);
  border-radius: 12px;
  box-shadow: 0 0 15px rgba(0, 243, 255, 0.05);
  transition: all 0.3s ease;
}

.card:hover {
  border-color: var(--neon-pink);
  box-shadow: 0 0 25px rgba(188, 19, 254, 0.2);
  transform: translateY(-2px);
}
```

### Neon Button

```css
.btn-neon {
  background: transparent;
  border: 1px solid var(--neon-blue);
  color: var(--neon-blue);
  font-family: 'Orbitron';
  text-transform: uppercase;
  padding: 8px 16px;
  cursor: pointer;
  box-shadow: 0 0 10px rgba(0, 243, 255, 0.2);
  transition: 0.2s;
}

.btn-neon:hover {
  background: var(--neon-blue);
  color: #000;
  box-shadow: 0 0 20px var(--neon-blue);
}
```

### Scanline Overlay (CRT Effect)

Apply to `body` for retro monitor feel:

```css
.scanline {
  position: fixed;
  top: 0; left: 0;
  width: 100vw; height: 100vh;
  background: repeating-linear-gradient(
    0deg,
    rgba(0, 0, 0, 0.15),
    rgba(0, 0, 0, 0.15) 1px,
    transparent 1px,
    transparent 2px
  );
  pointer-events: none;
  z-index: 9999;
}
```

## Layout Grid

CSS Grid Bento system:

- **Columns**: `grid-template-columns: repeat(4, 1fr)`
- **Gap**: `20px`
- **Responsive**:
    - Desktop (1200px+): 4 columns
    - Tablet (768px-1199px): 2 columns
    - Mobile (<768px): 1 column (stack)

## Implementation Checklist

When building CHUCK_UI components, verify:

1. [ ] **Contrast**: Sufficient background/text contrast for readability
2. [ ] **Glow**: Appropriate neon glow on key elements (avoid overuse)
3. [ ] **Glass**: Card backgrounds show blur-through effect
4. [ ] **Font**: Numbers use Orbitron, text uses Rajdhani
5. [ ] **Motion**: Smooth transitions on data loading