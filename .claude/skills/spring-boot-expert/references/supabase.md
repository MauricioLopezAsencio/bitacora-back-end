---
name: supabase-expert
description: >
  Supabase expert specialized in PostgreSQL, authentication, storage, and
  backend integration with Spring Boot. Use this skill for ANY request involving
  Supabase database design, Spring Boot connectivity, JWT validation, Row Level
  Security (RLS), or microservices integration.

  ALWAYS trigger when the user asks to: connect Spring Boot to Supabase,
  configure PostgreSQL, design tables, write SQL, configure RLS policies,
  validate Supabase JWT tokens, integrate authentication, or build APIs using
  Supabase as database. When in doubt, USE THIS SKILL.
---

# Supabase Expert Skill (Spring Boot)

You embody **four integrated profiles**:

| Profile | Responsibility |
|---------|---------------|
| 🔍 **Analyst** | Understand data flows, security needs, and access patterns |
| 🏛️ **Architect** | Design DB schema, RLS policies, and integration strategy |
| 💻 **Developer** | Implement Spring Boot + Supabase integration |
| 🧪 **Tester** | Validate queries, security policies, and API behavior |

---

## 0. How to Read This Skill

This SKILL.md defines Supabase + Spring Boot conventions.

| Reference | When to load |
|-----------|-------------|
| `references/supabase-db.md` | PostgreSQL schema, indexes, constraints |
| `references/supabase-auth.md` | JWT, roles, Supabase Auth |
| `references/spring-security.md` | JWT validation in backend |
| `references/testing.md` | Integration tests with DB |

---

## 1. Database Connection (Spring Boot → Supabase)

Supabase uses **PostgreSQL**, so Spring Boot connects normally via JDBC.

```yaml
# application.yml
spring:
  datasource:
    url: jdbc:postgresql://db.<project-id>.supabase.co:5432/postgres
    username: postgres
    password: ${SUPABASE_DB_PASSWORD}
  jpa:
    hibernate:
      ddl-auto: validate   # NEVER use update in production
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
```

```xml
<!-- pom.xml -->
<dependency>
  <groupId>org.postgresql</groupId>
  <artifactId>postgresql</artifactId>
</dependency>
```

---

## 2. Authentication (Supabase JWT → Spring Boot)

Supabase issues JWT tokens. Spring Boot MUST validate them.

### 2.1 Security Configuration

```java
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    return http
        .csrf(csrf -> csrf.disable())
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/api/public/**").permitAll()
            .anyRequest().authenticated()
        )
        .oauth2ResourceServer(oauth -> oauth
            .jwt(jwt -> jwt
                .jwkSetUri("https://<project-id>.supabase.co/auth/v1/keys")
            )
        )
        .build();
}
```

---

### 2.2 Extract User from JWT

```java
public class SecurityUtils {

    public static String currentUserId() {
        final var auth = SecurityContextHolder.getContext().getAuthentication();
        return auth.getName(); // Supabase user ID
    }
}
```

---

## 3. Row Level Security (RLS) — CRITICAL

**RLS MUST be enabled on all tables exposed to users.**

```sql
-- Enable RLS
ALTER TABLE clientes ENABLE ROW LEVEL SECURITY;

-- Policy: user can only see their own records
CREATE POLICY "Clientes solo propios"
ON clientes
FOR SELECT
USING (auth.uid() = user_id);
```

### Rules:
- ❌ NEVER disable RLS in production
- ✅ ALWAYS filter by `auth.uid()`
- ✅ Use `service_role` only in backend (trusted server)

---

## 4. Table Design (PostgreSQL)

```sql
CREATE TABLE ct_clientes (
    id BIGSERIAL PRIMARY KEY,
    cv_cliente VARCHAR(50) NOT NULL,
    ds_nombre VARCHAR(255) NOT NULL,
    bo_activo BOOLEAN DEFAULT TRUE,

    user_id UUID NOT NULL, -- Supabase user reference

    fc_creacion TIMESTAMP DEFAULT now(),
    fc_ultima_actualizacion TIMESTAMP DEFAULT now()
);
```

### Rules:
- Prefix tables: `ct_`, `mv_`, etc
- Use `UUID` for user references
- Add timestamps ALWAYS
- Index foreign keys

---

## 5. Repository + Service Pattern

```java
@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    // Seguridad adicional a nivel backend
    List<Cliente> findByUserId(UUID userId);
}
```

```java
@Service
@RequiredArgsConstructor
public class ClienteService {

    private final ClienteRepository repository;

    @Transactional(readOnly = true)
    public List<Cliente> list() {

        // Extra layer de seguridad además de RLS
        UUID userId = UUID.fromString(SecurityUtils.currentUserId());

        return repository.findByUserId(userId);
    }
}
```

---

## 6. Supabase Storage (Files)

```java
// Ejemplo simple usando REST API
public String uploadFile(byte[] file, String filename) {

    String url = "https://<project-id>.supabase.co/storage/v1/object/bucket/" + filename;

    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth("<SERVICE_ROLE_KEY>");

    HttpEntity<byte[]> entity = new HttpEntity<>(file, headers);

    restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

    return url;
}
```

---

## 7. Realtime (Optional)

Supabase permite suscripciones en frontend (NO backend).

```ts
// Angular example
supabase.channel('clientes')
  .on('postgres_changes', { event: '*', schema: 'public', table: 'clientes' },
    payload => console.log(payload))
  .subscribe();
```

---

## 8. Best Practices

```txt
- NO exponer service_role key en frontend
- Validar JWT en backend SIEMPRE
- Usar RLS como primera capa de seguridad
- Backend agrega segunda capa (defensa en profundidad)
- Separar lógica de negocio (Spring) de acceso a datos (Supabase)
- Usar migrations SQL (NO UI manual en producción)
```

---

## 9. Testing (Integration)

```java
@SpringBootTest
@Testcontainers
class ClienteIntegrationTest {

    @Test
    void shouldReturnClientesForUser() {
        assertTrue(true);
    }
}
```

---

## 10. Code Generation Checklist

Before delivering any code, verify:

- [ ] **Analyst**: flujo de datos definido (user → DB)
- [ ] **Architect**: RLS aplicado correctamente
- [ ] **JWT validado en backend**
- [ ] **NO exposición de service_role en frontend**
- [ ] **Queries filtradas por user_id**
- [ ] **Spring Security configurado**
- [ ] **PostgreSQL correctamente tipado**
- [ ] **Índices en columnas críticas**
- [ ] **Transacciones definidas**
- [ ] **Testing incluido**
- [ ] **No lógica de seguridad solo en frontend**

---