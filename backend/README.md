# API de gastos

Esqueleto de un prototipo REST para un unico usuario. Los endpoints CRUD y el
resumen mensual por categorias todavia no estan implementados.

## Tecnologias

- Java 25 (sin funciones preview).
- Spring Boot 4.1.2.
- Gradle 9.1.0, incluido mediante Gradle Wrapper.
- Spring Web MVC, Spring Data JPA y Bean Validation.
- H2 embebida y persistente en archivo.
- Spring Boot Test, JUnit Jupiter y AssertJ.

Spring Boot gestiona las versiones compatibles de las bibliotecas mediante su BOM.
No se incluye autenticacion ni gestion de multiples usuarios.

## Estructura

```text
backend/
  build.gradle
  settings.gradle
  gradlew / gradlew.bat
  gradle/wrapper/
  src/main/java/com/unir/gastos/
    GastosApplication.java
    controller/   # Endpoints REST
    service/      # Logica de negocio y resumen mensual
    repository/   # Interfaces Spring Data JPA
    entity/       # Entidades persistentes
    dto/          # Objetos de entrada y salida
  src/main/resources/application.properties
  src/test/java/com/unir/gastos/GastosApplicationTests.java
  data/           # H2 crea este directorio al arrancar
```

Los paquetes contienen un `package-info.java` para conservar la estructura inicial.

## Ejecutar

Requisito: JDK 25 instalado y disponible mediante `JAVA_HOME` o `PATH`.
No hace falta instalar Gradle. La primera ejecucion requiere acceso a Internet.

Desde la raiz del workspace:

```sh
cd backend
./gradlew bootRun
```

El servidor escucha en http://localhost:8080. Al no haber endpoints definidos,
una peticion a `/` devuelve 404.

## Pruebas y empaquetado

Desde `backend/`:

```sh
./gradlew test
./gradlew bootJar
java -jar build/libs/gastos-api-0.0.1-SNAPSHOT.jar
```

En Windows, utilizar `gradlew.bat` en lugar de `./gradlew`.

## Persistencia

La URL `jdbc:h2:file:./data/gastos` guarda los datos en `data/gastos.mv.db`.
Ejecutar siempre desde `backend/`, tambien al utilizar el JAR, para mantener
esa ubicacion dentro del backend y reutilizar la misma base al reiniciar.
`bootRun` fija ese directorio automaticamente.

`spring.jpa.hibernate.ddl-auto=update` permite crear y actualizar las tablas de
las futuras entidades sin recrearlas en cada arranque. Es una configuracion para
el prototipo; una aplicacion de produccion deberia utilizar migraciones.

Los datos y los archivos de compilacion estan excluidos de Git. Las pruebas usan
una base separada en `build/test-data/` y no modifican la base de desarrollo.
La consola web H2 no esta habilitada.