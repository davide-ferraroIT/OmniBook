# OmniBook 🗓️

[![Backend CI](https://github.com/davide-ferraroIT/OmniBook/actions/workflows/backend-ci.yml/badge.svg)](https://github.com/davide-ferraroIT/OmniBook/actions/workflows/backend-ci.yml)
[![Frontend CI](https://github.com/davide-ferraroIT/OmniBook/actions/workflows/frontend-ci.yml/badge.svg)](https://github.com/davide-ferraroIT/OmniBook/actions/workflows/frontend-ci.yml)
[![License: CC BY-NC-SA 4.0](https://img.shields.io/badge/License-CC%20BY--NC--SA%204.0-lightgrey.svg)](https://creativecommons.org/licenses/by-nc-sa/4.0/)

OmniBook è un'applicazione **SaaS Multi-Tenant White-Label** per la gestione di prenotazioni e appuntamenti. Progettata per servire diverse tipologie di attività (es. Barbieri, Officine, Studi Medici), utilizza un'architettura modulare e un database *data-driven* capace di adattarsi dinamicamente alle esigenze specifiche di ogni esercente.

## 🚀 Architettura e Stack Tecnologico

L'applicazione adotta un'architettura **N-Tier** cloud-native, strutturata come Polyglot Monorepo e guidata dai principi del Domain-Driven Design (DDD).

### 🖥️ Frontend (Angular 20 / Ionic 8 / Capacitor)
- **Omni-Channel:** Sviluppata in Angular e Ionic, pacchettizzata con Capacitor per offrire sia una PWA che applicazioni native iOS e Android.
- **Design White-Label:** L'interfaccia, costruita con **Tailwind CSS**, si auto-configura cromaticamente e testualmente in base alle impostazioni del Tenant.
- **Performance & UX:** Gestione ottimizzata tramite Lazy Loading, PreloadAllModules e integrazione con FullCalendar v6 per dashboard interattive.
- **Sicurezza:** Autenticazione basata su JWT Stateless, gestita centralmente tramite AuthInterceptor e Route Guards (RBAC) per la validazione dei ruoli.

### ⚙️ Backend (Java 21 / Spring Boot 3.4)
- **API RESTful:** Logica di business robusta con forte segregazione delle responsabilità (Controller, Service, Repository, DTO).
- **Database (PostgreSQL via Neon):** Sfrutta campi `JSONB` per la flessibilità dei dati *per-tenant* pur mantenendo la rigidità ACID relazionale. Le migrazioni sono gestite tramite Flyway.
- **Sicurezza & RBAC:** Policy di sicurezza *Secure by Default* con Spring Security. Accesso granulare basato su tre ruoli: `ADMIN`, `SHOP`, e `CUSTOMER`.
- **Media Management:** Integrazione con l'API di Cloudinary per l'ottimizzazione automatica e la delivery tramite CDN degli asset statici.

## ☁️ Deployment e Infrastruttura

L'infrastruttura segue un approccio **Serverless** e **Fully-Managed**, orchestrata per minimizzare le operations (Zero-Ops) massimizzando le performance:
- **Backend:** Containerizzato tramite Docker e ospitato su [Render (PaaS)](https://render.com/), garantendo Continuous Deployment.
- **Database:** PostgreSQL Serverless tramite [Neon](https://neon.tech/), ottimizzato per i costi tramite auto *scale-to-zero* e funzionalità di data branching.
- **Frontend:** Distribuito tramite [Vercel](https://vercel.com/) su una rete Edge globale, con gestione automatica di certificati SSL e domini personalizzati.
- **Storage:** [Cloudinary](https://cloudinary.com/) come CDN per trasformazioni multimediali *on-the-fly*.

## 🧪 Testing e Continuous Integration

La stabilità è garantita da un rigido controllo qualità automatizzato tramite **GitHub Actions**:
- **Backend CI:** Punta al **100% di Code Coverage** (JaCoCo). Adotta i *Gold Standard* di Spring Boot utilizzando **Testcontainers** per eseguire test d'integrazione su database PostgreSQL reali ed effimeri.
- **Frontend CI:** Esecuzione di Linting e Unit Test *Headless* per prevenire regressioni sul client a ogni Pull Request.

## 🛠️ Avvio Ambiente di Sviluppo

Per avviare l'intero ecosistema in locale (Database, Backend e Frontend), l'unico requisito è avere [Docker](https://www.docker.com/) installato.

È disponibile un comodo script che orchestra automaticamente l'avvio di tutti i servizi in parallelo. Dalla radice del progetto, esegui:

```bash
./script/start-dev.sh
```

Lo script si occuperà di:
1. Avviare il database **PostgreSQL** tramite Docker (porta `5432`).
2. Avviare le API **Spring Boot** (porta `8080`).
3. Avviare l'app web **Angular** (porta `8100`).

Al primo avvio il database verrà popolato automaticamente con alcuni dati e utenti di test:
- **Amministratori (ADMIN):** `root@root.it` (pw: `root`) | `davide@example.it` (pw: `davide`)
- **Proprietari Negozi (SHOP):** `barbiere@example.it` | `gommista@example.it` (pw: `password`)
- **Cliente (CUSTOMER):** `cliente@example.it` (pw: `password`)

Per arrestare tutti i servizi in modo pulito (incluso lo spegnimento di Docker), ti basterà premere `CTRL+C` nel terminale dove hai avviato lo script.

## 📄 Licenza

Questo progetto è rilasciato sotto la licenza **Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International (CC BY-NC-SA 4.0)**.

- **Non è consentito l'uso commerciale** del codice o dell'applicazione.
- **È obbligatorio citare l'autore originale** (Davide Ferraro).
- **Le modifiche devono essere condivise** sotto la stessa licenza.

Per maggiori dettagli, consulta il file [LICENSE](LICENSE) o il [testo ufficiale della licenza](https://creativecommons.org/licenses/by-nc-sa/4.0/deed.it).

---
*Progetto sviluppato da [Davide Ferraro](https://github.com/davide-ferraroIT).*
