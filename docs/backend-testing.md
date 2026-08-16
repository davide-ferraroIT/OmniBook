# Linee Guida per i Test (Backend)

Questo documento definisce gli standard (Gold Standard) da adottare per tutti i futuri sviluppi della suite di test del backend in Spring Boot. L'applicazione persegue l'obiettivo di una **Code Coverage del ~100%** sulla business logic.

> [!IMPORTANT]
> A partire dalla versione attuale, la pipeline di build Maven applica una regola restrittiva tramite il plugin **JaCoCo**. Se la copertura del codice scende sotto il **90%** (sia per linee di codice che per ramificazioni/branches logici), **la build fallirà automaticamente**. Questo garantisce che nessun codice venga introdotto nel ramo principale (main) senza i dovuti test.

## 1. Pattern Architetturale: BDD (Behavior-Driven Development)
Tutti gli unit test devono essere redatti seguendo il pattern `Given-When-Then` per massimizzare la leggibilità:

- **Given**: Setup del contesto e dei mock. Utilizzare `BDDMockito.given()` al posto del classico `Mockito.when()`.
- **When**: Esecuzione del metodo o del componente *SUT* (System Under Test).
- **Then**: Asserzioni sui risultati e verifiche sulle interazioni. Utilizzare `BDDMockito.then().should()` invece di `Mockito.verify()`.

```java
// Esempio pratico
@Test
@DisplayName("Recupero utente per email - Successo")
void findByEmail_ShouldReturnUser() {
    // Given
    given(userRepository.findByEmail(eq("test@test.com"))).willReturn(Optional.of(user));

    // When
    User response = userService.findByEmail("test@test.com");

    // Then
    assertThat(response).isNotNull();
    then(userRepository).should().findByEmail(eq("test@test.com"));
}
```

## 2. Naming Conventions e `@DisplayName`
- **Metodi di Test**: Usare il pattern `nomeMetodo_Scenario_RisultatoAtteso` (es. `create_ShouldThrowException_WhenResourceIsFull`).
- **@DisplayName**: Ogni classe di test e ogni metodo deve essere decorato con l'annotazione `@DisplayName` di JUnit 5, contenente una descrizione concisa in linguaggio naturale (es. *"Creazione prenotazione - Eccezione se la risorsa non è disponibile"*). Questo è cruciale per report CI/CD comprensibili anche a non tecnici.

## 3. Asserzioni e Mocking
- **AssertJ**: Sostituire le classiche asserzioni di JUnit (`assertEquals`, `assertTrue`) con l'approccio dichiarativo e fluente di **AssertJ** (`assertThat(...)`).
- **MockitoExtension**: Evitare l'inizializzazione manuale dei mock (es. `MockitoAnnotations.openMocks(this)`) o l'uso improprio di `@SpringBootTest` per gli unit test. Utilizzare `@ExtendWith(MockitoExtension.class)` per una corretta e leggera iniezione dei `@Mock` e `@InjectMocks`.

## 4. Test Architetturali (ArchUnit)
L'integrità dell'architettura a livelli (N-Tier) è garantita meccanicamente tramite **ArchUnit**.
I test validano automaticamente le dipendenze tra i package, assicurandosi che:
- I `Controllers` non abbiano accesso diretto ai `Repositories`.
- I `Services` siano richiamabili solo dai `Controllers` (e da altri Services).
- Nessun layer inferiore possa dipendere da un layer superiore.

## 5. Test d'Integrazione (DataJpaTest) e Testcontainers
I test di repository e l'integrazione del DB (specialmente per le query native o l'uso di `JSONB`) devono avvalersi dell'annotazione `@DataJpaTest`.
- L'infrastruttura sottostante si affida a **Testcontainers** e `@ServiceConnection` per avviare istanze effimere di *PostgreSQL*.
- Non utilizzare mai `H2` in memoria se le query JPQL fanno uso di specifiche Postgres (come i cast nativi).

## 6. Security Validation
Le classi responsabili della gestione JWT o WebSecurity (filtri, authentication entry points) devono essere testate simulando una chiamata.
L'utilizzo della libreria `spring-security-test` (in particolare `@WithMockUser`) è il *gold standard* per bypassare interamente la generazione fittizia dei security context in fase di test dei controller.

## 7. Report HTML (Code Coverage)
Al termine di ogni esecuzione della suite di test tramite il comando `mvn clean test` o `mvn verify`, il plugin JaCoCo **genera automaticamente un report HTML interattivo** della *Code Coverage*. 
Questo report mostra in dettaglio le righe di codice coperte, omesse e i rami decisionali esplorati o mancati.

- Il report è consultabile aprendo nel browser il file: `backend/target/site/jacoco/index.html`.
- Si raccomanda di utilizzare questo report per identificare visivamente le aree del codice (in rosso) che necessitano di test aggiuntivi per mantenere il rispetto delle soglie minime di sicurezza.
