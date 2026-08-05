import { describe, expect, it } from 'vitest';
import { App } from './app';

describe('App', () => {
  it('expone el componente raíz', () => {
    expect(App).toBeDefined();
  });
});
