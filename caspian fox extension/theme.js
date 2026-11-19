function applyCaspianPalette({ mode, accent, secondary, customBgColor }) {
  const root = document.documentElement;
  const isDark = mode === 'dark';
  root.dataset.theme = isDark ? 'dark' : 'light';

  const colors = {
    '--accent': accent,
    '--secondary': secondary,
    '--bg-deep': isDark ? customBgColor : null
  };
  for (const [property, color] of Object.entries(colors)) {
    if (typeof color === 'string' && /^#(?:[\da-f]{3}|[\da-f]{6})$/i.test(color)) {
      root.style.setProperty(property, color);
    } else {
      root.style.removeProperty(property);
    }
  }
}
