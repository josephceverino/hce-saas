/* Utilidades comunes del frontend HCE. Sin librerias ni CDN.
   El menu lo decide el servidor (MenuService, DEC-03, RN-02): aqui solo se dibuja. Ocultar un enlace no es seguridad. */
const HCE = (() => {
  const SVG = (trazos) => '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">' + trazos + '</svg>';
  /* Iconos propios por clave de menu (no vienen del usuario, por eso se pueden insertar como HTML). */
  const ICONOS = {
    inicio: SVG('<path d="M3 11l9-8 9 8"/><path d="M5 10v10h14V10"/>'),
    cola: SVG('<path d="M8 6h13"/><path d="M8 12h13"/><path d="M8 18h13"/><circle cx="4" cy="6" r="1"/><circle cx="4" cy="12" r="1"/><circle cx="4" cy="18" r="1"/>'),
    pacientes: SVG('<path d="M17 21v-2a4 4 0 0 0-4-4H7a4 4 0 0 0-4 4v2"/><circle cx="10" cy="7" r="4"/><path d="M21 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/>'),
    'registrar-paciente': SVG('<path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M19 8v6"/><path d="M22 11h-6"/>'),
    historia: SVG('<path d="M14 3H6a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V9z"/><path d="M14 3v6h6"/><path d="M8 13h8"/><path d="M8 17h5"/>'),
    atencion: SVG('<path d="M12 5v14"/><path d="M5 12h14"/><rect x="3" y="3" width="18" height="18" rx="3"/>'),
    'mi-historial': SVG('<path d="M12 8v4l3 2"/><circle cx="12" cy="12" r="9"/>'),
    consentimiento: SVG('<path d="M12 3l8 3v6c0 5-3.5 8-8 9-4.5-1-8-4-8-9V6z"/><path d="M9 12l2 2 4-4"/>'),
    usuarios: SVG('<path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M22 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/>'),
    auditoria: SVG('<circle cx="11" cy="11" r="7"/><path d="M21 21l-4.3-4.3"/>'),
    hospitales: SVG('<path d="M3 21h18"/><path d="M5 21V7l7-4 7 4v14"/><path d="M12 9v6"/><path d="M9 12h6"/>'),
    examenes: SVG('<path d="M9 3h6"/><path d="M10 3v6L4.5 19a1.5 1.5 0 0 0 1.3 2.2h12.4a1.5 1.5 0 0 0 1.3-2.2L14 9V3"/><path d="M7.5 15h9"/>'),
    perfil: SVG('<circle cx="12" cy="8" r="4"/><path d="M4 21v-1a6 6 0 0 1 6-6h4a6 6 0 0 1 6 6v1"/>')
  };

  /** Llama a la API y devuelve {ok, status, cuerpo}. Un 401 manda al login. */
  async function api(ruta, opciones = {}) {
    const config = { method: opciones.method || 'GET', headers: {} };
    if (opciones.cuerpo !== undefined) {
      config.headers['Content-Type'] = 'application/json';
      config.body = JSON.stringify(opciones.cuerpo);
    }
    const respuesta = await fetch(ruta, config);
    let cuerpo = null;
    try { cuerpo = await respuesta.json(); } catch (e) { /* sin cuerpo */ }
    if (respuesta.status === 401 && !opciones.sinRedireccion) {
      window.location.href = '/login/index.html';
    }
    return { ok: respuesta.ok, status: respuesta.status, cuerpo };
  }

  function mostrar(caja, texto, tipo) {
    caja.textContent = texto;
    caja.className = 'hce-mensaje ' + tipo;
  }

  function el(etiqueta, clase, texto) {
    const nodo = document.createElement(etiqueta);
    if (clase) nodo.className = clase;
    if (texto !== undefined) nodo.textContent = texto;
    return nodo;
  }

  /** Primera pantalla de cada rol: la primera opcion del menu que entrega el servidor. */
  function rutaInicial(sesion) {
    if (sesion && sesion.debeCambiarClave) return '/cambiar-clave/'; // RN-14, CU-18
    return (sesion && sesion.menu && sesion.menu.length) ? sesion.menu[0].ruta : '/login/index.html';
  }

  /** Indica si el menu del usuario trae una opcion (para mostrar o no un enlace; el servidor igual valida). */
  function tieneOpcion(sesion, clave) {
    return !!(sesion && sesion.menu && sesion.menu.some(o => o.clave === clave));
  }

  function esRutaActual(ruta) {
    const ahora = window.location.pathname;
    return ahora === ruta || (ruta.endsWith('/') && ahora === ruta + 'index.html');
  }

  /** Exige sesion, dibuja sidebar y barra superior y devuelve los datos de la sesion. */
  async function iniciarPantalla() {
    const r = await api('/api/sesion');
    if (!r.ok) return null;
    const sesion = r.cuerpo;
    // RN-14 (CU-18): con clave temporal no se usa otra pantalla. El servidor tambien lo rechaza (403).
    if (sesion.debeCambiarClave) { window.location.replace('/cambiar-clave/'); return null; }
    const app = document.getElementById('app');

    const sidebar = el('aside', 'hce-sidebar');
    const marca = el('div', 'hce-marca');
    const logo = el('img'); logo.src = '/logo.svg'; logo.alt = '';
    marca.append(logo, el('span', null, 'Plataforma HCE'));
    const menu = el('nav', 'hce-menu');
    menu.setAttribute('aria-label', 'Menú principal');
    menu.append(el('div', 'hce-menu-titulo', 'Menú principal'));
    (sesion.menu || []).forEach(opcion => {
      const enlace = el('a', esRutaActual(opcion.ruta) ? 'activo' : '');
      enlace.href = opcion.ruta;
      const icono = el('span', 'icono');
      if (ICONOS[opcion.clave]) icono.insertAdjacentHTML('afterbegin', ICONOS[opcion.clave]);
      enlace.append(icono, document.createTextNode(opcion.titulo));
      menu.append(enlace);
    });
    const perfil = el('div', 'hce-perfil');
    const avatar = el('span', 'hce-avatar-mini', sesion.usuario.slice(0, 2).toUpperCase());
    const datos = el('div', 'datos');
    datos.append(el('div', 'nombre', sesion.usuario), el('div', 'rol', sesion.rol));
    const salir = el('button', 'hce-boton-icono');
    salir.type = 'button';
    salir.title = 'Cerrar sesión';
    salir.setAttribute('aria-label', 'Cerrar sesión');
    salir.insertAdjacentHTML('afterbegin', SVG('<path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/><path d="M16 17l5-5-5-5"/><path d="M21 12H9"/>'));
    salir.addEventListener('click', async () => {
      await api('/api/logout', { method: 'POST', sinRedireccion: true });
      window.location.href = '/login/index.html';
    });
    perfil.append(avatar, datos, salir);
    sidebar.append(marca, menu, perfil);

    const barra = el('header', 'hce-barra');
    const contexto = el('div', 'contexto');
    // El paciente y el administrador de plataforma no pertenecen a un hospital: el servidor manda una etiqueta, no un hospital.
    if (sesion.rolCodigo === 'PACIENTE' || sesion.rolCodigo === 'ADMIN_PLATAFORMA') {
      contexto.append(el('span', 'hce-chip', sesion.establecimiento));
    } else {
      contexto.append(el('span', 'hce-chip', 'Hospital activo'), el('span', 'hce-hospital', sesion.establecimiento));
    }
    const usuarioBarra = el('div', 'hce-usuario-barra');
    const textoUsuario = el('div', 'datos');
    textoUsuario.append(el('div', 'nombre', sesion.usuario), el('div', 'rol', sesion.rol));
    usuarioBarra.append(el('span', 'hce-avatar-mini', sesion.usuario.slice(0, 2).toUpperCase()), textoUsuario);
    barra.append(contexto, usuarioBarra);

    app.prepend(sidebar);
    app.insertBefore(barra, document.getElementById('contenido'));
    return sesion;
  }

  /* ---- Ayudas de presentacion (solo formato; ninguna regla de negocio vive aqui) ---- */
  function fechaHora(texto) {
    const d = new Date(texto);
    return isNaN(d) ? String(texto) : d.toLocaleString('es-PE', { dateStyle: 'medium', timeStyle: 'short' });
  }

  function edad(fechaNacimiento) {
    const n = new Date(fechaNacimiento);
    if (isNaN(n)) return '';
    const hoy = new Date();
    let años = hoy.getFullYear() - n.getFullYear();
    if (hoy.getMonth() < n.getMonth() || (hoy.getMonth() === n.getMonth() && hoy.getDate() < n.getDate())) años--;
    return años;
  }

  function iniciales(nombres, apellidos) {
    return ((nombres || '').trim().charAt(0) + (apellidos || '').trim().charAt(0)).toUpperCase();
  }

  /** Modal de confirmacion propio. Devuelve una promesa con true (confirma) o false (cancela). */
  function confirmar(titulo, texto, textoBoton) {
    return new Promise(resolver => {
      const fondo = el('div', 'hce-modal-fondo');
      const caja = el('div', 'hce-modal');
      caja.setAttribute('role', 'dialog');
      caja.setAttribute('aria-modal', 'true');
      const cancelar = el('button', 'hce-boton claro', 'Cancelar');
      const aceptar = el('button', 'hce-boton primario', textoBoton || 'Confirmar');
      cancelar.type = aceptar.type = 'button';
      const cerrar = (valor) => { fondo.remove(); resolver(valor); };
      cancelar.addEventListener('click', () => cerrar(false));
      aceptar.addEventListener('click', () => cerrar(true));
      fondo.addEventListener('click', (e) => { if (e.target === fondo) cerrar(false); });
      const botones = el('div', 'hce-acciones');
      botones.append(cancelar, aceptar);
      caja.append(el('h2', null, titulo), el('p', null, texto), botones);
      fondo.append(caja);
      document.body.append(fondo);
      cancelar.focus();
    });
  }

  /** CU-26: restablece una clave (POST) tras confirmar y muestra la clave temporal UNA sola vez en un modal. */
  async function restablecerClave(ruta, nombre, caja) {
    const ok = await confirmar('¿Restablecer la clave de ' + nombre + '?',
      'Se generará una clave temporal. La clave actual dejará de servir y deberá cambiarla en su próximo ingreso.',
      'Sí, restablecer');
    if (!ok) return;
    const r = await api(ruta, { method: 'POST' });
    if (!r.ok) { mostrar(caja, (r.cuerpo && r.cuerpo.mensaje) || 'No se pudo restablecer la clave.', 'error'); return; }
    await new Promise(resolver => {
      const fondo = el('div', 'hce-modal-fondo');
      const modal = el('div', 'hce-modal');
      modal.setAttribute('role', 'dialog');
      modal.setAttribute('aria-modal', 'true');
      const cerrar = el('button', 'hce-boton primario', 'Listo');
      cerrar.type = 'button';
      cerrar.addEventListener('click', () => { fondo.remove(); resolver(); });
      const acciones = el('div', 'hce-acciones');
      acciones.append(cerrar);
      const datos = el('div', 'hce-constancia-datos');
      const dato = (etiqueta, valor, clase) => {
        const d = el('div', 'hce-dato');
        d.append(el('div', 'etiqueta', etiqueta), el('div', 'valor ' + (clase || ''), valor));
        datos.append(d);
      };
      dato('Usuario', r.cuerpo.usuario, 'hce-monoespacio');
      dato('Clave temporal', r.cuerpo.claveTemporal, 'hce-monoespacio hce-clave');
      modal.append(el('h2', null, 'Clave restablecida'), datos,
        el('div', 'hce-aviso-alerta', 'Esta clave se muestra una sola vez. Entrégala a la persona: deberá cambiarla en su primer ingreso.'),
        acciones);
      fondo.append(modal);
      document.body.append(fondo);
      cerrar.focus();
    });
  }

  /** Modal para elegir una opcion. opciones: [{valor, texto, descripcion, clase}]. Devuelve el valor o null. */
  function elegir(titulo, texto, opciones) {
    return new Promise(resolver => {
      const fondo = el('div', 'hce-modal-fondo');
      const caja = el('div', 'hce-modal');
      caja.setAttribute('role', 'dialog');
      caja.setAttribute('aria-modal', 'true');
      const cerrar = (valor) => { fondo.remove(); resolver(valor); };
      const lista = el('div', 'hce-opciones');
      opciones.forEach(o => {
        const b = el('button', 'hce-opcion ' + (o.clase || ''));
        b.type = 'button';
        b.append(el('span', 'titulo', o.texto), el('span', 'nota', o.descripcion || ''));
        b.addEventListener('click', () => cerrar(o.valor));
        lista.append(b);
      });
      const cancelar = el('button', 'hce-boton claro', 'Cancelar');
      cancelar.type = 'button';
      cancelar.addEventListener('click', () => cerrar(null));
      fondo.addEventListener('click', (e) => { if (e.target === fondo) cerrar(null); });
      const pie = el('div', 'hce-acciones');
      pie.append(cancelar);
      caja.append(el('h2', null, titulo), el('p', null, texto), lista, pie);
      fondo.append(caja);
      document.body.append(fondo);
      lista.querySelector('button').focus();
    });
  }

  /** Contenido de solo lectura de una atencion (AtencionDto): texto clinico, diagnosticos, tratamientos y enmiendas. */
  function detalleAtencion(a) {
    const caja = el('div', 'hce-atencion-detalle');
    const dato = (titulo, texto) => {
      const bloque = el('div', 'hce-dato');
      bloque.append(el('div', 'etiqueta', titulo), el('div', 'valor', texto));
      caja.append(bloque);
    };
    dato('Motivo de consulta', a.motivo);
    if (a.enfermedadActual) dato('Enfermedad actual', a.enfermedadActual);
    dato('Anamnesis', a.anamnesis);
    if (a.funcionesBiologicas) dato('Funciones biológicas', a.funcionesBiologicas);
    dato('Examen físico', a.examenFisico);
    if (a.examenSistemas) dato('Examen por sistemas', a.examenSistemas);
    const lista = (titulo, items, texto) => {
      const bloque = el('div', 'hce-dato');
      bloque.append(el('div', 'etiqueta', titulo));
      if (!items || items.length === 0) {
        bloque.append(el('div', 'valor suave', 'Sin registros.'));
      } else {
        const ul = el('ul', 'hce-lista');
        items.forEach(i => ul.append(el('li', null, texto(i))));
        bloque.append(ul);
      }
      caja.append(bloque);
    };
    lista('Diagnósticos (CIE-10)', a.diagnosticos, d => d.codigoCie10 + ' · ' + d.descripcion + ' (' + d.tipo.toLowerCase() + ')');
    lista('Tratamiento e indicaciones', a.tratamientos,
      t => t.medicamento + ' ' + t.dosis + ' por ' + t.duracion + '. ' + t.indicacion);
    if (a.examenes && a.examenes.length) {
      lista('Exámenes solicitados', a.examenes, x => x.tipoExamen + (x.indicaciones ? ' (' + x.indicaciones + ')' : '') + ' · ' +
        (x.resultado ? 'Resultado del ' + x.fechaResultado + ': ' + x.resultado : 'pendiente de resultado'));
    }
    if (a.enmiendas && a.enmiendas.length) {
      lista('Enmiendas', a.enmiendas, e => fechaHora(e.fecha) + ' · ' + e.autor + ': ' + e.motivo + '. ' + e.contenidoCorregido);
    }
    return caja;
  }

  /** Imprime solo los bloques marcados con la clase "imprimible" (constancias de acceso, resumen). */
  function imprimir() {
    document.body.classList.add('imprimiendo');
    window.addEventListener('afterprint', () => document.body.classList.remove('imprimiendo'), { once: true });
    window.print();
  }

  return { api, mostrar, el, imprimir, elegir, iniciarPantalla, rutaInicial, tieneOpcion, fechaHora, edad, iniciales, confirmar, restablecerClave, detalleAtencion };
})();
