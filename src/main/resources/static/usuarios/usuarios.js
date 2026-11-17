/* P-08 Usuarios del hospital (CU-17; RF-11, RF-12; RN-16, RN-14). Solo el administrador del hospital.
   El servidor decide el hospital (sesion) y que roles se pueden asignar. Las cifras salen de la lista real. */
const $ = (id) => document.getElementById(id);
const POR_PAGINA = 8;
const CLINICOS = ['MEDICO', 'ENFERMERIA'];
const PROFESIONALES = ['MEDICO', 'ENFERMERIA', 'APOYO_DIAGNOSTICO'];
const COLOR_ROL = { ADMIN_HOSPITAL: 'error', MEDICO: 'info', ENFERMERIA: 'ok', ADMISION: 'alerta', APOYO_DIAGNOSTICO: '' };
const FILTROS = [
  { clave: 'todos', texto: 'Todos', prueba: () => true },
  { clave: 'activos', texto: 'Activos', prueba: u => u.estado === 'ACTIVO' && !u.debeCambiarClave },
  { clave: 'pendientes', texto: 'Clave pendiente', prueba: u => u.estado === 'ACTIVO' && u.debeCambiarClave },
  { clave: 'inactivos', texto: 'Inactivos', prueba: u => u.estado !== 'ACTIVO' }
];
let todos = [];
let filtro = 'todos';
let pagina = 0;
let yo = null;

function normalizar(t) { return String(t || '').normalize('NFD').replace(/[̀-ͯ]/g, '').toLowerCase(); }

function estadoDe(u) {
  if (u.estado !== 'ACTIVO') return { texto: 'Inactivo', clase: 'inactivo' };
  if (u.debeCambiarClave) return { texto: 'Clave pendiente', clase: 'pendiente' };
  return { texto: 'Activo', clase: 'activo' };
}

function dibujarCifras() {
  const cont = $('cifras');
  cont.replaceChildren();
  const datos = [
    ['Total', todos.length, 'En tu hospital', ''],
    ['Activos', todos.filter(FILTROS[1].prueba).length, 'Ya cambiaron su clave', 'ok'],
    ['Clave pendiente', todos.filter(FILTROS[2].prueba).length, 'Aún no entran por primera vez', 'alerta'],
    ['Inactivos', todos.filter(FILTROS[3].prueba).length, 'No pueden iniciar sesión', 'error']
  ];
  datos.forEach(([titulo, valor, nota, clase]) => {
    const t = HCE.el('article', 'hce-cifra-estado ' + clase);
    t.append(HCE.el('div', 'titulo', titulo), HCE.el('div', 'valor', String(valor)), HCE.el('div', 'nota', nota));
    cont.append(t);
  });
  const pestanas = $('pestanas');
  pestanas.replaceChildren();
  FILTROS.forEach(f => {
    const b = HCE.el('button', 'hce-pestana' + (f.clave === filtro ? ' activa' : ''));
    b.type = 'button';
    b.setAttribute('role', 'tab');
    b.append(document.createTextNode(f.texto + ' '), HCE.el('span', 'cuenta', String(todos.filter(f.prueba).length)));
    b.addEventListener('click', () => { filtro = f.clave; pagina = 0; dibujarCifras(); dibujarTabla(); });
    pestanas.append(b);
  });
}

function accion(u) {
  if (yo && u.id === yo) return HCE.el('span', 'hce-subtitulo', 'Tu cuenta');
  const activo = u.estado === 'ACTIVO';
  const b = HCE.el('button', 'hce-boton claro pequeno', activo ? 'Desactivar' : 'Reactivar');
  b.type = 'button';
  b.addEventListener('click', async () => {
    if (activo) {
      const ok = await HCE.confirmar('¿Desactivar a ' + u.usuario + '?', 'No podrá iniciar sesión hasta que lo reactives.', 'Sí, desactivar');
      if (!ok) return;
    }
    const r = await HCE.api('/api/usuarios/' + u.id + (activo ? '/desactivacion' : '/reactivacion'), { method: 'POST' });
    if (!r.ok) { HCE.mostrar($('error'), (r.cuerpo && r.cuerpo.mensaje) || 'No se pudo cambiar el estado.', 'error'); return; }
    await cargar();
  });
  return b;
}

function dibujarTabla() {
  const f = FILTROS.find(x => x.clave === filtro);
  const q = normalizar($('buscar').value.trim());
  const lista = todos.filter(f.prueba).filter(u => !q || normalizar([u.nombre, u.usuario, u.correo].join(' ')).includes(q));
  const paginas = Math.max(1, Math.ceil(lista.length / POR_PAGINA));
  if (pagina >= paginas) pagina = paginas - 1;
  const visibles = lista.slice(pagina * POR_PAGINA, (pagina + 1) * POR_PAGINA);
  const cuerpo = $('cuerpo');
  cuerpo.replaceChildren();
  visibles.forEach(u => {
    const tr = document.createElement('tr');
    const persona = HCE.el('td');
    const caja = HCE.el('div', 'hce-persona');
    const texto = HCE.el('div');
    texto.append(HCE.el('div', 'nombre', u.nombre || u.usuario), HCE.el('div', 'nota', u.correo || ''));
    caja.append(HCE.el('span', 'hce-avatar-mini', (u.nombre || u.usuario).replace(/[^A-Za-zÁÉÍÓÚÑáéíóúñ ]/g, '').split(' ').filter(Boolean).slice(-2).map(x => x[0]).join('').toUpperCase() || 'U'), texto);
    persona.append(caja);
    const rol = HCE.el('td');
    rol.append(HCE.el('span', 'hce-chip ' + (COLOR_ROL[u.rolCodigo] || ''), u.rol));
    if (u.colegiatura) rol.append(HCE.el('div', 'hce-nota-celda', 'Colegiatura ' + u.colegiatura));
    const estado = HCE.el('td');
    const e = estadoDe(u);
    estado.append(HCE.el('span', 'hce-estado ' + e.clase, e.texto));
    const acc = HCE.el('td', 'derecha');
    acc.append(accion(u));
    tr.append(persona, HCE.el('td', 'hce-monoespacio-celda', u.usuario), rol, estado,
      HCE.el('td', null, u.ultimoAcceso ? HCE.fechaHora(u.ultimoAcceso) : 'Nunca'), acc);
    cuerpo.append(tr);
  });
  const vacio = $('vacio');
  vacio.hidden = lista.length > 0;
  vacio.textContent = todos.length <= 1 ? 'Aún no hay personal registrado. Usa "Nuevo usuario" para crear el primero.' : 'No hay usuarios que coincidan.';
  $('rango').textContent = lista.length ? 'Mostrando ' + (pagina * POR_PAGINA + 1) + ' a ' + (pagina * POR_PAGINA + visibles.length) + ' de ' + lista.length : '';
  $('anterior').disabled = pagina === 0;
  $('siguiente').disabled = pagina >= paginas - 1;
}

async function cargar() {
  const r = await HCE.api('/api/usuarios');
  if (!r.ok) { HCE.mostrar($('error'), (r.cuerpo && r.cuerpo.mensaje) || 'No se pudo cargar la lista de usuarios.', 'error'); return; }
  todos = r.cuerpo;
  dibujarCifras();
  dibujarTabla();
}

function abrirFormulario(abrir) {
  $('formularioTarjeta').hidden = !abrir;
  $('nuevo').hidden = abrir;
  $('mensaje').className = 'hce-mensaje';
  if (abrir) { $('constancia').hidden = true; $('nombres').focus(); }
}

function ajustarCamposClinicos() {
  const rol = $('rol').value;
  $('campoColegiatura').hidden = !PROFESIONALES.includes(rol);
  $('campoEspecialidad').hidden = !PROFESIONALES.includes(rol);
  $('colegiatura').required = CLINICOS.includes(rol);
}

$('rol').addEventListener('change', ajustarCamposClinicos);
$('nuevo').addEventListener('click', () => abrirFormulario(true));
$('cancelar').addEventListener('click', () => { $('formulario').reset(); ajustarCamposClinicos(); abrirFormulario(false); });
$('buscar').addEventListener('input', () => { pagina = 0; dibujarTabla(); });
$('anterior').addEventListener('click', () => { pagina--; dibujarTabla(); });
$('siguiente').addEventListener('click', () => { pagina++; dibujarTabla(); });
$('imprimir').addEventListener('click', () => HCE.imprimir());
$('cerrarConstancia').addEventListener('click', () => { $('constancia').hidden = true; $('cClave').textContent = ''; });

$('formulario').addEventListener('submit', async (e) => {
  e.preventDefault();
  const datos = {};
  ['nombres', 'apellidos', 'documento', 'correo', 'usuario', 'rol', 'colegiatura', 'especialidad'].forEach(c => datos[c] = $(c).value.trim());
  const r = await HCE.api('/api/usuarios', { method: 'POST', cuerpo: datos });
  if (r.status !== 201) { HCE.mostrar($('mensaje'), (r.cuerpo && r.cuerpo.mensaje) || 'No se pudo crear el usuario.', 'error'); return; }
  const u = r.cuerpo.usuario;
  $('constanciaTitulo').textContent = u.nombre;
  $('cNombre').textContent = u.nombre + ' · documento ' + u.documento;
  $('cRol').textContent = u.rol;
  $('cUsuario').textContent = u.usuario;
  $('cClave').textContent = r.cuerpo.claveTemporal;
  e.target.reset();
  ajustarCamposClinicos();
  abrirFormulario(false);
  $('constancia').hidden = false;
  await cargar();
});

(async () => {
  const sesion = await HCE.iniciarPantalla();
  if (!sesion) return;
  ajustarCamposClinicos();
  await cargar();
  const propio = todos.find(u => u.usuario === sesion.usuario);
  yo = propio ? propio.id : null;
  dibujarTabla();
})();
