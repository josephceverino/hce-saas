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
    await page.waitForSelector('.hce-hospital-tarjeta');
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
    await page.waitForFunction(() => document.querySelectorAll('#cuerpo tr').length === 5);
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
  },

  /** Paso 4 (CU-20): admision admite a la paciente por consulta externa y a otro paciente por emergencia. */
  async 4(page) {
    await entrarConClave(page, datos.personal.ADMISION.usuario, 'Admision2026');
    // Un segundo paciente que llega despues por emergencia (para mostrar que la emergencia va primero)
    await page.goto(BASE + '/pacientes/nuevo.html');
    const otro = { nroDocumento: '73456729', nombres: 'José Antonio', apellidos: 'Vargas León', fechaNacimiento: '1958-02-03',
      lugarNacimiento: 'Chiclayo', ocupacion: 'Jubilado', direccion: 'Av. España 1020, Trujillo', procedencia: 'Trujillo',
      telefono: '944222333', correo: 'jvargas@example.com', contactoEmergenciaNombre: 'Carmen Vargas', contactoEmergenciaTelefono: '944333222' };
    for (const [id, v] of Object.entries(otro)) await page.fill('#' + id, v);
    await page.selectOption('#sexo', 'M');
    await page.selectOption('#estadoCivil', 'VIUDO');
    await page.selectOption('#gradoInstruccion', 'PRIMARIA');
    await page.selectOption('#contactoEmergenciaParentesco', 'Hijo o hija');
    await page.click('#formulario button[type=submit]');
    await page.waitForSelector('#resultado:not([hidden])');
    await page.goto(BASE + '/pacientes/');
    await page.waitForLoadState('networkidle');
    await admitirDesdeLista(page, '72345618', 'CONSULTA_EXTERNA', 'paso4_P-10_admitir_dialogo');
    await admitirDesdeLista(page, '73456729', 'EMERGENCIA');
    await captura(page, 'paso4_P-02_admitidos', false);
    const texto = await page.textContent('#cuerpo');
    verificar(texto.includes('Admitido'), 'los pacientes figuran como admitidos hoy');
    verificar(texto.includes('Emergencia'), 'la emergencia se distingue en la lista');
    await page.goto(BASE + '/cola/');
    await page.waitForLoadState('networkidle');
    await captura(page, 'paso4_P-09_pacientes_del_dia', false);
  },

  /** Paso 5 (RF-22, CU-21): enfermeria ve la cola con la emergencia primero y toma al paciente. */
  async 5(page) {
    const e = datos.personal.ENFERMERIA;
    await entrarConTemporal(page, e.usuario, e.clave, 'Enfermeria2026');
    await captura(page, 'paso5_inicio_enfermeria', false);
    await page.goto(BASE + '/cola/');
    await page.waitForLoadState('networkidle');
    await captura(page, 'paso5_P-09_cola_enfermeria', false);
    const primera = await page.textContent('#cuerpo tr:first-child');
    verificar(primera.includes('Vargas León') && primera.includes('Emergencia'), 'la emergencia va primero en la cola aunque llegó después');
    // Toma a la emergencia (primera de la cola): pasa a "en enfermeria" y abre su historia con la admision de hoy
    await page.click('#cuerpo tr:first-child button:has-text("Tomar paciente")');
    await page.waitForURL('**/historia/**');
    await page.waitForSelector('#formSignos:not([hidden])');
    await captura(page, 'paso5_P-04_historia_enfermeria', true);
    // Un valor imposible se rechaza con mensaje claro (RN-17)
    const signos = { presionSistolica: '150', presionDiastolica: '95', frecuenciaCardiaca: '96', frecuenciaRespiratoria: '22',
      temperatura: '45.5', saturacionOxigeno: '93' };
    for (const [id, v] of Object.entries(signos)) await page.fill('#' + id, v);
    await page.click('#formSignos button[type=submit]');
    await page.waitForSelector('#mensajeEnfermeria.error');
    verificar((await page.textContent('#mensajeEnfermeria')).includes('temperatura'), 'una temperatura imposible se rechaza con mensaje claro');
    await captura(page, 'paso5_P-04_signos_rechazados', false);
    await page.fill('#temperatura', '37.8');
    await page.click('#formSignos button[type=submit]');
    await page.waitForSelector('#mensajeEnfermeria.ok');
    await page.selectOption('#tipoAntecedente', 'PERSONAL');
    await page.fill('#descripcionAntecedente', 'Hipertensión arterial en tratamiento');
    await page.click('#formAntecedente button[type=submit]');
    await page.waitForSelector('#mensajeAntecedente.ok');
    await page.selectOption('#tipoAntecedente', 'QUIRURGICO');
    await page.fill('#descripcionAntecedente', 'Colecistectomía en 2015');
    await page.click('#formAntecedente button[type=submit]');
    await page.waitForSelector('#mensajeAntecedente.ok');
    await page.fill('#agente', 'Ácido acetilsalicílico');
    await page.fill('#reaccion', 'Dificultad para respirar');
    await page.selectOption('#gravedad', 'ALTA');
    await page.click('#formAlergia button[type=submit]');
    await page.waitForSelector('#mensajeAlergia.ok');
    await captura(page, 'paso5_P-04_signos_y_antecedentes', true);
    await page.click('#listo');
    await page.click('.hce-modal .primario');
    await page.waitForFunction(() => document.getElementById('enfermeriaEstado').textContent.includes('Listo'), null, { timeout: 10000 }).catch(() => {});
    verificar((await page.textContent('#enfermeriaEstado')).includes('Listo para atención'), 'la admisión pasó a listo para atención');
    await captura(page, 'paso5_P-04_listo_para_atencion', false);
    // Cierre del paso 5: el medico ve al paciente listo en su cola (el paso 6 empieza aqui)
    const medico = await nuevaPagina();
    await colaDelMedico(medico);
    await medico.context().close();
  }
};

async function colaDelMedico(page) {
    const m = datos.personal.MEDICO;
    await entrarConTemporal(page, m.usuario, m.clave, 'Medico2026');
    await page.goto(BASE + '/cola/');
    await page.waitForLoadState('networkidle');
    await captura(page, 'paso5_P-09_cola_medico', false);
    const texto = await page.textContent('#cuerpo');
    verificar(texto.includes('Vargas León') && texto.includes('Listo para atención'), 'el médico ve al paciente listo en su cola');
    verificar(!texto.includes('Gutiérrez Salas'), 'el médico no ve a quien aún no pasa por enfermería');
}

async function entrarConClave(page, usuario, clave) {
  await page.goto(BASE + '/login/index.html');
  await page.fill('#usuario', usuario);
  await page.fill('#clave', clave);
  await page.click('button[type=submit]');
  await page.waitForURL(u => !u.pathname.startsWith('/login'));
  await page.waitForLoadState('networkidle');
}

async function admitirDesdeLista(page, documento, tipo, capturaDialogo) {
  await page.fill('#buscar', documento);
  await page.click('#cuerpo tr:first-child button:has-text("Admitir")');
  await page.waitForSelector('.hce-modal');
  if (capturaDialogo) await captura(page, capturaDialogo, false);
  await page.click('.hce-opcion:has-text("' + (tipo === 'EMERGENCIA' ? 'Emergencia' : 'Consulta externa') + '")');
  await page.waitForSelector('#mensaje.ok');
  await page.fill('#buscar', '');
  await page.dispatchEvent('#buscar', 'input');
}

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
