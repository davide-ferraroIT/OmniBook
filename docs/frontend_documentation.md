# Documentazione Architetturale Frontend - OmniBook

Questa documentazione approfondita descrive l'architettura, le scelte tecniche, i pattern di sicurezza, le policy di autorizzazione e le best practice adottate per lo sviluppo del frontend del progetto OmniBook.

## 1. Introduzione e Stack Tecnologico

Il frontend di OmniBook è stato progettato come un'applicazione moderna, performante e multi-piattaforma, pensata per funzionare sia come Web App che come applicazione mobile nativa (iOS/Android).

Le scelte tecnologiche principali includono:
- **Angular (v20)**: Framework principale utilizzato per lo sviluppo della Single Page Application (SPA). Fornisce una struttura robusta, tipizzazione forte con TypeScript e un eccellente sistema di dependency injection.
- **Ionic Framework (v8)**: Utilizzato come libreria di componenti UI per garantire un'esperienza utente "native-like" e responsiva su tutti i dispositivi.
- **Capacitor (v8)**: Runtime nativo adottato per pacchettizzare l'applicazione web in app native per iOS e Android, permettendo l'accesso alle API native del dispositivo.
- **Tailwind CSS**: Framework CSS "utility-first" adottato per definire lo styling in maniera rapida, modulare e consistente, affiancato da PostCSS e Autoprefixer.
- **FullCalendar (v6)**: Integrato per le visualizzazioni interattive e complesse legate alle prenotazioni.

---

## 2. Architettura e Struttura del Progetto

Il progetto segue le linee guida architetturali e i "gold standard" di Angular:

- **Moduli Funzionali (Feature Modules)**: L'applicazione è divisa in moduli logici indipendenti (`admin`, `auth`, `booking`, `dashboard`, `profile`). Questo favorisce la manutenibilità e la scalabilità.
- **Core Module (`src/app/core`)**: Centralizza i servizi singleton (es. `AuthService`, `ApiService`), gli Interceptor HTTP (`AuthInterceptor`) e le Route Guards (`AuthGuard`, `NoAuthGuard`, `RoleGuard`).
- **State Management e Reattività**: Il flusso dei dati e la gestione dello stato globale (come lo stato di login) sono gestiti in modo reattivo utilizzando **RxJS** (tramite `BehaviorSubject` e `Observable`).

---

## 3. Gestione delle Rotte (Routing) e Prestazioni

Le rotte dell'applicazione (`app-routing.module.ts`) sono state progettate per garantire sicurezza e performance. Per minimizzare il Time to Interactive (TTI), è stato implementato il **Lazy Loading**: il bundle di una specifica sezione viene scaricato dal browser solo quando l'utente prova ad accedervi. A questo si aggiunge la strategia `PreloadAllModules`, che carica i moduli in background mentre l'utente naviga nell'app.

### Mappatura delle Rotte e Permessi
Ogni rotta è protetta da specifiche **Guards** che determinano l'accesso in base allo stato di autenticazione e al ruolo:

| Rotta | Modulo Lazy Loaded | Accesso (Guards) | Ruoli Ammessi (`RoleGuard`) | Descrizione |
| :--- | :--- | :--- | :--- | :--- |
| `/login` | `LoginPageModule` | `NoAuthGuard` | *Nessuno / Ospiti* | Pagina di accesso. Preclusa agli utenti già loggati (vengono reindirizzati alla loro home). |
| `/register` | `RegisterPageModule` | `NoAuthGuard` | *Nessuno / Ospiti* | Pagina di registrazione. |
| `/dashboard` | `DashboardPageModule` | `AuthGuard`, `RoleGuard` | `ADMIN` | Pannello di controllo globale, accessibile esclusivamente dagli amministratori del sistema. |
| `/booking/:slug` | `BookingPageModule` | `AuthGuard`, `RoleGuard` | `ADMIN`, `CUSTOMER` | Pagina dedicata alle prenotazioni di uno specifico tenant (shop). |
| `/shop/:slug` | `AdminDashboardModule` | `AuthGuard`, `RoleGuard` | `ADMIN`, `SHOP` | Pannello di gestione del negozio/tenant. Accessibile al proprietario del negozio (`SHOP`) e all'amministratore di sistema (`ADMIN`). |
| `/profile` | `ProfilePageModule` | `AuthGuard` | *Qualsiasi utente loggato* | Profilo dell'utente corrente. Solo requisito: essere autenticati. |
| `**` e `/` | - | - | - | Fallback. Reindirizzano automaticamente a `/login`. |

---

## 4. Sicurezza (Security Decisions) e Autenticazione (JWT)

La sicurezza del frontend e l'integrità dei dati si basano sullo standard **JSON Web Token (JWT)**. A differenza dei sistemi a sessione basati su cookie (Stateful), l'approccio JWT è **Stateless**: il server non mantiene memoria della sessione, ma demanda al token stesso il trasporto delle informazioni dell'utente.

### 4.1 Il Token JWT come Veicolo di Informazioni
Quando un utente effettua il login o si registra (tramite `AuthService`), il backend restituisce un token JWT che viene memorizzato nel `localStorage`.
Questo token non è solo una chiave di accesso, ma **trasporta attivamente informazioni sull'utente (Claims)** nel suo payload (codificato in Base64 URL). 

Il frontend decodifica in modo sicuro questo payload (senza bisogno di contattare il backend) per estrarre:
- `sub`: L'email o l'ID identificativo primario dell'utente.
- `role`: Il ruolo dell'utente (`ADMIN`, `SHOP`, `CUSTOMER`), vitale per il calcolo delle autorizzazioni lato UI.
- `tenantId` e `tenantSlug`: Identificativi del negozio associato, necessari per le rotte e il fetch di dati specifici.

*Nota di Sicurezza*: Poiché il JWT nel `localStorage` è esposto ad attacchi XSS (Cross-Site Scripting), Angular offre una solida protezione intrinseca eseguendo un escape automatico di tutti i dati renderizzati nel DOM, abbassando notevolmente questo rischio. Inoltre, il token è firmato digitalmente dal backend, per cui se il frontend lo manomettesse, il backend lo respingerebbe.

### 4.2 AuthInterceptor
Il file `auth.interceptor.ts` gestisce il traffico di rete in uscita e in entrata:
- **Iniezione Automatica**: Per ogni chiamata API (eccetto verso `/auth/login` o `/register`), l'interceptor clona la richiesta e aggiunge l'header `Authorization: Bearer <token>`.
- **Gestione Errori e Logout Forzato**: Se un token scade o l'utente viene invalidato lato server, l'API risponde con codice `401 Unauthorized`. L'interceptor intercetta globalmente questo errore, pulisce il `localStorage` e forza un redirect immediato alla pagina `/login`, chiudendo la sessione in maniera sicura.

### 4.3 Controllo degli Accessi (Route Guards e RBAC)
La sicurezza della navigazione client-side è stratificata tramite **Route Guards**:
- **AuthGuard (`canActivate`)**: Verifica semplicemente l'esistenza del token locale. Se assente, ridireziona a `/login`.
- **RoleGuard (`canActivate`)**: Implementa la sicurezza RBAC (Role-Based Access Control). Estrae il claim `role` dal JWT e lo confronta con la lista di ruoli richiesti definita nelle proprietà `data: { roles: [...] }` delle rotte in `app-routing.module.ts`. Se il ruolo non matcha, l'utente viene respinto (e solitamente mandato a `/home` o al fallback di sicurezza).
- **Redirezione Intelligente (`AuthService.getRoleRedirectUrl`)**: Al login, o quando richiesto dalle policy, l'utente viene direzionato alla dashboard corretta per il proprio ruolo in modo automatico.

