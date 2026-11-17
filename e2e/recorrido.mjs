// Recorrido del paciente (docs/recorrido.md) con Playwright. Uso: node recorrido.mjs [ultimoPaso] [primerPaso]
// El paso 9 solo usa claves fijas de los pasos 1 a 8, asi que puede correr solo: node recorrido.mjs 9 9
// Requiere la app recien creada (sin cuentas): solo existe admin / Temporal2026. Guarda capturas en docs/capturas/.
import { navegador, captura, BASE } from './lib.mjs';

const ultimo = Number(process.argv[2] || 9);
const primero = Number(process.argv[3] || 1);

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
  },

  /** Paso 6 (CU-05 ampliado, CU-08, CU-22): el medico inicia la atencion desde la cola, diagnostica, indica y pide un examen. */
  async 6(page) {
    await entrarConClave(page, datos.personal.MEDICO.usuario, 'Medico2026');
    await captura(page, 'paso6_inicio_medico', false);
    await page.goto(BASE + '/cola/');
    await page.waitForLoadState('networkidle');
    await captura(page, 'paso6_P-09_cola_medico', false);
    await page.click('#cuerpo tr:first-child a:has-text("Iniciar atención")');
    await page.waitForSelector('#nueva:not([hidden])');
    verificar(await page.isVisible('#enfermedadActual'), 'desde la cola aparecen los campos ampliados de la atención');
    await page.fill('#motivo', 'Dolor torácico opresivo de 2 horas');
    await page.fill('#enfermedadActual', 'Dolor retroesternal que se irradia al brazo izquierdo, con sudoración');
    await page.fill('#anamnesis', 'Paciente con hipertensión arterial; refiere dolor desde hace 2 horas.');
    await page.fill('#funcionesBiologicas', 'Apetito conservado, sueño alterado, orina normal');
    await page.fill('#examenFisico', 'Ansioso, sudoroso, ruidos cardíacos rítmicos');
    await page.fill('#examenSistemas', 'Cardiovascular: sin soplos. Respiratorio: murmullo vesicular conservado.');
    await captura(page, 'paso6_P-03_atencion_ampliada', true);
    await page.click('#formNueva button[type=submit]');
    await page.waitForURL('**/atencion/?id=*');
    await page.waitForSelector('#atencion:not([hidden])');
    datos.atencionUrl = page.url();
    await page.fill('#cie10', 'J02.9');
    await page.click('#formDiagnostico button[type=submit]');
    await page.waitForSelector('#mensajeDiagnostico.ok');
    await page.fill('#medicamento', 'Paracetamol');
    await page.fill('#dosis', '500 mg cada 8 horas');
    await page.fill('#duracion', '3 días');
    await page.fill('#indicacion', 'Reposo relativo y control en 48 horas');
    await page.click('#formTratamiento button[type=submit]');
    await page.waitForSelector('#mensajeTratamiento.ok');
    await page.fill('#tipoExamen', 'Hemograma completo');
    await page.fill('#indicacionesExamen', 'Urgente');
    await page.click('#formExamen button[type=submit]');
    await page.waitForSelector('#mensajeExamen.ok');
    const detalle = await page.textContent('#detalle');
    verificar(detalle.includes('Enfermedad actual') && detalle.includes('Examen por sistemas'), 'la atención guarda los datos ampliados');
    verificar(detalle.includes('Hemograma completo') && detalle.includes('pendiente de resultado'), 'el examen pedido figura pendiente en la atención');
    await captura(page, 'paso6_P-03_atencion_con_examen', true);
    await page.goto(BASE + '/cola/');
    await page.waitForLoadState('networkidle');
    verificar((await page.textContent('#cuerpo')).includes('En atención'), 'la admisión pasó a "en atención"');
  },

  /** Paso 7 (CU-23): apoyo al diagnostico ve el examen pendiente de su hospital y registra el resultado. */
  async 7(page) {
    const a = datos.personal.APOYO_DIAGNOSTICO;
    await entrarConTemporal(page, a.usuario, a.clave, 'Apoyo2026');
    await captura(page, 'paso7_inicio_apoyo', false);
    await page.goto(BASE + '/examenes/');
    await page.waitForLoadState('networkidle');
    await page.waitForSelector('#cuerpo tr');
    const fila = await page.textContent('#cuerpo tr:first-child');
    verificar(fila.includes('Vargas León') && fila.includes('Hemograma completo'), 'apoyo ve el examen pendiente con su paciente');
    await captura(page, 'paso7_P-12_examenes_pendientes', false);
    await page.click('#cuerpo tr:first-child button:has-text("Registrar resultado")');
    await page.waitForSelector('.hce-modal textarea');
    await page.fill('.hce-modal textarea', 'Hemoglobina 13,2 g/dL; leucocitos 11 200 por microlitro');
    await captura(page, 'paso7_P-12_resultado', false);
    await page.click('.hce-modal .primario');
    await page.waitForSelector('#mensaje.ok');
    verificar((await page.textContent('#pestanas')).includes('Con resultado 1'), 'el examen pasa a "con resultado"');
    await captura(page, 'paso7_P-12_examenes_resueltos', false);
  },

  /** Paso 8 (CU-05, RN-03): el medico ve el resultado en la atencion, la cierra y ya no se puede editar. */
  async 8(page) {
    await entrarConClave(page, datos.personal.MEDICO.usuario, 'Medico2026');
    await page.goto(datos.atencionUrl);
    await page.waitForSelector('#atencion:not([hidden])');
    const antes = await page.textContent('#detalle');
    verificar(antes.includes('Hemoglobina 13,2'), 'el médico ve el resultado del examen en la atención');
    await captura(page, 'paso8_P-03_resultado_visible', true);
    await page.click('#cerrar');
    await page.click('.hce-modal .primario');
    await page.waitForFunction(() => document.getElementById('estadoAtencion').textContent === 'Cerrada');
    verificar(await page.isHidden('#edicion'), 'la atención cerrada ya no muestra formularios de edición (RN-03)');
    verificar(await page.isVisible('#enmienda'), 'la corrección posterior es por enmienda');
    await captura(page, 'paso8_P-03_atencion_cerrada', true);
    await page.goto(BASE + '/cola/');
    await page.waitForLoadState('networkidle');
    verificar(!(await page.textContent('#cuerpo')).includes('Vargas León'), 'el paciente sale de la cola del médico al cerrar');
  },

  /** Paso 9 (CU-26, CU-10, CU-11, CU-25): admision restablece la clave del paciente; el paciente ve su historia cerrada,
      su resumen y gestiona su consentimiento; el administrador del hospital ve la auditoria. */
  async 9(page) {
    // Admision restablece la clave de Vargas Leon desde la lista de pacientes (la clave temporal se ve una sola vez)
    await entrarConClave(page, 'carlos.rios', 'Admision2026');
    await page.goto(BASE + '/pacientes/');
    await page.waitForLoadState('networkidle');
    await page.fill('#buscar', '73456729');
    await page.click('#cuerpo tr:first-child button:has-text("Restablecer clave")');
    await page.click('.hce-modal .primario');
    await page.waitForSelector('.hce-modal .hce-clave');
    const temporal = (await page.textContent('.hce-modal .hce-clave')).trim();
    verificar(/^[A-Za-z0-9]{10}$/.test(temporal), 'admisión restablece la clave del paciente y ve una clave temporal de 10 caracteres');
    await captura(page, 'paso9_P-02_restablecer_clave_paciente', false);
    await page.click('.hce-modal .primario');
    await page.context().close();

    // El paciente entra con la temporal, la cambia y ve su historial (solo atenciones cerradas, sin signos vitales)
    const paciente = await nuevaPagina();
    await entrarConTemporal(paciente, '73456729', temporal, 'Paciente2026', 'paso9');
    await paciente.goto(BASE + '/mi-historial/');
    await paciente.waitForLoadState('networkidle');
    await paciente.waitForSelector('#atenciones .hce-evento, #atenciones > *');
    const historial = await paciente.textContent('#contenido');
    verificar(historial.includes('Dolor torácico') && historial.includes('Enfermedad actual'), 'el paciente ve su atención cerrada con los datos ampliados');
    verificar(historial.includes('Hemograma completo') && historial.includes('Hemoglobina 13,2'), 'el paciente ve su examen con el resultado');
    verificar(!/signos vitales|saturaci/i.test(historial), 'el portal no muestra signos vitales');
    await captura(paciente, 'paso9_P-05_mi_historial', true);
    await paciente.click('#verResumen');
    await paciente.waitForSelector('#resumen:not([hidden])');
    verificar((await paciente.textContent('#resumenContenido')).includes('Hemoglobina 13,2'), 'el resumen descargable lista el examen con resultado');
    await captura(paciente, 'paso9_P-05_resumen', true);

    // Consentimiento: otorga y revoca
    await paciente.goto(BASE + '/consentimiento/');
    await paciente.waitForLoadState('networkidle');
    await paciente.fill('#otorgadoA', 'Hospital Regional de Chiclayo');
    await paciente.fill('#alcance', 'Lectura de mi historial para una interconsulta');
    await paciente.click('#formulario button[type=submit]');
    await paciente.waitForSelector('#mensaje.ok');
    verificar((await paciente.textContent('#lista')).includes('Hospital Regional de Chiclayo'), 'el consentimiento otorgado aparece en la lista');
    await captura(paciente, 'paso9_P-06_consentimiento_otorgado', false);
    const revocadasAntes = await paciente.locator('#lista .hce-autorizacion.revocada').count();
    await paciente.click('#lista .hce-autorizacion:not(.revocada) input[type=checkbox]');
    await paciente.click('.hce-modal .primario');
    await paciente.waitForSelector('#mensajeLista.ok');
    verificar(await paciente.locator('#lista .hce-autorizacion.revocada').count() === revocadasAntes + 1, 'al apagar el interruptor el consentimiento queda revocado');
    await captura(paciente, 'paso9_P-06_consentimiento_revocado', false);
    await paciente.context().close();

    // El administrador del hospital ve la auditoria de todo lo anterior
    const admin = await nuevaPagina();
    await entrarConClave(admin, 'admin.trujillo', 'Hospital2026');
    await admin.goto(BASE + '/auditoria/');
    await admin.waitForLoadState('networkidle');
    await admin.waitForSelector('#cuerpo tr');
    await admin.selectOption('#accion', 'RESTABLECER_CLAVE');
    await admin.click('#filtros button[type=submit]');
    await admin.waitForFunction(() => document.getElementById('cuerpo').textContent.includes('RESTABLECER_CLAVE'));
    verificar((await admin.textContent('#cuerpo')).includes('carlos.rios'), 'la auditoría muestra quién restableció la clave');
    await captura(admin, 'paso9_P-14_auditoria_restablecer', false);
    await admin.selectOption('#accion', 'SOLICITAR_EXAMEN');
    await admin.click('#filtros button[type=submit]');
    await admin.waitForFunction(() => document.getElementById('cuerpo').textContent.includes('SOLICITAR_EXAMEN'));
    verificar((await admin.textContent('#cuerpo')).includes('jorge.paz'), 'la auditoría muestra al médico que pidió el examen');
    await captura(admin, 'paso9_P-14_auditoria_examen', false);
    await admin.context().close();
  },

  /** Paso 10, controles transversales (MV-21 a MV-23): un segundo hospital no ve nada del primero (RN-06), los roles no
      pasan sus permisos (RN-02) y 5 claves malas bloquean la cuenta hasta que el administrador la restablece (RN-12).
      Usa claves fijas de los pasos 1 a 9 y un sufijo propio, asi que puede repetirse: node recorrido.mjs 10 10 */
  async 10(page) {
    const numero = String(Date.now());
    const sufijo = numero.slice(-5);
    const estado = async (p, ruta, metodo = 'GET') => p.evaluate(async ([r, m]) => (await fetch(r, { method: m })).status, [ruta, metodo]);

    // Segundo hospital con su administrador y su admision (por pantallas)
    await entrarConClave(page, 'admin', 'Plataforma2026');
    await page.goto(BASE + '/hospitales/');
    await page.waitForLoadState('networkidle');
    await page.click('#nuevo');
    await page.fill('#nombre', 'Hospital Regional de Chiclayo ' + sufijo);
    await page.fill('#codigo', '00' + numero.slice(-6));
    await page.fill('#region', 'Lambayeque');
    await page.selectOption('#plan', 'BASICO');
    await page.fill('#adminNombre', 'Marta Rojas Díaz');
    await page.fill('#adminDocumento', '46789123');
    await page.fill('#adminCorreo', 'mrojas@example.com');
    await page.fill('#adminUsuario', 'admin.chiclayo' + sufijo);
    await page.click('#formulario button[type=submit]');
    await page.waitForSelector('#constancia:not([hidden])');
    const admin2 = { usuario: await page.textContent('#cUsuario'), clave: await page.textContent('#cClave') };
    await captura(page, 'paso10_P-07_segundo_hospital', false);

    const adm = await nuevaPagina();
    await entrarConTemporal(adm, admin2.usuario, admin2.clave, 'Chiclayo2026');
    await adm.goto(BASE + '/usuarios/');
    await adm.waitForLoadState('networkidle');
    await adm.click('#nuevo');
    await adm.fill('#nombres', 'Luis');
    await adm.fill('#apellidos', 'Mora Vega');
    await adm.fill('#documento', '47891234');
    await adm.fill('#correo', 'lmora@example.com');
    await adm.fill('#usuario', 'luis.mora' + sufijo);
    await adm.selectOption('#rol', 'ADMISION');
    await adm.click('#formulario button[type=submit]');
    await adm.waitForSelector('#constancia:not([hidden])');
    const luis = { usuario: 'luis.mora' + sufijo, clave: await adm.textContent('#cClave') };
    await adm.click('#cerrarConstancia');

    // MV-21: la admision del segundo hospital no ve nada del primero
    const op = await nuevaPagina();
    await entrarConTemporal(op, luis.usuario, luis.clave, 'Admision2026b');
    await op.goto(BASE + '/pacientes/');
    await op.waitForLoadState('networkidle');
    const lista = await op.textContent('#contenido');
    verificar(!lista.includes('Gutiérrez') && !lista.includes('Vargas'), 'el segundo hospital no ve a los pacientes del primero');
    await captura(op, 'paso10_P-02_segundo_hospital_sin_pacientes', false);
    verificar([403, 404].includes(await estado(op, '/api/historias/1')), 'pedir la historia de otro hospital por la API se rechaza');
    verificar([403, 404].includes(await estado(op, '/api/atenciones/1')), 'pedir la atención de otro hospital por la API se rechaza');
    await adm.goto(BASE + '/auditoria/');
    await adm.waitForLoadState('networkidle');
    await adm.waitForSelector('#cuerpo tr');
    const auditoria = await adm.textContent('#cuerpo');
    verificar(!auditoria.includes('jorge.paz') && !auditoria.includes('carlos.rios'), 'la auditoría del segundo hospital no muestra acciones del primero');
    await captura(adm, 'paso10_P-14_auditoria_segundo_hospital', false);

    // MV-22: cada rol solo hace lo suyo (el servidor lo impone, no solo el menu)
    const enf = await nuevaPagina();
    await entrarConClave(enf, 'ana.flores', 'Enfermeria2026');
    const menu = await enf.textContent('#app');
    verificar(!menu.includes('Usuarios') && !menu.includes('Hospitales') && !menu.includes('Exámenes'), 'el menú de enfermería no ofrece usuarios, hospitales ni exámenes');
    verificar(await estado(enf, '/api/usuarios') === 403, 'enfermería no puede pedir la lista de usuarios');
    verificar(await estado(enf, '/api/examenes') === 403, 'enfermería no puede ver los exámenes');
    verificar(await estado(enf, '/api/restablecimientos/personal/1', 'POST') === 403, 'enfermería no puede restablecer claves');
    const med = await nuevaPagina();
    await entrarConClave(med, 'jorge.paz', 'Medico2026');
    verificar(await estado(med, '/api/examenes') === 403, 'el médico no registra resultados de exámenes');
    verificar(await estado(med, '/api/indicadores/hospitales') === 403, 'el médico no ve la lista de hospitales');

    // MV-23: 5 claves malas bloquean la cuenta; el administrador del hospital restablece la clave y se levanta
    const intento = async (clave) => (await op.request.post(BASE + '/api/login', { data: { usuario: luis.usuario, clave } }));
    for (let i = 0; i < 5; i++) verificar((await intento('incorrecta1')).status() === 401, 'intento fallido ' + (i + 1) + ' rechazado');
    const bloqueada = await intento('Admision2026b');
    verificar(bloqueada.status() === 401 && JSON.stringify(await bloqueada.json()).includes('bloqueada'), 'tras 5 intentos la cuenta queda bloqueada, aunque la clave sea la correcta');
    await adm.goto(BASE + '/usuarios/');
    await adm.waitForLoadState('networkidle');
    await adm.fill('#buscar', luis.usuario);
    await adm.click('#cuerpo tr:first-child button:has-text("Restablecer clave")');
    await adm.click('.hce-modal .primario');
    await adm.waitForSelector('.hce-modal .hce-clave');
    const nueva = (await adm.textContent('.hce-modal .hce-clave')).trim();
    await captura(adm, 'paso10_P-08_restablecer_bloqueada', false);
    verificar((await intento(nueva)).status() === 200, 'tras restablecer la clave, la cuenta entra de nuevo');
    for (const p of [adm, op, enf, med]) await p.context().close();
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
  for (let n = primero; n <= ultimo; n++) {
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
