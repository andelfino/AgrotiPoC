# Imagen de compilación: JDK 21 + Ant (el proyecto arma el JAR con build.xml).
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

RUN apt-get update \
    && apt-get install -y --no-install-recommends ant \
    && rm -rf /var/lib/apt/lists/*

COPY build.xml .
COPY src ./src

RUN ant jar

# Imagen de ejecución: solo JRE; copiamos el JAR y los JDBC/Gson descargados por Ant en lib/.
FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=build /app/build/agromonitor-backend.jar .
COPY --from=build /app/lib ./lib

ENV PORT=8080
EXPOSE 8080

# Classpath: dependencias primero, luego el JAR de la app (igual que el target "run" de Ant).
ENTRYPOINT ["sh", "-c", "exec java -cp \"lib/*:agromonitor-backend.jar\" com.agromonitor.Main"]
