/* P-07 Hospitales: lista y alta (CU-16; RF-20; RN-16, RN-14; DEC-11). Solo el administrador de plataforma.
   La lista trae SOLO totales por hospital. Al dar de alta, la clave temporal se muestra una sola vez. */
const $ = (id) => document.getElementById(id);
const PLANES = { BASICO: 'Básico', ESTANDAR: 'Estándar', COMPLETO: 'Completo' };
const CAMPOS = ['nombre', 'codigo', 'region', 'plan', 'adminNombre', 'adminDocumento', 'adminCorreo', 'adminUsuario'];

function cifra(titulo, valor) {
  const c = HCE.el('div', 'cifra');
  c.append(HCE.el('div', 'valor', String(valor)), HCE.el('div', 'etiqueta', titulo));
  return c;
}

async function cargar() {
  const r = await HCE.api('/api/indicadores/hospitales');
  if (!r.ok) { HCE.mostrar($('error'), (r.cuerpo && r.cuerpo.mensaje) || 'No se pudo cargar la lista de hospitales.', 'error'); return; }
  const lista = $('lista');
  lista.replaceChildren();
  $('vacio').hidden = r.cuerpo.length > 0;
  r.cuerpo.forEach(h => {
    const tarjeta = HCE.el('article', 'hce-tarjeta hce-hospital-tarjeta');
    const cab = HCE.el('div', 'hce-evento-cabecera');
    const titulo = HCE.el('div');
    titulo.append(HCE.el('h2', null, h.nombre), HCE.el('div', 'hce-subtitulo', 'Código ' + h.codigoIpress + ' · ' + h.region));
    const chips = HCE.el('div', 'hce-fila-chips');
    if (h.plan) chips.append(HCE.el('span', 'hce-chip info', 'Plan ' + (PLANES[h.plan] || h.plan)));
    chips.append(HCE.el('span', 'hce-chip ' + (h.estado === 'ACTIVO' ? 'ok' : 'error'), h.estado === 'ACTIVO' ? 'Activo' : h.estado));
    cab.append(titulo, chips);
    const cifras = HCE.el('div', 'cifras');
    cifras.append(cifra('Usuarios', h.usuarios), cifra('Pacientes', h.pacientes), cifra('Atenciones', h.atenciones));
    tarjeta.append(cab, cifras);
    lista.append(tarjeta);
  });
}

function abrirFormulario(abrir) {
  $('formularioTarjeta').hidden = !abrir;
  $('nuevo').hidden = abrir;
  $('mensaje').className = 'hce-mensaje';
  if (abrir) $('nombre').focus();
}

$('nuevo').addEventListener('click', () => { $('constancia').hidden = true; abrirFormulario(true); });
$('cancelar').addEventListener('click', () => { $('formulario').reset(); abrirFormulario(false); });

$('formulario').addEventListener('submit', async (e) => {
  e.preventDefault();
  const datos = {};
  CAMPOS.forEach(c => datos[c] = $(c).value.trim());
  const r = await HCE.api('/api/hospitales', { method: 'POST', cuerpo: datos });
  if (r.status !== 201) {
    HCE.mostrar($('mensaje'), (r.cuerpo && r.cuerpo.mensaje) || 'No se pudo dar de alta el hospital.', 'error');
    return;
  }
  const c = r.cuerpo;
  $('constanciaTitulo').textContent = c.hospital + ' (código ' + c.codigo + ')';
  $('cNombre').textContent = c.nombreAdministrador;
  $('cUsuario').textContent = c.usuario;
  $('cClave').textContent = c.claveTemporal;
  $('cPlan').textContent = (PLANES[c.plan] || c.plan) + ' · vence el ' + c.vence;
  e.target.reset();
  abrirFormulario(false);
  $('constancia').hidden = false;
  $('constancia').scrollIntoView({ behavior: 'smooth' });
  await cargar();
});

$('imprimir').addEventListener('click', () => HCE.imprimir());
// Al cerrar la constancia la clave desaparece de la pantalla: no se vuelve a mostrar.
$('cerrarConstancia').addEventListener('click', () => { $('constancia').hidden = true; $('cClave').textContent = ''; });

(async () => {
  const sesion = await HCE.iniciarPantalla();
  if (sesion) await cargar();
})();
