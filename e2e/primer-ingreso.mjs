// P-13 cambio de clave en el primer ingreso (CU-18). Requiere una base nueva: admin / Temporal2026.
import { navegador, captura, BASE } from './lib.mjs';
const NUEVA = process.env.HCE_CLAVE_ADMIN || 'Plataforma2026';
const { browser, nuevaPagina, errores } = await navegador();
const page = await nuevaPagina();
await page.goto(BASE + '/login/index.html');
await page.fill('#usuario', 'admin');
await page.fill('#clave', 'Temporal2026');
await page.click('button[type=submit]');
await page.waitForURL('**/cambiar-clave/');
await captura(page, 'P-13_cambiar_clave', false);
// Intentar usar otra pantalla: vuelve a cambiar clave
await page.goto(BASE + '/hospitales/');
await page.waitForURL('**/cambiar-clave/');
await page.fill('#claveActual', 'Temporal2026');
await page.fill('#claveNueva', 'corta');
await page.fill('#confirmacion', 'corta');
await page.click('button[type=submit]');
await page.waitForSelector('.hce-mensaje.error');
await captura(page, 'P-13_cambiar_clave_error', false);
await page.fill('#claveNueva', NUEVA);
await page.fill('#confirmacion', NUEVA);
await page.click('button[type=submit]');
await page.waitForURL(u => !u.pathname.startsWith('/cambiar-clave'));
console.log('tras cambiar clave:', page.url());
await browser.close();
console.log(errores.length ? errores.join('\n') : 'sin errores');
