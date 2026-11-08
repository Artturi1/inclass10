FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY init.sql ./init.sql
COPY src ./src
RUN mvn -B clean verify

FROM eclipse-temurin:17-jre
WORKDIR /app

RUN apt-get update && apt-get install -y --no-install-recommends \
    libasound2t64 libgtk-3-0t64 libx11-6 libxext6 libxrender1 libxtst6 libxi6 \
    libxrandr2 libxfixes3 libxcursor1 libxinerama1 libxxf86vm1 wget unzip \
    fonts-noto-core fonts-noto-cjk \
    && rm -rf /var/lib/apt/lists/*

RUN mkdir -p /javafx-sdk \
    && wget -O /tmp/javafx.zip https://download2.gluonhq.com/openjfx/21.0.4/openjfx-21.0.4_linux-x64_bin-sdk.zip \
    && unzip /tmp/javafx.zip -d /javafx-sdk \
    && rm /tmp/javafx.zip

COPY --from=build /app/target/temperature-converter-1.0-SNAPSHOT.jar app.jar
COPY --from=build /app/target/dependency ./dependency
ENV DISPLAY=host.docker.internal:0.0

CMD ["java", "--module-path", "/javafx-sdk/javafx-sdk-21.0.4/lib", "--add-modules", "javafx.controls,javafx.fxml", "-Dprism.order=sw", "-cp", "app.jar:dependency/*", "TemperatureConverter"]
