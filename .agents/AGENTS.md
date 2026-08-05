# OmniBook - Senior Development Best Practices

Le seguenti linee guida devono essere rigorosamente rispettate durante tutto lo sviluppo del progetto.

## 1. Architettura e Naming REST
- **Naming delle Risorse**: Nomi al plurale, sostantivi, gerarchia chiara (es. `GET /api/v1/resources`, `DELETE /api/v1/resources/{id}`). Nessun verbo negli URL.
- **API Versioning**: Utilizzare sempre il versionamento nell'URL (es. `/api/v1/...`) per prevenire rotture di compatibilità future.
- **Codici HTTP appropriati**:
  - `200 OK` (Lettura/Modifica)
  - `201 Created` (Creazione riuscita)
  - `204 No Content` (Eliminazione riuscita)
  - `400 Bad Request` (Errori di validazione)
  - `404 Not Found` (Risorsa non trovata)
  - `409 Conflict` (Stati conflittuali)

## 2. Pattern DTO e Disaccoppiamento
- **Mai esporre le `@Entity` nei Controller**: Qualsiasi transito HTTP deve avvenire esclusivamente tramite DTO.
- **Records Java**: Utilizzare i `record` di Java per request/response DTO in modo da garantire compattezza e immutabilità.

## 3. Gestione Errori e Validazione
- **Global Exception Handling**: Le eccezioni devono essere catturate globalmente tramite un `@RestControllerAdvice`. Restituire sempre payload coerenti (es. ProblemDetail / RFC 7807).
- **Validazione Dichiarativa**: Mantenere i controller puliti delegando i controlli di validazione a Jakarta Validation (annotazioni come `@Valid`, `@NotNull`, `@NotBlank`).

## 4. Livello Dati e Performance
- **Paginazione**: Le API che restituiscono collezioni devono obbligatoriamente implementare la paginazione (`Pageable`) per evitare Memory Leaks o query lente su tabelle di grandi dimensioni.
- **Transazionalità Ottimizzata**: Utilizzare `@Transactional(readOnly = true)` sui metodi di lettura nel Service layer per istruire Hibernate a disabilitare il dirty-checking, migliorando notevolmente le performance. Utilizzare `@Transactional` base per le scritture.

## 5. Sicurezza e Qualità del Codice
- **Isolamento Multi-Tenant**: In ogni operazione (lettura, modifica, eliminazione), il backend deve sempre verificare che la risorsa richiesta appartenga effettivamente al `tenant_id` dell'utente autenticato (prevenzione vulnerabilità IDOR).
- **Constructor Injection**: Usare l'iniezione delle dipendenze via Costruttore (es. tramite `@RequiredArgsConstructor` di Lombok su campi `private final`) e **mai** `@Autowired` sui field. Questo rende le classi immutabili e testabili con i Mock.
- **Logging Strutturato**: Vietato `System.out.println`. Utilizzare SLF4J (`@Slf4j`) con livelli logici adeguati (INFO per audit, DEBUG/TRACE per analisi, WARN/ERROR per problemi). Non loggare mai PII (Personal Identifiable Information) o chiavi segrete.
- **Idempotenza**: Assicurarsi che le richieste PUT o DELETE inviate ripetutamente lascino il server nel medesimo stato.
