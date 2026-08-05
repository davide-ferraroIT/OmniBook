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
