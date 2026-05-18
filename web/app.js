const API = "";

const form = document.getElementById("form-registro");
const mensaje = document.getElementById("mensaje");
const turnoActual = document.getElementById("turnoActual");
const tipoActual = document.getElementById("tipoActual");
const nombreActual = document.getElementById("nombreActual");
const atendidos = document.getElementById("totalAtendidos");
const pendientes = document.getElementById("totalPendientes");
const proximo = document.getElementById("proximoTurno");
const tablaEspera = document.getElementById("tablaEspera");
const listaHistorial = document.getElementById("listaHistorial");
const btnAtender = document.getElementById("btnAtender");

function setMensaje(texto, ok) {
  mensaje.textContent = texto;
  mensaje.className = ok ? "notice ok" : "notice error";
}

async function request(url, method, body) {
  try {
    const res = await fetch(API + url, {
      method: method,
      headers: { "Content-Type": "application/x-www-form-urlencoded" },
      body: body || undefined,
    });

    const text = await res.text();

    try {
      return JSON.parse(text);
    } catch (e) {
      console.error("Respuesta invalida:", text);
      return { ok: false, mensaje: "Respuesta invalida del servidor." };
    }

  } catch (error) {
    console.error(error);
    return { ok: false, mensaje: "Error de conexion." };
  }
}

function toForm(data) {
  return new URLSearchParams(data).toString();
}

async function cargarEstado() {
  const data = await request("/api/estado", "GET");
  if (!data.ok) {
    return;
  }
  atendidos.textContent = data.atendidos;
  pendientes.textContent = data.pendientes;
  proximo.textContent = data.proximoTurno === null ? "---" : data.proximoTurno;
}

async function cargarCola() {
  const data = await request("/api/cola", "GET");
  tablaEspera.innerHTML = "";
  if (!data.ok || data.clientes.length === 0) {
    tablaEspera.innerHTML = "<tr><td>---</td><td>---</td><td>---</td></tr>";
    return;
  }
  data.clientes.forEach((c) => {
    const tr = document.createElement("tr");
    tr.innerHTML = `<td>${c.turno}</td><td>${c.nombre}</td><td>${c.tipo}</td>`;
    tablaEspera.appendChild(tr);
  });
}

async function cargarHistorial() {
  const data = await request("/api/historial", "GET");
  listaHistorial.innerHTML = "";
  if (!data.ok || data.clientes.length === 0) {
    listaHistorial.innerHTML = "<li>Sin registros</li>";
    return;
  }
  data.clientes.forEach((c) => {
    const li = document.createElement("li");
    li.textContent = `Turno ${c.turno} - ${c.nombre} (${c.tipo})`;
    listaHistorial.appendChild(li);
  });
}

async function cargarActual() {
  const data = await request("/api/actual", "GET");
  if (!data.ok || !data.cliente) {
    turnoActual.textContent = "---";
    tipoActual.textContent = "---";
    nombreActual.textContent = "---";
    return;
  }
  turnoActual.textContent = data.cliente.turno;
  tipoActual.textContent = data.cliente.tipo;
  nombreActual.textContent = data.cliente.nombre;
}

form.addEventListener("submit", async (event) => {
  event.preventDefault();
  const data = {
    nombre: document.getElementById("nombre").value.trim(),
    tipoDocumento: document.getElementById("tipoDocumento").value,
    documento: document.getElementById("documento").value.trim(),
    edad: document.getElementById("edad").value,
  };

  const res = await request("/api/registrar", "POST", toForm(data));
  if (res.ok) {
    setMensaje("Turno registrado: " + res.cliente.turno, true);
    form.reset();
    await cargarEstado();
    await cargarCola();
  } else {
    setMensaje(res.mensaje || "No se pudo registrar.", false);
  }
});

btnAtender.addEventListener("click", async () => {
  const res = await request("/api/atender", "POST", "");
  if (res.ok && res.cliente) {
    turnoActual.textContent = res.cliente.turno;
    tipoActual.textContent = res.cliente.tipo;
    nombreActual.textContent = res.cliente.nombre;
    setMensaje("Cliente atendido.", true);
  } else if (res.ok) {
    turnoActual.textContent = "---";
    tipoActual.textContent = "---";
    nombreActual.textContent = "---";
    setMensaje("No hay clientes en espera.", false);
  } else {
    setMensaje(res.mensaje || "No se pudo atender.", false);
  }
  await cargarEstado();
  await cargarCola();
  await cargarHistorial();
});

cargarEstado();
cargarCola();
cargarHistorial();
cargarActual();
