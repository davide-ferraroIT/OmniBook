# Documentazione di Deploy e Infrastruttura: OmniBook

Questo documento descrive in dettaglio i servizi cloud **effettivamente in uso** per il deploy dell'applicazione OmniBook, analizzandone il funzionamento e il motivo per cui sono stati scelti. L'infrastruttura si basa su un approccio moderno, fully-managed e serverless.

---

## 1. Backend: Render (PaaS)
**Servizio in uso:** [Render](https://render.com/) (Web Service)
**URL API in Produzione:** `https://omnibook-o73p.onrender.com/api/v1`

### Perché è stato scelto?
Render è una piattaforma Platform-as-a-Service (PaaS) che semplifica enormemente il deploy di applicazioni backend. È stato scelto per ospitare l'applicazione **Java / Spring Boot 3** perché:
*   **Zero-Ops:** Si occupa autonomamente del provisioning dei server, dei certificati SSL/TLS e del bilanciamento del carico.
*   **Supporto Docker Nativo:** Permette di deployare l'applicazione basandosi sul `Dockerfile` presente nella cartella `backend/`, garantendo un ambiente di esecuzione isolato e coerente tra locale e produzione.
*   **Integrazione GitHub:** Si collega al repository Git e avvia automaticamente le build ad ogni push sul branch principale, allineandosi con l'idea di Continuous Deployment.

### Come funziona?
Quando il codice viene pushato su GitHub, Render esegue la build basata sul `Dockerfile`. Lo stage 1 compila il pacchetto Maven `.jar` (ignorando i test per velocizzare la build dato che i test passano già su GitHub Actions), e lo stage 2 (basato su JRE) esegue il server Spring Boot esponendo la porta 8080. Render mappa automaticamente la porta 8080 su traffico HTTPS sicuro per il mondo esterno.

---

## 2. Database: Neon (Serverless PostgreSQL)
**Servizio in uso:** [Neon](https://neon.tech/)

### Perché è stato scelto?
Neon è un database **PostgreSQL serverless** che separa la componente di archiviazione (storage) da quella di elaborazione (compute).
*   **Costi Ottimizzati (Scale-to-zero):** Quando l'applicazione non riceve richieste, Neon può sospendere le risorse di calcolo (scale-to-zero) abbattendo i costi, riaccendendosi in poche frazioni di secondo quando arriva una nuova richiesta dal backend.
*   **Branching del Database:** Offre la possibilità di creare dei veri e propri "branch" del database, utilissimi per avere ambienti isolati per lo sviluppo locale, il testing (CI/CD) o staging senza dover clonare manualmente i dati.
*   **Compatibilità Totale:** Funziona nativamente con i driver JDBC PostgreSQL e supporta alla perfezione l'architettura data-driven (JSONB) che gestisce i campi dinamici dei Tenant.

### Come funziona?
Il backend Spring Boot si connette a Neon tramite un Connection String JDBC configurata nelle variabili d'ambiente (il parametro `DB_URL` al posto del classico `localhost`). Neon gestisce autonomamente l'alta disponibilità, le patch e i backup.

---

## 3. Gestione Media e Asset: Cloudinary
**Servizio in uso:** [Cloudinary](https://cloudinary.com/)

### Perché è stato scelto?
Per sollevare il backend e il database dalla responsabilità di archiviare e servire immagini (es. foto di profilo, logo del tenant, immagini dei servizi).
*   **Ottimizzazione on-the-fly:** Permette di ritagliare, comprimere e trasformare le immagini dinamicamente in base al dispositivo del client, migliorando drasticamente le performance web (Core Web Vitals).
*   **Global CDN:** Serve i file statici tramite Content Delivery Network, garantendo tempi di caricamento istantanei per gli utenti di tutto il mondo.

### Come funziona?
Il backend comunica con Cloudinary in modo sicuro tramite chiavi API dedicate (`CLOUDINARY_API_KEY`, `CLOUDINARY_API_SECRET`). Quando un utente carica un'immagine, Spring Boot fa l'upload direttamente su Cloudinary, riceve in risposta un URL CDN pubblico e salva solo l'URL all'interno del database PostgreSQL.

---

## 4. Frontend: Angular e Vercel
**Servizio in uso:** [Vercel](https://vercel.com/) (Web Hosting)
**URL in Produzione:** `https://omnibook.davide-ferraro.it`

### Perché è stato scelto?
Vercel è la piattaforma ideale per ospitare applicazioni frontend moderne e Single-Page Applications (SPA). È stata scelta per ospitare la web app Angular perché:
*   **Edge CDN Globale:** Offre un'infrastruttura superveloce a livello mondiale, garantendo caricamenti istantanei per gli utenti finali.
*   **Gestione Custom Domain:** Permette di collegare facilmente il dominio personalizzato tramite DNS, generando e rinnovando automaticamente i certificati SSL (HTTPS). L'URL fornito da Vercel è stato inserito nel record DNS del dominio principale, esponendo l'app sotto il sottodominio ufficiale `omnibook.davide-ferraro.it`.
*   **Integrazione nativa con GitHub:** Abilita un processo di Continuous Deployment trasparente, con deploy automatici per ogni push sul branch principale.

### Come funziona?
Il frontend è un'applicazione sviluppata in **Angular 18**.
Quando il codice viene aggiornato su GitHub (sul branch `main`), Vercel intercetta l'evento ed esegue automaticamente il comando di build del progetto (`npm run build`). Una volta completata la compilazione, Vercel preleva tutti gli asset statici (HTML, CSS, JavaScript) generati all'interno della cartella `www` e li distribuisce in modo istantaneo sui suoi nodi CDN periferici (Edge Network). L'applicazione web è così accessibile in sicurezza e ad alte prestazioni.

---

## 5. Continuous Integration (CI) - GitHub Actions
Nonostante l'hosting (come Render) gestisca la build finale e il deploy (Continuous Deployment), il controllo qualità (Continuous Integration) avviene su **GitHub Actions**:
*   **Backend CI (`backend-ci.yml`):** Testa l'applicazione Java usando Testcontainers (con un'immagine locale di PostgreSQL) per i test di integrazione, garantendo che non arrivino regressioni nel database o nella business logic prima del deploy.
*   **Frontend CI (`frontend-ci.yml`):** Esegue il Linting e Unit Test *Headless* del codice Angular ad ogni pull request, fermando il blocco di codice in caso di difetti architetturali.
