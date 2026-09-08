---
name: Caspian AI Browser
colors:
  surface: '#0f131d'
  surface-dim: '#0f131d'
  surface-bright: '#353944'
  surface-container-lowest: '#0a0e17'
  surface-container-low: '#181b25'
  surface-container: '#1c1f29'
  surface-container-high: '#262a34'
  surface-container-highest: '#31353f'
  on-surface: '#dfe2f0'
  on-surface-variant: '#bac9cc'
  inverse-surface: '#dfe2f0'
  inverse-on-surface: '#2d303b'
  outline: '#849396'
  outline-variant: '#3b494c'
  surface-tint: '#00daf3'
  primary: '#c3f5ff'
  on-primary: '#00363d'
  primary-container: '#00e5ff'
  on-primary-container: '#00626e'
  inverse-primary: '#006875'
  secondary: '#4edea3'
  on-secondary: '#003824'
  secondary-container: '#00a572'
  on-secondary-container: '#00311f'
  tertiary: '#ffeac0'
  on-tertiary: '#3e2e00'
  tertiary-container: '#fec931'
  on-tertiary-container: '#6f5500'
  error: '#ffb4ab'
  on-error: '#690005'
  error-container: '#93000a'
  on-error-container: '#ffdad6'
  primary-fixed: '#9cf0ff'
  primary-fixed-dim: '#00daf3'
  on-primary-fixed: '#001f24'
  on-primary-fixed-variant: '#004f58'
  secondary-fixed: '#6ffbbe'
  secondary-fixed-dim: '#4edea3'
  on-secondary-fixed: '#002113'
  on-secondary-fixed-variant: '#005236'
  tertiary-fixed: '#ffdf96'
  tertiary-fixed-dim: '#f3bf26'
  on-tertiary-fixed: '#251a00'
  on-tertiary-fixed-variant: '#594400'
  background: '#0f131d'
  on-background: '#dfe2f0'
  surface-variant: '#31353f'
typography:
  display-lg:
    fontFamily: Inter
    fontSize: 32px
    fontWeight: '700'
    lineHeight: 40px
    letterSpacing: -0.02em
  headline-md:
    fontFamily: Inter
    fontSize: 24px
    fontWeight: '600'
    lineHeight: 32px
    letterSpacing: -0.01em
  headline-sm:
    fontFamily: Inter
    fontSize: 20px
    fontWeight: '600'
    lineHeight: 28px
  body-lg:
    fontFamily: Inter
    fontSize: 16px
    fontWeight: '400'
    lineHeight: 24px
  body-md:
    fontFamily: Inter
    fontSize: 14px
    fontWeight: '400'
    lineHeight: 20px
  label-caps:
    fontFamily: JetBrains Mono
    fontSize: 12px
    fontWeight: '500'
    lineHeight: 16px
    letterSpacing: 0.05em
  headline-lg-mobile:
    fontFamily: Inter
    fontSize: 28px
    fontWeight: '700'
    lineHeight: 36px
rounded:
  sm: 0.5rem
  DEFAULT: 1rem
  md: 1.5rem
  lg: 2rem
  xl: 3rem
  full: 9999px
spacing:
  base: 4px
  xs: 8px
  sm: 12px
  md: 16px
  lg: 24px
  xl: 32px
  container-margin: 20px
  gutter: 12px
---

## Brand & Style

This design system establishes a high-performance, futuristic aesthetic tailored for a next-generation AI browsing experience. The visual narrative is rooted in "Digital Fluidity"—combining the depth of deep space with the sharp precision of neon instrumentation.

The style is a sophisticated blend of **Glassmorphism** and **Minimalist Futurism**. It prioritizes extreme clarity and reduced cognitive load through translucent layers that imply depth without clutter. The interface should feel like a high-end heads-up display (HUD): reactive, lightweight, and premium. Key characteristics include heavy use of background blurs, 1px "micro-borders," and subtle glow effects that signify AI activity.

## Colors

The palette is anchored in **Deep Obsidian Black (#050811)** to maximize OLED efficiency and provide a high-contrast canvas for interactive elements. 

- **Primary (Neon Cyan):** Used for critical actions, AI suggestions, and active states. It represents the "intelligence" of the browser.
- **Secondary (Emerald):** Used for security indicators, success states, and secondary utilities (e.g., ad-blocker status).
- **Surface:** Surfaces are never fully opaque. Use a base of #0D1426 at 60% opacity with a `backdrop-filter: blur(20px)` to create the glass effect.
- **Accents:** Use low-opacity glows (10-15%) of the primary color to highlight active regions or "glowing action pods."

## Typography

The system utilizes **Inter** for its neutral, systematic clarity, ensuring that complex AI-generated text remains highly readable. **JetBrains Mono** is introduced sparingly for technical metadata, URLs, and "labels" to reinforce the futuristic, developer-grade performance of the browser.

Headlines should use tighter letter spacing to maintain a "locked-in" professional look. Use `body-md` for standard web content descriptions and `label-caps` for status badges or small UI signals like "AI ENHANCED" or "SECURE."

## Layout & Spacing

This design system employs a **Fluid Grid** model optimized for thumb-driven mobile navigation. 

- **The "Safe Zone":** All primary navigation (Omnibox, Action Pods) is anchored to the bottom 33% of the screen for ergonomic reach.
- **Margins:** A consistent 20px side margin is used for all main content containers.
- **Rhythm:** Use an 8px base grid, but allow for 4px increments for micro-adjustments within components like buttons or input fields.
- **Breakpoints:** While mobile-first, the layout should transition to a centered max-width (600px) on tablets to maintain the "handheld HUD" feel.

## Elevation & Depth

Depth is conveyed through **translucency and luminosity** rather than traditional drop shadows.

1.  **Base Layer:** The Obsidian background (#050811).
2.  **Mid Layer (Content):** Glassmorphic cards with `blur(12px)` and a 1px stroke (`rgba(255,255,255,0.05)`).
3.  **Top Layer (Interactive):** Floating elements like the Omnibox. These feature a 1px Neon Cyan border (`rgba(0, 225, 255, 0.3)`) and a subtle outer glow with a 20px spread and 5% opacity.
4.  **Scrims:** When modals appear, use a 40% black tint with a heavy backdrop blur to pull the user's focus forward.

## Shapes

The shape language is defined by **hyper-rounded "Squircle" geometry**, inspired by 120Hz display curves and organic tech aesthetics.

- **Large Containers:** Cards and main sections use a 24px to 32px corner radius.
- **Buttons & Inputs:** The Omnibox and Action Pods must be full pill-shaped (radius 999px) to emphasize the "pod" metaphor.
- **Selection States:** Use a 12px radius for smaller items like list selection or chips.

## Components

### Omnibox (Pill)
The search/URL bar is a floating pill at the bottom of the screen. Use a glass background with a permanent 1px neon cyan border. The text cursor should be the primary cyan color.

### Action Pods
Circular or pill-shaped buttons that house AI functions. These feature a "breathing" glow effect (pulsing from 5% to 15% opacity) when the AI is processing information.

### Cards
Cards use a 24px radius and a subtle vertical gradient (top: transparent, bottom: `rgba(255,255,255,0.02)`). They do not use shadows; they are defined entirely by their 1px semi-transparent borders.

### Badges & Chips
Small, high-contrast indicators. Use **JetBrains Mono** all-caps for text. For "AI-Mode" badges, use a gradient fill from Neon Cyan to Emerald at 20% opacity.

### Input Fields
Inputs should have no background fill until focused. On focus, they transition to a glassmorphic state with the 1px neon border.

### Tab Switcher
Visualized as a vertical stack of glass cards with 32px rounded corners, showing a blurred preview of the underlying website.