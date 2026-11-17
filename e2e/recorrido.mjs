// Recorrido del paciente (docs/recorrido.md) con Playwright. Uso: node recorrido.mjs [ultimoPaso]
// Requiere la app recien creada (sin cuentas): solo existe admin / Temporal2026. Guarda capturas en docs/capturas/.
import { navegador, captura, BASE } from './lib.mjs';

const ultimo = Number(process.argv[2] || 9);

/** Verificacion explicita: si falla, el recorrido se detiene con el motivo (sirve como prueba de aceptacion). */
function verificar(condicion, mensaje) {
  if (!condicion) throw new Error('Falla del recorrido: ' + mensaje);
  console.log('  ok:', mensaje);
}
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
    verificar(datos.adminHospital.usuario === 'admin.trujillo', 'la constancia muestra el usuario del administrador');
    verificar(/^[A-Za-z0-9]{10}$/.test(datos.adminHospital.clave), 'la constancia muestra una clave temporal de 10 caracteres');
    verificar(await page.locator('.hce-hospital-tarjeta').count() === 1, 'el hospital aparece en la lista');
    await captura(page, 'paso1_P-07_constancia', true);
  },

  /** Paso 2 (CU-18, CU-17): el administrador del hospital cambia su clave y crea al personal con su rol. */
  async 2(page) {
    const a = datos.adminHospital;
    await entrarConTemporal(page, a.usuario, a.clave, 'Hospital2026', 'paso2');
    await captura(page, 'paso2_inicio_admin_hospital', false);
    await page.goto(BASE + '/usuarios/');
    await page.waitForLoadState('networkidle');
    await captura(page, 'paso2_P-08_usuarios_vacio', false);
    const personal = [
      { usuario: 'carlos.rios', nombres: 'Carlos', apellidos: 'Ríos Salazar', documento: '41234567', rol: 'ADMISION' },
      { usuario: 'ana.flores', nombres: 'Ana', apellidos: 'Flores Vega', documento: '42345678', rol: 'ENFERMERIA', colegiatura: 'CEP-55120' },
      { usuario: 'jorge.paz', nombres: 'Jorge', apellidos: 'Paz Herrera', documento: '43456789', rol: 'MEDICO', colegiatura: 'CMP-70231', especialidad: 'Medicina interna' },
      { usuario: 'elena.campos', nombres: 'Elena', apellidos: 'Campos Ruiz', documento: '44567890', rol: 'APOYO_DIAGNOSTICO', colegiatura: 'CTMP-3301' }
    ];
    datos.personal = {};
    for (const [i, p] of personal.entries()) {
      await page.click('#nuevo');
      await page.fill('#nombres', p.nombres);
      await page.fill('#apellidos', p.apellidos);
      await page.fill('#documento', p.documento);
      await page.fill('#correo', p.usuario + '@example.com');
      await page.fill('#usuario', p.usuario);
      await page.selectOption('#rol', p.rol);
      if (p.colegiatura) await page.fill('#colegiatura', p.colegiatura);
      if (p.especialidad) await page.fill('#especialidad', p.especialidad);
      if (i === 2) await captura(page, 'paso2_P-08_formulario_medico', false);
      await page.click('#formulario button[type=submit]');
      await page.waitForSelector('#constancia:not([hidden])');
      datos.personal[p.rol] = { usuario: p.usuario, clave: await page.textContent('#cClave') };
      if (i === 2) await captura(page, 'paso2_P-08_constancia', false);
      await page.click('#cerrarConstancia');
    }
    await captura(page, 'paso2_P-08_usuarios_lista', false);
    verificar(await page.locator('#cuerpo tr').count() === 5, 'la lista tiene al administrador y 4 personas');
    verificar((await page.textContent('#pestanas')).includes('Clave pendiente 4'), 'las 4 cuentas nuevas tienen clave pendiente');
  },

  /** Paso 3 (CU-03, CU-19): admision registra al paciente con su filiacion completa; se abre su historia. */
  async 3(page) {
    const a = datos.personal.ADMISION;
    await entrarConTemporal(page, a.usuario, a.clave, 'Admision2026');
    await captura(page, 'paso3_inicio_admision', false);
    await page.goto(BASE + '/pacientes/nuevo.html');
    await page.waitForLoadState('networkidle');
    const valores = {
      nroDocumento: '72345618', nombres: 'María Elena', apellidos: 'Gutiérrez Salas', fechaNacimiento: '1987-06-14',
      lugarNacimiento: 'Trujillo', ocupacion: 'Comerciante', religion: 'Católica', direccion: 'Jr. Pizarro 455, Trujillo',
      procedencia: 'Trujillo', telefono: '949123456', correo: 'mgutierrez@example.com',
      contactoEmergenciaNombre: 'Luis Gutiérrez Salas', contactoEmergenciaTelefono: '949654321'
    };
    for (const [id, v] of Object.entries(valores)) await page.fill('#' + id, v);
    await page.selectOption('#sexo', 'F');
    await page.selectOption('#estadoCivil', 'CASADO');
    await page.selectOption('#gradoInstruccion', 'SECUNDARIA');
    await page.selectOption('#contactoEmergenciaParentesco', 'Hermano o hermana');
    await captura(page, 'paso3_P-16_registrar_paciente', true);
    await page.click('#formulario button[type=submit]');
    await page.waitForSelector('#resultado:not([hidden])');
    datos.paciente = { documento: valores.nroDocumento,
      usuario: await page.textContent('#cUsuario'), clave: await page.textContent('#cClave') };
    await captura(page, 'paso3_P-16_constancia', false);
    verificar(datos.paciente.usuario === valores.nroDocumento, 'el usuario del portal es el documento del paciente');
    verificar(datos.paciente.clave.length === 10, 'la constancia del paciente muestra su clave temporal');
    await page.goto(BASE + '/pacientes/');
    await page.waitForLoadState('networkidle');
    await captura(page, 'paso3_P-02_pacientes', false);
    verificar((await page.textContent('#cuerpo')).includes('72345618'), 'el paciente aparece en la lista del hospital');
  }
};

let fallo = null;
try {
  for (let n = 1; n <= ultimo; n++) {
    if (!pasos[n]) break;
    const page = await nuevaPagina();
    console.log('--- paso', n);
    await pasos[n](page);
    await page.context().close();
  }
} catch (e) {
  fallo = e;
}
await browser.close();
if (fallo) { console.error(fallo.message); process.exitCode = 1; }
console.log('datos', JSON.stringify(datos));
console.log(errores.length ? errores.join('\n') : 'sin errores');
