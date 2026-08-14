#!/bin/bash

# Abilita l'uscita in caso di errore
set -e

# Colori per l'output nel terminale
GREEN='\033[0;32m'
CYAN='\033[0;36m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Spostati nella cartella root del progetto (se lo script è chiamato da altrove)
cd "$(dirname "$0")/.."

echo -e "${CYAN}===========================================${NC}"
echo -e "${CYAN}      Avvio Ambiente Sviluppo OmniBook     ${NC}"
echo -e "${CYAN}===========================================${NC}"

echo -e "\n${GREEN}[1/3] Avvio del Database Postgres (Docker)...${NC}"
docker compose up -d

echo -e "\n${GREEN}[2/3] Avvio del Backend Spring Boot...${NC}"
cd backend

if [ ! -f ".env" ] && [ -f ".env.example" ]; then
    echo -e "${CYAN}File .env non trovato, lo creo partendo da .env.example...${NC}"
    cp .env.example .env
fi

if [ -f ".env" ]; then
    set -a
    source .env
    set +a
fi

./mvnw spring-boot:run &
BACKEND_PID=$!
cd ..

echo -e "\n${GREEN}[3/3] Avvio del Frontend Angular (Porta 8100)...${NC}"
cd frontend
npm start -- --port 8100 &
FRONTEND_PID=$!
cd ..

# Funzione per pulire alla chiusura
cleanup() {
    echo -e "\n${RED}Ricevuto segnale di interruzione. Chiusura in corso...${NC}"
    echo "Fermando il Backend (PID: $BACKEND_PID)..."
    kill $BACKEND_PID 2>/dev/null || true
    echo "Fermando il Frontend (PID: $FRONTEND_PID)..."
    kill $FRONTEND_PID 2>/dev/null || true
    echo "Fermando i servizi Docker..."
    docker compose stop
    echo -e "${GREEN}Chiusura completata. A presto!${NC}"
    exit 0
}

# Cattura i segnali SIGINT (CTRL+C) e SIGTERM per eseguire la pulizia
trap cleanup SIGINT SIGTERM

echo -e "\n${CYAN}In attesa che Backend e Frontend completino l'avvio...${NC}\n"

# Attendi che il Backend (porta 8080) risponda
until curl -s http://localhost:8080 > /dev/null 2>&1; do
    sleep 1
done

# Attendi che il Frontend (porta 8100) risponda
until curl -s http://localhost:8100 > /dev/null 2>&1; do
    sleep 1
done

# Pausa per permettere la stampa degli ultimi log di avvio di Angular/Spring
sleep 1.5

echo -e "\n${GREEN}===========================================${NC}"
echo -e "${GREEN} Tutti i servizi sono pronti e attivi!${NC}"
echo -e "${GREEN} - Frontend: http://localhost:8100${NC}"
echo -e "${GREEN} - Backend API: http://localhost:8080/api/v1${NC}"
echo -e "${GREEN} Premi CTRL+C per fermare e chiudere tutto.${NC}"
echo -e "${GREEN}===========================================${NC}\n"

# Mantiene in vita lo script aspettando la terminazione dei figli
wait $BACKEND_PID $FRONTEND_PID
