# Backend Backlog (Technical Debt & Features)

Questo documento traccia le funzionalità backend che sono state rimandate per dare priorità allo sviluppo del Frontend e alla chiusura del MVP visivo. Dovranno essere implementate prima della messa in produzione.

## 1. Sicurezza e Autenticazione (IAM)
- [ ] Integrare Spring Security.
- [ ] Configurare JWT o un Identity Provider esterno (es. Firebase Auth, Keycloak, Auth0).
- [ ] Creare ruoli (`ROLE_SUPERADMIN`, `ROLE_TENANT_ADMIN`, `ROLE_CUSTOMER`).
- [ ] Proteggere gli endpoint amministrativi (es. creazione risorse/servizi) limitandoli ai proprietari del Tenant verificando l'identità tramite token.

## 2. CORS (Cross-Origin Resource Sharing)
- [ ] Configurare un `WebMvcConfigurer` globale per permettere le richieste HTTP provenienti dal dominio del frontend (es. `http://localhost:4200` o domini white-label), altrimenti le chiamate REST verranno bloccate dai browser.

## 3. Gestione Notifiche (Email/SMS)
- [ ] Configurare Spring Boot Email o servizi di terze parti (es. SendGrid, Resend, Twilio).
- [ ] Implementare eventi asincroni (es. `@Async`, `ApplicationEventPublisher` o RabbitMQ) per l'invio di notifiche di conferma quando lo stato di un `Booking` cambia.

## 4. Migrazioni Database (Flyway / Liquibase)
- [ ] Rimuovere l'impostazione `spring.jpa.hibernate.ddl-auto=update` da `application.yml`.
- [ ] Introdurre Flyway.
- [ ] Creare gli script SQL iniziali (`V1__init_schema.sql`) basati sulle entità attuali per avere uno storico immutabile del database.

## 5. Gateway di Pagamento (Feature on-demand)
- [ ] Implementare l'integrazione Stripe / PayPal se il `TenantConfig` possiede il `FeatureModule.ONLINE_PAYMENTS` attivo per la prenotazione.
