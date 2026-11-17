/* Tablero del administrador del hospital (A-10; DEC-10; RF-19; RN-06). Cifras de GET /api/indicadores/tablero,
   filtradas por el hospital del usuario. Cada grafico responde una pregunta y muestra un estado vacio si no hay datos. */
const TableroAdmin = (() => {
  const $ = (id) => document.getElementById(id);
  const corta = (iso) => { const p = String(iso).split('-'); return p[2] + '/' + p[1]; };

  async function dibujar() {
    const r = await HCE.api('/api/indicadores/tablero');
    if (!r.ok) return; // sin tablero no se muestra nada (nunca cifras inventadas)
    const t = r.cuerpo;
    Graficos.barras($('graficoAtenciones'), t.atencionesPorDia.map(d => ({ etiqueta: corta(d.fecha), valor: d.total })),
      'Atenciones por día, últimos 14 días', 'Aún no hay atenciones en los últimos 14 días.');
    Graficos.barras($('graficoPacientes'), t.pacientesNuevosPorSemana.map(d => ({ etiqueta: corta(d.fecha), valor: d.total })),
      'Pacientes nuevos por semana, últimas 8 semanas', 'Aún no hay pacientes nuevos en las últimas 8 semanas.');
    Graficos.barrasHorizontales($('graficoDiagnosticos'),
      t.diagnosticosFrecuentes.map(d => ({ etiqueta: d.codigoCie10 + ' · ' + d.descripcion, valor: d.total })),
      'Diagnósticos CIE-10 más frecuentes', 'Aún no hay diagnósticos registrados.');
    Graficos.dona($('graficoEstado'), [
      { etiqueta: 'Abiertas', valor: t.atencionesAbiertas, color: 'var(--hce-alerta)' },
      { etiqueta: 'Cerradas', valor: t.atencionesCerradas, color: 'var(--hce-primario)' }
    ], 'Atenciones abiertas frente a cerradas', 'Aún no hay atenciones.');
    const cuerpo = $('ultimosAccesos');
    cuerpo.replaceChildren();
    t.ultimosAccesos.forEach(a => {
      const tr = document.createElement('tr');
      [HCE.fechaHora(a.fechaHora), a.usuario, a.accion, a.nroHc || ''].forEach(x => tr.append(HCE.el('td', null, x)));
      cuerpo.append(tr);
    });
    $('sinAccesos').hidden = t.ultimosAccesos.length > 0;
    $('tablero').hidden = false;
  }

  return { dibujar };
})();
