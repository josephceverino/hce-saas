// Recorrido del paciente (docs/recorrido.md) con Playwright. Uso: node recorrido.mjs [ultimoPaso]
// Requiere la app recien creada (sin cuentas): solo existe admin / Temporal2026. Guarda capturas en docs/capturas/.
import { navegador, captura, BASE } from './lib.mjs';

const ultimo = Number(process.argv[2] || 9);
const datos = {}; // lo que un paso le pasa al siguiente (usuarios y claves temporales)
const { browser, nuevaPagina, errores } = await navegador();

async function entrarConTemporal(page, usuario, temporal, nueva, prefijo) {
  await page.goto(BASE + '/login/index.html');
  await page.fill('#usuario', usuario);
  await page.fill('#clave', temporal);
  await page.click('button[type=submit]');
  await page.waitForURL('**/cambiar-clave/');
  if (prefijo) await captura(page, prefijo + '_cambiar_clave', false);
  await page.fill('#claveActual', temporal);
  await page.fill('#claveNueva', nueva);
  await page.fill('#confirmacion', nueva);
  await page.click('button[type=submit]');
  await page.waitForURL(u => !u.pathname.startsWith('/cambiar-clave'));
  await page.waitForLoadState('networkidle');
}

const pasos = {
  /** Paso 1 (CU-16): el administrador de plataforma entra, cambia su clave y da de alta un hospital. */
  async 1(page) {
    await entrarConTemporal(page, 'admin', 'Temporal2026', 'Plataforma2026', 'paso1');
    await captura(page, 'paso1_P-07_hospitales_vacio', false);
    await page.click('#nuevo');
    await page.fill('#nombre', 'Hospital Regional de Trujillo');
    await page.fill('#codigo', '0012345');
    await page.fill('#region', 'La Libertad');
    await page.selectOption('#plan', 'ESTANDAR');
    await page.fill('#adminNombre', 'Rosa Mendoza Quispe');
    await page.fill('#adminDocumento', '45678912');
    await page.fill('#adminCorreo', 'rmendoza@example.com');
    await page.fill('#adminUsuario', 'admin.trujillo');
    await captura(page, 'paso1_P-07_formulario', false);
    await page.click('#formulario button[type=submit]');
    await page.waitForSelector('#constancia:not([hidden])');
    datos.adminHospital = { usuario: await page.textContent('#cUsuario'), clave: await page.textContent('#cClave') };
    await captura(page, 'paso1_P-07_constancia', true);
  }
};

for (let n = 1; n <= ultimo; n++) {
  if (!pasos[n]) break;
  const page = await nuevaPagina();
  console.log('--- paso', n);
  await pasos[n](page);
  await page.context().close();
}
await browser.close();
console.log('datos', JSON.stringify(datos));
console.log(errores.length ? errores.join('\n') : 'sin errores');
