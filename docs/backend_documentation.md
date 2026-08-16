# Documentazione Tecnica e Architetturale: Backend OmniBook

## 1. Panoramica Tecnologica e Architettura
Il backend dell'applicazione OmniBook è un sistema *cloud-native* progettato seguendo i gold standard dell'ingegneria del software enterprise, garantendo scalabilità, robustezza, alta disponibilità e una facile manutenibilità.

### Stack Tecnologico
*   **Linguaggio:** Java 21 (LTS) - Sfrutta le ultime feature come Virtual Threads (se abilitati nel runtime), Records per i DTO, e Pattern Matching.
*   **Framework Core:** Spring Boot 3.4.2 - Lo standard di settore per lo sviluppo di applicazioni enterprise, che assicura configurazioni ottimali autogestite e un robusto ecosistema.
*   **Database Relazionale:** PostgreSQL - Selezionato per la sua assoluta affidabilità, rigida conformità ACID e gestione avanzata di dati JSON/complessi.
*   **ORM e Data Access:** Spring Data JPA con Hibernate (PostgreSQL Dialect) - Mappa l'Object-Oriented in entità relazionali e astrae le query più complesse, proteggendo da SQL Injection.
*   **Gestione Media (CDN):** Cloudinary - Utilizzato per lo storage distribuito, la delivery ottimizzata e la manipolazione on-the-fly di immagini (es. loghi dei tenant) e file media.
*   **Documentazione API:** Springdoc OpenAPI (Swagger UI) - Assicura l'esposizione automatica di una specifica OpenAPI 3 (OAS3) interattiva per l'integrazione frontend/mobile.

### Architettura
L'applicazione è strutturata secondo un'architettura **N-Tier (Layered Architecture)** basata sui principi del **Domain-Driven Design (DDD)** in ambito Multi-Tenant:
1.  **API Routing (Controllers):** Riceve il traffico HTTP, valida gli input tramite `spring-boot-starter-validation` e delega la logica al livello inferiore.
2.  **Business Logic (Services):** Il nucleo dell'applicazione, che coordina le regole di business ed esegue transazioni atomiche (`@Transactional`).
3.  **Data Access (Repositories/Models):** Persistenza dei dati raggruppata per domini logici (es. `auth`, `tenant`, `booking`, `service`, `resource`).
4.  **Data Transfer Objects (DTO):** Pattern applicato in ogni punto di ingresso e uscita per isolare l'esposizione delle entità DB ed evitare vulnerabilità come il *Mass Assignment*.

## 2. Autenticazione e Autorizzazione (RBAC)
La sicurezza del sistema è implementata tramite **Spring Security** in combinazione con uno schema **JWT (JSON Web Token)** di tipo *stateless*, che garantisce scalabilità orizzontale non richiedendo l'uso di sessioni in memoria.

### Ruoli di Sistema (Role-Based Access Control)
Il sistema modella tre ruoli distinti (`enum Role`), con permessi gerarchici ed espliciti:

1.  **ADMIN (Amministratore di Piattaforma)**
    *   **Permessi:** Accesso globale ("Super User").
    *   **Capacità esclusive:** È l'unico ruolo autorizzato a creare nuovi `Tenant` (Negozi/Esercenti) nella piattaforma (`POST /api/v1/tenants`). Può agire sui dati di chiunque.
2.  **SHOP (Proprietario Esercente / Tenant)**
    *   **Permessi:** Accesso limitato al proprio ecosistema.
    *   **Capacità:** Quando un utente SHOP invoca endpoint protetti, Spring Security verifica dinamicamente (`@PreAuthorize`) che il `tenant.id` del principal (dal JWT) corrisponda al Tenant che si sta modificando.
    *   **Operazioni consentite:** Creazione, modifica e cancellazione di propri **Services**, **Resources**, **Bookings** e l'aggiornamento dei dettagli del proprio **Tenant**.
3.  **CUSTOMER (Cliente Finale)**
    *   **Permessi:** Base.
    *   **Capacità:** Creazione di nuove prenotazioni (`Booking`) per i vari tenant. Può visionare solo i propri appuntamenti.

### Mappatura delle Policy di Sicurezza (SecurityConfig)

Il sistema adotta un approccio "Secure by Default": **tutti gli endpoint sono bloccati**, eccetto quelli esplicitamente permessi (*Whitelist approach*).

**Endpoint Pubblici (Nessuna Autenticazione Richiesta):**

*   `/api/v1/auth/**` (Login, Registrazione)
*   Lettura (`GET`) di `/api/v1/tenants` e relative ricerche per slug.
*   Lettura (`GET`) dei servizi, risorse e disponibilità di prenotazioni di un tenant (es. `/api/v1/tenants/{tenantId}/services/**`).
*   Swagger Documentation (`/swagger-ui/**`, `/v3/api-docs/**`).
*   Pagina di `/error`.

## 3. Best Practice Adottate

### 3.1 Gestione delle Modifiche al Database
Le mutazioni dello schema SQL non sono demandate ad Hibernate (configurato con `ddl-auto=validate`), ma controllate da **Flyway** (`spring.flyway.enabled=true`).
*   **Impatto:** Le migrazioni sono versionate (es. `V1__init.sql`), garantendo rollout sicuri e rollback deterministici negli ambienti di CI/CD e Produzione.

### 3.2 Protezione Contro Attacchi Web Comuni
*   **CSRF (Cross-Site Request Forgery):** Disabilitato in quanto l'applicazione è stateless e non usa cookie di sessione.
*   **CORS (Cross-Origin Resource Sharing):** Strettamente limitato a origini dichiarate (`application.cors.allowed-origins`), evitando che domini maligni possano comunicare con il backend.
*   **Password Hashing:** Si avvale di un encoder robusto (es. **Bcrypt**) configurato nel blocco di *AuthenticationProvider*.
*   **Gestione Globale Eccezioni:** Tramite Controller Advice, le eccezioni interne non divulgano mai stack trace completi al frontend, emettendo invece errori semantici mascherati per limitare la *surface of attack* intellettuale.

### 3.3 Configurazione 12-Factor App
L'applicativo utilizza `spring-dotenv` per garantire che credenziali e chiavi (DB, JWT, Cloudinary API Key) siano sempre iniettate dall'infrastruttura (variabili d'ambiente) e **mai committate nei repository GIT**, secondo i paradigmi 12-Factor.

### 3.4 Metodologia di Testing
Per garantire che il codice si comporti in test esattamente come in produzione, è integrato **Testcontainers**.
*   **Impatto:** Invece di usare mock DB instabili (come H2), JUnit 5 orchestra container Docker PostgreSQL reali ed effimeri, permettendo la scrittura di test di integrazione del 100% veritieri sulle transazioni e i constraint relazionali reali.
