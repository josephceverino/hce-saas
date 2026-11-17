// Utilidades de los recorridos con Playwright (solo desarrollo). Usa el Chrome instalado en la PC.
import { chromium } from 'playwright-core';
import { mkdirSync } from 'node:fs';
import { fileURLToPath } from 'node:url';
import path from 'node:path';

export const BASE = process.env.HCE_URL || 'http://localhost:8191';
const raiz = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..');
export const CAPTURAS = path.join(raiz, 'docs', 'capturas');
mkdirSync(CAPTURAS, { recursive: true });

export async function navegador() {
  const browser = await chromium.launch({ channel: 'chrome', headless: true });
  const errores = [];
  const nuevaPagina = async () => {
    const ctx = await browser.newContext({ viewport: { width: 1366, height: 860 }, locale: 'es-PE' });
    const page = await ctx.newPage();
    page.on('pageerror', e => errores.push('Error JS: ' + e.message));
    page.on('console', m => { if (m.type() === 'error' && !/401|favicon/.test(m.text())) errores.push('Consola: ' + m.text()); });
    return page;
  };
  return { browser, nuevaPagina, errores };
}

export async function entrar(page, usuario, clave) {
  await page.goto(BASE + '/login/index.html');
  await page.fill('#usuario', usuario);
  await page.fill('#clave', clave);
  await Promise.all([page.waitForURL(u => !u.pathname.startsWith('/login'), { timeout: 10000 }), page.click('button[type=submit]')]);
  await page.waitForLoadState('networkidle');
}

export async function captura(page, nombre, completa = true) {
  await page.waitForLoadState('networkidle');
  await page.screenshot({ path: path.join(CAPTURAS, nombre + '.png'), fullPage: completa });
  console.log('captura', nombre, page.url());
}
