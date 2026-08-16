# Stage 1: Costruzione (Build) dell'applicazione
FROM eclipse-temurin:25-jdk-jammy AS build
WORKDIR /app

# Copia i file necessari per Maven
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

# Rende eseguibile il wrapper di Maven e scarica le dipendenze
RUN chmod +x ./mvnw
RUN ./mvnw dependency:go-offline

# Copia il codice sorgente e compila l'applicazione ignorando i test per velocizzare
COPY src ./src
RUN ./mvnw clean package -DskipTests

# Stage 2: Esecuzione (Run) dell'applicazione
FROM eclipse-temurin:25-jre-jammy
WORKDIR /app

# Copia il jar compilato dallo Stage 1
COPY --from=build /app/target/*.jar app.jar

# Espone la porta di default di Spring Boot
EXPOSE 8080

# Avvia l'applicazione
ENTRYPOINT ["java", "-jar", "app.jar"]
