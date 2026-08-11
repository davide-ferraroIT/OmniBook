# Diario di Sviluppo (Walkthroughs)

Questo documento traccia l'evoluzione e l'implementazione del progetto OmniBook, registrando tutti gli step principali affrontati durante lo sviluppo.

---

## 1. Setup Iniziale e Configurazione Monorepo

**Data:** Agosto 2026

Abbiamo posto le basi per l'architettura tecnica del progetto configurando un Polyglot Monorepo "Senior Level" che unifica backend e frontend sotto un unico ecosistema.

**Cosa è stato fatto:**
- Inizializzazione repository Git e collegamento al repository remoto `davide-ferraroIT/OmniBook`.
- Strutturazione cartelle in due macro-moduli: `/backend` e `/frontend`.
- Creazione del `docker-compose.yml` per orchestrazione database (PostgreSQL 16).
- **Inizializzazione Backend:** Setup progetto Java (Spring Boot 3 + Maven + Java 21) con dipendenze base: JPA, PostgreSQL, Validation e Web.
- **Inizializzazione Frontend:** Setup Angular + Ionic (Capacitor) con configurazione fully-custom di Tailwind CSS.
- **Continuous Integration (CI):** Implementazione di GitHub Actions con check automatici su PR: 
  - `backend-ci.yml`: test unitari Maven.
  - `frontend-ci.yml`: test headless Chrome su Angular e linting.
- **Governance:** Stesura del `README.md` tecnico-architetturale e aggiunta dei template per le Pull Request.

---

## 2. Implementazione Modello Dati (SaaS Multi-Tenant)

**Data:** Agosto 2026

Abbiamo creato lo strato dati principale per la gestione del Multi-Tenancy White Label. Si è optato per un'architettura **"Resource-Based Capacity"** che permette di unificare diverse tipologie di aziende (barbieri, gommisti, palestre) astraendo il calcolo delle disponibilità.

**Cosa è stato fatto:**
- **`Tenant` (L'Azienda Cliente):** Entità con UUID e JSONB (`TenantConfig`) sfruttando `@JdbcTypeCode(SqlTypes.JSON)` nativo di Hibernate 6. 
- **`Resource` (La Postazione):** Entità con una specifica "capacità". Gestisce l'erogazione del servizio e definisce quanti slot sono prenotabili in parallelo.
- **`Service` (La Prestazione):** Entità collegata a una o più `Resource` tramite relazione Molti-A-Molti.
- **Feature Flags:** Aggiunto Enum `FeatureModule` nel JSONB per gestire funzionalità on-demand (es. e-commerce, pagamenti) lato frontend e API.
- **Test In-Memory Avanzato:** Configurato il nuovo standard Spring Boot 3.4.2 con **Testcontainers** (`@ServiceConnection`). Il test JPA avvia automaticamente un Postgres temporaneo via Docker, verifica il corretto salvataggio e recupero del campo JSONB per l'entità `Tenant`, e si spegne, garantendo affidabilità enterprise.

---

## 3. Implementazione Livello API REST per Tenant

**Data:** Agosto 2026

L'architettura per esporre i dati all'esterno è stata costruita rispettando i paradigmi di sviluppo Senior per API REST scalabili e sicure.

**Cosa è stato fatto:**
- **Data Transfer Objects (DTO):** Creati `TenantCreateRequest` e `TenantResponse` come Record Java 21 per incapsulare i dati, nascondere le entità JPA al controller e fornire immutabilità.
- **Service Layer (`TenantService`):** Implementata la logica di business disaccoppiata dal Controller. Gestisce la validazione dei duplicati (controllo univocità slug) e la trasformazione da Entity a DTO.
- **Controller Layer (`TenantController`):** Implementati gli endpoint RESTful su `/api/v1/tenants` seguendo le best practices (nomi al plurale, zero verbi):
  - `POST /` per la creazione (con Status HTTP 201).
  - `GET /{id}` per il recupero tramite identificativo univoco.
  - `GET /slug/{slug}` per il fetch della configurazione White-Label del client.
  - `GET /` con paginazione nativa (`Pageable`).
- **Global Exception Handling (RFC 7807):** Implementato `@RestControllerAdvice` (`GlobalExceptionHandler`) per catturare eccezioni custom (`ResourceNotFoundException`, `SlugAlreadyExistsException`) ed errori di validazione Jakarta (`@Valid`). Le risposte di errore sono standardizzate nel formato `ProblemDetail`.
- **Automated Testing:** Creato `TenantControllerTest` sfruttando `@WebMvcTest` e Mockito (`@MockBean`) per validare il livello web isolato, inclusi i casi di errore 400, 404 e 409.

---
## 4. Implementazione Livello API REST per Risorse e Servizi (Prevenzione IDOR)

**Data:** Agosto 2026

Abbiamo esteso il backend implementando gli endpoint per `Resource` e `Service`, adottando una strategia di protezione rigorosa contro vulnerabilità di tipo IDOR (Insecure Direct Object Reference) tipiche dei sistemi SaaS Multi-Tenant.

**Cosa è stato fatto:**
- **Risoluzione Compilatore:** Fissato il bug del compilatore Lombok con JDK 26, aggiornando in `pom.xml` la `<lombok.version>` alla 1.18.38.
- **Sicurezza e Isolamento Dati:** 
  - Estesi i repository (`ResourceRepository` e `ServiceRepository`) aggiungendo query protette per isolamento al Tenant: `findByIdAndTenantId`.
  - Strutturati i percorsi REST in modo annidato per garantire il contesto: `/api/v1/tenants/{tenantId}/resources` e `/api/v1/tenants/{tenantId}/services`.
- **Implementazione DTO:** Creati i Request/Response record (es. `ResourceCreateRequest`, `ServiceCreateRequest`) incapsulando tutte le logiche di `@Valid`.
- **Logica Relazionale Sicura:** Nel Service Layer (`ProvidedServiceManager`), la creazione di un servizio che referenzia delle risorse (`allowedResourceIds`) verifica strictmente che *ogni* singola risorsa esista e appartenga effettivamente allo stesso Tenant, garantendo l'integrità referenziale Multi-Tenant.
- **Unit/Integration Test:** Aggiunto un test su base Testcontainers (`ProvidedServiceManagerTest`) per verificare empiricamente il corretto rigetto di richieste illegali, documentando il comportamento dell'architettura in caso di attacco IDOR inter-tenant.

---

## 5. Booking Engine: Prenotazioni e Disponibilità con Regole Avanzate

**Data:** Agosto 2026

Implementazione del core system per la gestione delle prenotazioni (`Booking`), con un avanzato motore di calcolo disponibilità (Availability Engine) integrato nel backend Spring Boot.

**Cosa è stato fatto:**
- **Estensione Modello Dati (TenantConfig):** 
  - Aggiunto l'oggetto `DaySchedule` al JSONB di configurazione per permettere a ciascun tenant di gestire dinamicamente gli orari di apertura e chiusura.
  - Aggiunto il parametro `allowAutoAssignment` per dare il controllo al Tenant se permettere l'assegnazione randomica della risorsa oppure forzare l'utente a scegliere uno specifico membro dello staff (Resource).
- **Nuova Entità `Booking`:**
  - Relazionata con `Tenant`, `Service` e `Resource`.
  - Include lo stato `BookingStatus` e i dati basilari del cliente in attesa dell'integrazione di un modulo IAM/Auth.
- **Availability Engine (`BookingService`):**
  - Implementata la logica `getAvailableSlots()` che estrae dinamicamente gli intervalli di orario in base alla durata del Servizio (es. slot ogni 30 min) controllando che ricadano negli orari di apertura del Tenant del giorno specifico.
  - Sfruttata la funzione di aggregazione SQL `countOverlappingBookings()` sul `BookingRepository` per confrontare le sovrapposizioni orarie con la *capacità reale* (`capacity`) della `Resource`. Se la Poltrona ha capacità 1, rifiuta il secondo utente; se la Sala Yoga ha capacità 20, permette prenotazioni parallele fino a riempimento.
- **API REST & Error Handling:**
  - Esposti endpoint isolati su `/api/v1/tenants/{tenantId}/bookings` (`GET`, `POST`, `PATCH`).
  - L'endpoint `POST` di creazione valida in tempo reale la logica anti-overbooking: se un hacker bypassa l'UI per prenotare uno slot già occupato, il Service Layer lancia una `IllegalStateException`, prevenendo inconsistenza DB.

---

