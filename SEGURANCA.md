# 🔐 Implementação de Segurança - LojaCarro

## 📋 Resumo das Mudanças

Este documento descreve a implementação completa de um sistema de autenticação e autorização seguro para a aplicação LojaCarro, incluindo proteção contra vulnerabilidades comuns.

---

## ✨ Funcionalidades Implementadas

### 1. **Autenticação com JWT (JSON Web Token)**
- Implementação de tokens JWT com expiração configurável (24h)
- Geração segura de tokens com assinatura HS512
- Validação de tokens em cada requisição
- Suporte a Bearer Token no header `Authorization`

**Classe Principal:** `JwtTokenProvider`

### 2. **Sistema de Papéis (Roles)**
Quatro papéis implementados com permissões diferenciadas:

| Role | Permissões |
|------|-----------|
| **ROLE_ADMIN** | Acesso total ao sistema |
| **ROLE_GERENTE** | Cadastrar, atualizar e deletar carros |
| **ROLE_VENDEDOR** | Apenas cadastrar novos carros |
| **ROLE_CLIENTE** | Apenas visualizar carros |

**Classes Principais:** `Role.java`, `User.java`

### 3. **Validação Contra XSS (Cross-Site Scripting)**
- Validação de padrão regex no modelo do carro
- Rejeição de caracteres especiais perigosos: `< > { } [ ] ; : ' " & |`
- Apenas permite: letras, números, hífens, espaços e caracteres acentuados

**Exemplo de Rejeição:**
```json
{
  "modelo": "<script>alert('XSS')</script>"
}
// Resposta: 400 Bad Request - Caracteres inválidos
```

### 4. **Validação Contra SQL Injection**
- Uso de Prepared Statements automaticamente via JPA
- Validação de padrão regex dos campos
- Rejeição de caracteres SQL perigosos

**Exemplo de Rejeição:**
```json
{
  "modelo": "'; DROP TABLE carros; --"
}
// Resposta: 400 Bad Request - Caracteres inválidos
```

### 5. **Validação de Entrada**
- Campo `modelo` obrigatório, entre 1-255 caracteres
- Campo `ano` obrigatório, entre 1900-2100
- Campo `preço` obrigatório, não negativo
- Proteção contra campos vazios e valores inválidos

### 6. **Controle de Acesso por Roles**

#### Endpoints Públicos (sem autenticação):
```
GET /carro - Listar todos os carros
GET /carro/{id} - Visualizar carro por ID
POST /auth/login - Login de usuário
POST /auth/register - Registro de novo usuário
```

#### Endpoints Protegidos:
```
POST /carro/salvar - Requer: GERENTE ou VENDEDOR
PUT /carro/{id} - Requer: GERENTE (apenas)
DELETE /carro/{id} - Requer: GERENTE (apenas)
```

---

## 🚀 Como Usar

### 1. **Login (Obtenha um Token)**

```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "gerente",
    "password": "gerente123"
  }'
```

**Resposta:**
```json
{
  "token": "eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "id": 1,
  "username": "gerente",
  "email": "gerente@example.com",
  "fullName": "Gerente da Loja"
}
```

### 2. **Usar o Token em Requisições**

```bash
curl -X POST http://localhost:8080/carro/salvar \
  -H "Authorization: Bearer eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9..." \
  -H "Content-Type: application/json" \
  -d '{
    "modelo": "Corolla",
    "ano": 2024,
    "preco": 50000.00
  }'
```

### 3. **Usuários Padrão do Sistema**

| Usuário | Senha | Role |
|---------|-------|------|
| admin | admin123 | ROLE_ADMIN |
| gerente | gerente123 | ROLE_GERENTE |
| vendedor | vendedor123 | ROLE_VENDEDOR |
| cliente | cliente123 | ROLE_CLIENTE |

> **Nota:** Esses usuários são criados automaticamente na primeira execução

---

## 🧪 Testes de Segurança

A aplicação inclui 18 testes de segurança abrangentes:

### Testes de Validação (4 testes)
- ✅ Rejeição de XSS no modelo
- ✅ Rejeição de modelo vazio
- ✅ Rejeição de ano inválido
- ✅ Rejeição de preço negativo

### Testes de Autenticação (3 testes)
- ✅ Login com credenciais válidas
- ✅ Login com credenciais inválidas
- ✅ Rejeição de senha vazia

### Testes de Acesso Não Autorizado (3 testes)
- ✅ Acesso sem token (401 Unauthorized)
- ✅ Token inválido (401 Unauthorized)
- ✅ Cliente não pode cadastrar carro (403 Forbidden)

### Testes de Autorização por Roles (4 testes)
- ✅ Gerente pode cadastrar carro
- ✅ Vendedor pode cadastrar carro
- ✅ Apenas gerente pode atualizar
- ✅ Apenas gerente pode deletar

### Testes de Acesso Público (2 testes)
- ✅ Leitura de todos os carros (sem autenticação)
- ✅ Leitura de carro por ID (sem autenticação)

### Testes de Proteção (2 testes)
- ✅ Rejeição de SQL Injection
- ✅ Rejeição de strings muito grandes

---

## 🔧 Configurações

### application.properties

```properties
# JWT Secret (altere em produção!)
app.jwtSecret=mySecretKeyForJWTTokenGenerationAndValidationOfTheApplicationv1v1v1v1v1v1v1v1v1v1v1v1v1v1v1v1

# Tempo de expiração do token (em ms)
# Padrão: 86400000 (24 horas)
app.jwtExpirationMs=86400000

# Habilitar SQL logging
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
```

---

## 📦 Dependências Adicionadas

```xml
<!-- Spring Security -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>

<!-- JWT (JJWT) -->
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.12.5</version>
</dependency>

<!-- Validação Jakarta -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>

<!-- OWASP Encoder (XSS Prevention) -->
<dependency>
    <groupId>org.owasp.encoder</groupId>
    <artifactId>encoder</artifactId>
    <version>1.2.3</version>
</dependency>
```

---

## 🏗️ Arquitetura

```
├── config/
│   └── DataInitializer.java          # Inicializa roles e usuários
├── controllers/
│   ├── AuthController.java           # Login e registro
│   └── CarroController.java          # CRUD com autorização
├── dto/
│   ├── LoginRequest.java
│   ├── LoginResponse.java
│   └── SignUpRequest.java
├── model/
│   ├── User.java                     # Usuário implementa UserDetails
│   ├── Role.java                     # Papéis do sistema
│   └── Carro.java                    # (Validações adicionadas)
├── repository/
│   ├── UserRepository.java
│   └── RoleRepository.java
├── security/
│   ├── SecurityConfig.java           # Configuração do Spring Security
│   ├── JwtTokenProvider.java         # Geração/validação de JWT
│   ├── JwtAuthenticationFilter.java  # Filtro JWT
│   └── CustomUserDetailsService.java # Carregamento de usuários
├── services/
│   ├── AuthService.java              # Lógica de autenticação
│   └── CarroService.java             # (sem mudanças)
└── exception/
    └── GlobalExceptionHandler.java   # Tratamento global de erros
```

---

## 🔒 Boas Práticas de Segurança Implementadas

### 1. **Senhas com Hash (BCrypt)**
```java
PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
String hashedPassword = passwordEncoder.encode(plainPassword);
```

### 2. **Session Stateless**
- JWT substituiu sessão tradicional
- Cada requisição é independente

### 3. **CORS Configurado**
```java
@CrossOrigin(origins = "*", maxAge = 3600)
```

### 4. **Validação em Múltiplas Camadas**
- Annotations Jakarta Validation
- Regex patterns
- Lógica de negócio

### 5. **Erro Handling Seguro**
- Mensagens genéricas ao usuário
- Detalhes completos nos logs

### 6. **Proteção contra CSRF**
```java
csrf(csrf -> csrf.disable()) // Desabilitado pois usa JWT stateless
```

---

## ⚠️ Possíveis Melhorias Futuras

1. **Rate Limiting** - Limitar tentativas de login
2. **Refresh Token** - Implementar refresh token para aumentar segurança
3. **2FA (Two-Factor Authentication)** - Autenticação em dois fatores
4. **Audit Log** - Registrar todas as ações dos usuários
5. **HTTPS Obrigatório** - Forçar HTTPS em produção
6. **API Keys** - Para acesso programático
7. **OAuth 2.0** - Integração com provedores externos

---

## 🧪 Executar os Testes

```bash
# Rodar todos os testes de segurança
mvn test -Dtest=SecurityIntegrationTest

# Rodar todos os testes
mvn test

# Gerar relatório de cobertura (JaCoCo)
mvn test jacoco:report
```

---

## 📊 Cobertura de Testes

Os testes cobrem os requisitos mencionados:

✅ **1 – Validação de Entrada**
- Teste de XSS com `<script>alert('XSS')</script>`
- Teste de campos vazios
- Teste de valores inválidos

✅ **2 – Acesso Não Autorizado**
- Teste sem autenticação
- Teste com token inválido
- Teste de cliente sem permissão

✅ **3 – Manipulação de Dados**
- Teste de strings muito grandes
- Teste de campos obrigatórios vazios
- Teste de valores inválidos (ano, preço)

✅ **4 – SQL Injection**
- Teste com `'; DROP TABLE carros; --`
- Validação de regex previne execução

✅ **5 – Sistema de Papéis**
- Gerente: cadastrar, atualizar, deletar
- Vendedor: apenas cadastrar
- Cliente: apenas visualizar
- Verificação de permissões implementada

---

## 📝 Próximos Passos

1. Alterar `app.jwtSecret` em produção (usar variável de ambiente)
2. Implementar HTTPS
3. Adicionar logs de auditoria
4. Configurar rate limiting
5. Implementar refresh tokens

---

**Data:** 11 de Junho de 2026  
**Branch:** SEGURANCA  
**Status:** ✅ Pronto para Produção (com as recomendações de segurança aplicadas)
