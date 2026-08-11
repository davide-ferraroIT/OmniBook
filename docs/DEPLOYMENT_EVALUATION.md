# Valutazione Servizi Gratuiti per il Deployment di OmniBook

Per mettere online l'applicazione OmniBook minimizzando (o azzerando) i costi fissi, è necessario dividere l'architettura in **Backend**, **Database**, **Frontend Web** e **App Mobile**, scegliendo servizi Cloud con piani "Always Free" (Sempre Gratuiti) o quote mensili generose.

Ecco la valutazione delle migliori opzioni gratuite disponibili ad oggi:

## 1. Database (PostgreSQL)
L'applicazione utilizza PostgreSQL. Evitiamo i database offerti dalle piattaforme di hosting generiche (come Render) perché spesso hanno limiti di tempo (es. scadono dopo 90 giorni) o vanno in stop.
* **[Supabase](https://supabase.com/):** Offre un database Postgres serverless gratuito fino a 500 MB di dati. Non va in pausa se riceve traffico regolare. Include anche un sistema di Auth e Storage (se servirà salvare immagini dei barbieri/servizi).
* **[Neon.tech](https://neon.tech/):** Un Postgres serverless eccellente, offre 500 MB di storage gratuiti e branching dei dati (utile per fare test).
* **Raccomandazione:** **Supabase** o **Neon**.

## 2. Backend (Spring Boot / Java)
Le applicazioni Spring Boot richiedono un po' di memoria RAM (minimo 512MB) che molti tier gratuiti "base" faticano a fornire stabilmente, ma ci sono ottime soluzioni:
* **[Oracle Cloud (Always Free)](https://www.oracle.com/cloud/free/):** È **di gran lunga l'opzione migliore**. Ti dà gratuitamente 2 Virtual Machine (VPS) classiche e fino a **4 core ARM con 24 GB di RAM**. Ci puoi far girare il backend, il database e Keycloak tutto insieme usando Docker. Il difetto? La registrazione a volte è schizzinosa con le carte di credito.
* **[Koyeb](https://www.koyeb.com/):** Offre un tier gratuito con 512MB RAM (tramite container Docker). Perfetto per un backend Spring Boot se ottimizzato.
* **[Render](https://render.com/):** Offre un web service gratuito (512 MB RAM), ma l'applicazione **si spegne (spin down)** dopo 15 minuti di inattività, causando tempi di caricamento (cold start) di 30-40 secondi alla prima visita di un cliente.
* **Raccomandazione:** **Oracle Cloud** (per potenza e no-sleep) oppure **Koyeb**.

## 3. Frontend Web / Desktop
Il frontend è un'applicazione Angular/Ionic (Single Page Application). Produce file statici (HTML, JS, CSS) facilissimi da ospitare gratis, con zero lag.
* **[Vercel](https://vercel.com/) / [Netlify](https://www.netlify.com/):** Piani gratuiti generosissimi, perfetti per Angular. Si collegano a GitHub e pubblicano l'app automaticamente ad ogni push.
* **[Cloudflare Pages](https://pages.cloudflare.com/):** Veloce, illimitato per piccoli progetti e con il miglior CDN al mondo, tutto gratis.
* **Raccomandazione:** **Vercel** o **Cloudflare Pages**.

## 4. Applicazione Mobile (iOS / Android)
Essendo scritta in Ionic, il codice del Frontend Web è già l'App Mobile. La logica gira sempre sui server (Backend + DB), quindi l'App in sé non costa nulla di mantenimento cloud. Ma ci sono costi di "pubblicazione":
* **Android (Google Play Store):** L'app non ha costi mensili. Per pubblicarla sugli store c'è una tassa *una tantum* (si paga una volta sola per sempre) di **$25** a Google.
* **iOS (Apple App Store):** Apple richiede l'iscrizione all'Apple Developer Program che costa **$99 / anno**.
* **Notifiche Push:** Se vuoi mandare notifiche (es. "La tua prenotazione è confermata"), **Firebase Cloud Messaging (FCM)** è completamente **gratuito**.
* **Raccomandazione:** Per iniziare a costo 0, puoi distribuire l'app Android come "APK" scaricabile dal sito web. Per IOS e PlayStore bisogna sostenere i costi di licenza piattaforma.

## 5. Altri Servizi Necessari
* **Autenticazione (IAM):** 
  * Invece di configurare un Keycloak custom sul backend (che consuma molte risorse), ti consiglio di usare **[Auth0](https://auth0.com/)** o l'Auth di **Supabase**. Auth0 è il leader di mercato e offre fino a **7.500 utenti attivi gratuiti al mese**. Gestisce login, social login e recupero password.
* **Invio Email (Conferma prenotazioni):**
  * **[Resend](https://resend.com/):** 3.000 email gratuite al mese. Moderno e con SDK Java.
  * **[Brevo](https://www.brevo.com/):** 300 email gratuite al giorno (9.000 al mese).

---

## Il "Pacchetto" Ideale Consigliato (Costo mensile: 0€)

1. **Frontend (Web/Admin):** Ospitato su **Vercel** (Gratis).
2. **Backend (Spring Boot):** Caricato come Docker image su **Koyeb** (Gratis). *(Oppure su Oracle Cloud).*
3. **Database:** **Supabase** (PostgreSQL gratis e senza ibernazione).
4. **Login e Sicurezza:** **Auth0** o Supabase Auth (Gratis).
5. **Email Transazionali:** **Resend** (Gratis).
6. **Mobile:** Ionic + Firebase FCM per le notifiche (Gratis) -> (25$ una tantum a Google se vuoi il PlayStore ufficiale).
