// Plan v3, 0.4: inicio propio de cada rol. Requiere la app con cuentas de prueba:
//   --hce.demo-usuarios=true --hce.demo-datos=true (solo para capturas; la demo real no las trae).
import { navegador, entrar, captura } from './lib.mjs';
const roles = ['admision.a', 'enfermera.a', 'medico.a', 'admin.a', 'plataforma', 'paciente.a'];
const { browser, nuevaPagina, errores } = await navegador();
for (const u of roles) {
  const page = await nuevaPagina();
  await entrar(page, u, 'demo123');
  await captura(page, 'inicio_' + u.replace('.', '_'), false);
  await page.context().close();
}
await browser.close();
console.log(errores.length ? errores.join('\n') : 'sin errores');
