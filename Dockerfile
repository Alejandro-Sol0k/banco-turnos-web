FROM eclipse-temurin:24-jdk-alpine AS build
WORKDIR /app
COPY . .

# Compila apuntando directo al driver exacto
# Reemplaza tu línea actual de RUN javac por esta:
RUN find . -name "*.java" > sources.txt && javac -d out -cp "postgresql-42.7.8.jar:src/postgresql-42.7.8.jar:." @sources.txt
FROM eclipse-temurin:24-jre-alpine
WORKDIR /app

COPY --from=build /app/out ./out
COPY --from=build /app/web ./web
# Copiamos únicamente el conector de Postgres
COPY --from=build /app/postgresql-42.7.8.jar ./ 

# Arranca el servidor con el nombre exacto del driver
CMD ["java", "-cp", "out:postgresql-42.7.8.jar:.", "BancoApiServer"]
