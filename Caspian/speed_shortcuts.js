globalThis.caspianSpeedKeys ??= (() => {
  const cycleDefault = 'alt+shift+d';
  const modifierKeys = new Set(['Alt', 'AltGraph', 'Control', 'Shift', 'Meta', 'Dead', 'Unidentified']);

  function fromEvent(event) {
    if (event.isComposing || event.getModifierState('AltGraph') || modifierKeys.has(event.key)) return '';
    let key = event.key.toLowerCase();
    if (key === ' ') key = 'space';
    if (key === '+') key = 'plus';
    // Some layouts generate a symbol when Alt is held, even on a letter key.
    if (event.altKey && /^Key[A-Z]$/.test(event.code) && !/^[a-z]$/.test(key)) {
      key = event.code.slice(-1).toLowerCase();
    }
    return [
      event.ctrlKey && 'ctrl',
      event.altKey && 'alt',
      event.shiftKey && 'shift',
      event.metaKey && 'meta',
      key
    ].filter(Boolean).join('+');
  }

  function normalize(value, fallback) {
    if (typeof value !== 'string' || !value.trim()) return fallback;
    const parts = value.toLowerCase().split('+').map(part => part.trim());
    const key = parts.pop();
    const modifiers = parts.map(part => part === 'control' ? 'ctrl' : part);
    if (fallback === cycleDefault && key === 'd' && modifiers.join('+') === 'alt') return cycleDefault;
    return key ? [...['ctrl', 'alt', 'shift', 'meta'].filter(part => modifiers.includes(part)), key].join('+') : fallback;
  }

  return { cycleDefault, fromEvent, normalize };
})();
