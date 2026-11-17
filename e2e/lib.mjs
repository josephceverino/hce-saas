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
    // Los rechazos de validacion (400, 401, 403, 404, 409) son respuestas esperadas que la pantalla muestra; un 500 o un
    // error de JavaScript si es una falla.
    page.on('console', m => { if (m.type() === 'error' && !/status of 4\d\d|favicon/.test(m.text())) errores.push('Consola: ' + m.text()); });
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
  // Pagina completa sin que la barra lateral fija y la superior se desplacen: se agranda la ventana y se restaura.
  const original = page.viewportSize();
  if (completa) {
    const alto = await page.evaluate(() => document.documentElement.scrollHeight);
    if (alto > original.height) await page.setViewportSize({ width: original.width, height: Math.min(alto, 4000) });
  }
  await page.screenshot({ path: path.join(CAPTURAS, nombre + '.png') });
  await page.setViewportSize(original);
  console.log('captura', nombre, page.url());
}
