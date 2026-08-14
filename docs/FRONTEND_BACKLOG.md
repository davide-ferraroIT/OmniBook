# Frontend Backlog

Questo documento traccia le attività future e i debiti tecnici per l'applicazione Angular/Ionic.

## Autenticazione e Sicurezza
- [ ] **Protezione Rotte Admin**: Implementare Guard in Angular (es. `AuthGuard`) per proteggere le rotte `/admin/*`. La dashboard al momento è accessibile pubblicamente. Richiederà l'integrazione con il sistema IAM (Keycloak/Auth0) che verrà sviluppato lato backend.

## UI/UX
- [ ] Internazionalizzazione (i18n).
- [ ] Dark Mode nativa in base alle preferenze del sistema (attualmente il CSS è custom per themer).
- [ ] Aggiunta Notifiche push / Toast per l'amministratore all'arrivo di nuove prenotazioni (WebSocket/SSE).

## Funzionalità Admin
- [ ] Rendere le prenotazioni esistenti modificabili (orario, dati cliente, ecc.) per l'amministratore tramite l'interfaccia UI.
- [ ] **Gestione Servizi (CRUD)**: Creare l'interfaccia UI per permettere all'amministratore di aggiungere/modificare i servizi offerti (nome, descrizione, durata, risorse richieste).
- [ ] **Gestione Calendario e Orari**: Interfaccia per configurare gli orari di apertura del negozio e la vista calendario avanzata (che supporti gli slot dinamici in base alla durata del servizio).

## Architettura e Navigazione
- [ ] Gestione completa del routing dell'applicazione (redirect avanzati, landing pages, pagina 404, ecc.).
