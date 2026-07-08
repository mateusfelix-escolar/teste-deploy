const API_BASE = (() => {
  const protocol = window.location.protocol || 'http:';
  const hostname = window.location.hostname || 'localhost';
  return `${protocol}//${hostname}:8080`;
})();
const DEFAULT_IMAGE = 'https://thumbs.dreamstime.com/b/%C3%ADcone-de-imagem-sem-foto-ou-em-branco-carregamento-imagens-aus%C3%AAncia-marca-n%C3%A3o-dispon%C3%ADvel-sinal-breve-silhueta-natureza-simples-215973362.jpg';

function getToken() {
  return localStorage.getItem('jwtToken');
}

function setToken(token) {
  localStorage.setItem('jwtToken', token);
}

function clearToken() {
  localStorage.removeItem('jwtToken');
}

function showMessage(text, type = 'success') {
  const status = document.getElementById('status');
  if (!status) return;
  status.textContent = text;
  status.className = `status ${type}`;
}

function getAuthHeaders() {
  const token = getToken();
  return token ? { Authorization: `Bearer ${token}` } : {};
}

async function apiRequest(path, options = {}) {
  const headers = { ...(options.headers || {}) };
  if (options.body) {
    headers['Content-Type'] = 'application/json';
  }
  const authHeaders = getAuthHeaders();
  Object.assign(headers, authHeaders);

  const response = await fetch(`${API_BASE}${path}`, { ...options, headers });
  const contentType = response.headers.get('content-type') || '';
  const text = await response.text();
  let body = null;
  if (contentType.includes('application/json') && text) {
    body = JSON.parse(text);
  } else if (text) {
    body = text;
  }

  if (!response.ok) {
    throw new Error(body?.message || body?.error || 'Erro ao chamar a API');
  }
  return body;
}

async function loginUser(event) {
  event?.preventDefault();
  const username = document.getElementById('username').value.trim();
  const password = document.getElementById('password').value;

  try {
    const result = await apiRequest('/auth/login', {
      method: 'POST',
      body: JSON.stringify({ username, password }),
      headers: {}
    });
    setToken(result.token);
    showMessage('Login realizado com sucesso!', 'success');
    window.location.href = '/pages/carros.html';
  } catch (error) {
    showMessage(error.message, 'error');
  }
}

async function registerUser(event) {
  event?.preventDefault();
  const username = document.getElementById('username').value.trim();
  const email = document.getElementById('email').value.trim();
  const password = document.getElementById('password').value;
  const fullName = document.getElementById('fullName').value.trim();

  try {
    await apiRequest('/auth/register', {
      method: 'POST',
      body: JSON.stringify({ username, email, password, fullName })
    });
    showMessage('Usuário cadastrado com sucesso! Faça login.', 'success');
    window.location.href = '/pages/login.html';
  } catch (error) {
    showMessage(error.message, 'error');
  }
}

async function loadCarros() {
  try {
    const carros = await apiRequest('/carro', { method: 'GET' });
    const container = document.getElementById('carrosList');
    if (!container) return;

    if (!Array.isArray(carros) || carros.length === 0) {
      container.innerHTML = '<p>Nenhum carro encontrado.</p>';
      return;
    }

    container.innerHTML = carros.map((carro) => `
      <article class="car-card">
        <img src="${DEFAULT_IMAGE}" alt="${carro.modelo || 'Carro'}" />
        <div class="car-body">
          <h3>${carro.modelo || 'Sem modelo'}</h3>
          <p><strong>Ano:</strong> ${carro.ano || '-'}</p>
          <p><strong>Preço:</strong> R$ ${Number(carro.preco || 0).toFixed(2)}</p>
          <div class="actions">
            <button class="btn-edit" onclick="editCarro(${carro.id})">Editar</button>
            <button class="btn-delete" onclick="deleteCarro(${carro.id})">Excluir</button>
          </div>
        </div>
      </article>
    `).join('');
  } catch (error) {
    showMessage(error.message, 'error');
  }
}

function editCarro(id) {
  window.location.href = `/pages/carro-form.html?id=${id}`;
}

async function deleteCarro(id) {
  if (!confirm('Deseja realmente excluir este carro?')) return;
  try {
    await apiRequest(`/carro/${id}`, { method: 'DELETE' });
    showMessage('Carro excluído com sucesso!', 'success');
    loadCarros();
  } catch (error) {
    showMessage(error.message, 'error');
  }
}

async function saveCarro(event) {
  event?.preventDefault();
  const id = document.getElementById('carroId').value;
  const payload = {
    modelo: document.getElementById('modelo').value.trim(),
    ano: Number(document.getElementById('ano').value),
    preco: Number(document.getElementById('preco').value)
  };

  try {
    if (id) {
      await apiRequest(`/carro/${id}`, { method: 'PUT', body: JSON.stringify(payload) });
      showMessage('Carro atualizado com sucesso!', 'success');
    } else {
      await apiRequest('/carro/salvar', { method: 'POST', body: JSON.stringify(payload) });
      showMessage('Carro cadastrado com sucesso!', 'success');
    }
    window.location.href = '/pages/carros.html';
  } catch (error) {
    showMessage(error.message, 'error');
  }
}

async function loadCarroForEdit() {
  const params = new URLSearchParams(window.location.search);
  const id = params.get('id');
  if (!id) return;

  try {
    const carro = await apiRequest(`/carro/${id}`, { method: 'GET' });
    document.getElementById('carroId').value = carro.id;
    document.getElementById('modelo').value = carro.modelo || '';
    document.getElementById('ano').value = carro.ano || '';
    document.getElementById('preco').value = carro.preco || '';
  } catch (error) {
    showMessage(error.message, 'error');
  }
}

function logout() {
  clearToken();
  window.location.href = '/pages/login.html';
}

function checkAuth() {
  const token = getToken();
  const topbar = document.getElementById('userBadge');
  if (topbar) {
    topbar.textContent = token ? 'Sessão ativa' : 'Sem sessão';
  }
  if (!token && window.location.pathname !== '/pages/login.html' && window.location.pathname !== '/pages/register.html') {
    window.location.href = '/pages/login.html';
  }
}

window.addEventListener('DOMContentLoaded', () => {
  checkAuth();
  if (window.location.pathname.includes('/carros.html')) {
    loadCarros();
  }
  if (window.location.pathname.includes('/carro-form.html')) {
    loadCarroForEdit();
  }
});
