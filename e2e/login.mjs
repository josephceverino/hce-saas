// Captura de P-01 (inicio de sesion), item 0.1.
import { navegador, captura, BASE } from './lib.mjs';
const { browser, nuevaPagina, errores } = await navegador();
const page = await nuevaPagina();
await page.goto(BASE + '/login/index.html');
await captura(page, 'P-01_login', false);
await browser.close();
console.log(errores.length ? errores.join('\n') : 'sin errores');
