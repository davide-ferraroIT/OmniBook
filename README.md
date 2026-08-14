# OmniBook 🗓️

[![Backend CI](https://github.com/davide-ferraroIT/OmniBook/actions/workflows/backend-ci.yml/badge.svg)](https://github.com/davide-ferraroIT/OmniBook/actions/workflows/backend-ci.yml)
[![Frontend CI](https://github.com/davide-ferraroIT/OmniBook/actions/workflows/frontend-ci.yml/badge.svg)](https://github.com/davide-ferraroIT/OmniBook/actions/workflows/frontend-ci.yml)

OmniBook è un'applicazione **SaaS Multi-Tenant White-Label** per la gestione di prenotazioni e appuntamenti, progettata per essere utilizzata da diverse tipologie di attività (es. Barbieri, Officine, Studi Medici) tramite un'architettura generica e un database flessibile guidato dai dati (Data-Driven).

## 🚀 Architettura

L'applicazione è suddivisa in due componenti principali racchiusi in un Polyglot Monorepo:

1. **Backend (Java / Spring Boot 3)**
   - API RESTful basate su Java 21.
   - Database PostgreSQL, sfruttando il tipo di dato `JSONB` (tramite `hypersistence-utils`) per permettere a ciascun Tenant di memorizzare campi dinamici sulle proprie prenotazioni (es. la Targa per un gommista, il tipo di taglio per un barbiere) mantenendo un'unica struttura relazionale e scalabile.

2. **Frontend (Angular 18 / Ionic / Capacitor)**
   - PWA e App Nativa per iOS/Android sviluppata in Angular e Ionic.
   - Design reattivo e dinamico: l'interfaccia si auto-configura scaricando le impostazioni del Tenant (Colori, Testi, Form dinamici) dal backend, abilitando un vero approccio **White-Label**.
   - **Tailwind CSS** per facilitare il theming dinamico basato su variabili CSS.

---

## 🛠️ Come avviare l'ambiente di sviluppo

Per far girare il progetto in locale non è necessario installare nulla se non [Docker](https://www.docker.com/) e un terminale.

### 1. Avviare il Database Locale
Nella cartella radice del progetto:
```bash
docker compose up -d
```
*(Verrà avviato un container PostgreSQL sulla porta `5432` con utente `root` e password `rootpassword`).*

### 2. Avviare il Backend API
Apri un terminale nella cartella `backend/`:
```bash
# Esegue Spring Boot
./mvnw spring-boot:run
```
*(Il server risponderà all'indirizzo `http://localhost:8080`).*

> **Nota:** Al primo avvio verrà creata automaticamente un'utenza amministrativa fittizia con credenziali:
> - **Email**: `root@root.it`
> - **Password**: `root`

### 3. Avviare il Frontend App
Apri un nuovo terminale nella cartella `frontend/`:
```bash
# Installa le dipendenze
npm install

# Avvia l'app nel browser
npm start
```
*(L'app web sarà raggiungibile all'indirizzo `http://localhost:4200`).*

---

## 🧪 Testing & Continuous Integration (CI/CD)

Questo progetto adotta le best practice per il testing continuo.
Le **GitHub Actions** configurate in `.github/workflows/` eseguono automaticamente su ogni Push e Pull Request:
- Il Linting e lo Unit Testing Headless sul frontend Angular.
- La compilazione Maven e gli Unit Test sul backend Spring Boot.

Non è consentito il merge sul branch `main` se la pipeline non risulta verde.

---
*Progetto sviluppato da [Davide Ferraro](https://github.com/davide-ferraroIT).*
