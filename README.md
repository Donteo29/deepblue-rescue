Sí, bro. Revisé nuevamente el documento porque **sí hay preguntas que el profesor pide responder durante el laboratorio**. Algunas aparecen como preguntas de análisis en los pasos, y otras son parte de las consultas del reto integrador. Por ejemplo, antes de programar pide responder 7 preguntas sobre FK, 1:1, tabla intermedia y Treatment.  También pide explicar por qué se usa `ddl-auto: validate` y no `update`.

Pero **esas preguntas no hacen parte de los 10 puntos obligatorios del README**. El README debe contener la documentación indicada por el profesor.

Te dejo **solo el contenido para pegar en `README.md`**, incluyendo tus datos:

````markdown
# DeepBlue Rescue

## Información del estudiante

**Nombre:** William Elias Gonzalez Solano  
**Código:** 2022114040

---

## 1. Descripción del proyecto

DeepBlue Rescue es un proyecto desarrollado para implementar la capa de persistencia de una plataforma dedicada al rescate y rehabilitación de fauna marina.

El proyecto utiliza Java 21, Spring Boot 4, Spring Data JPA, Hibernate, Flyway, PostgreSQL y Testcontainers.

El alcance del proyecto se limita a la capa de persistencia. No se implementan Controller, REST API, Service, DTO, Spring Security, Frontend, Kafka ni Docker Compose.

---

## 2. Tecnologías utilizadas

- Java 21
- Spring Boot 4
- Spring Data JPA
- Hibernate
- Maven
- Flyway
- PostgreSQL
- Testcontainers
- JUnit

---

## 3. Modelo de datos

El sistema está compuesto por las siguientes entidades:

- RescueCenter
- RescueCase
- Animal
- MedicalRecord
- Specialist
- Expertise
- Treatment

Además, se utiliza la tabla asociativa `specialist_expertise` para representar la relación entre especialistas y áreas de experiencia.

---

## 4. Relaciones entre entidades

Las principales relaciones del modelo son:

- `RescueCenter 1:N RescueCase`
  - Un centro de rescate puede gestionar múltiples casos.
  - Cada caso pertenece a un centro.

- `RescueCase 1:1 Animal`
  - Cada caso de rescate se relaciona con un animal.

- `Animal 1:1 MedicalRecord`
  - Cada animal puede tener un expediente médico.

- `Specialist N:M Expertise`
  - Un especialista puede tener varias áreas de experiencia.
  - Una experiencia puede pertenecer a varios especialistas.
  - La relación se representa mediante `specialist_expertise`.

- `Animal 1:N Treatment`
  - Un animal puede recibir múltiples tratamientos.

- `Specialist 1:N Treatment`
  - Un especialista puede realizar múltiples tratamientos.

---

## 5. Base de datos y migraciones

El esquema de la base de datos es administrado mediante Flyway.

Las migraciones utilizadas son:

- `V1__create_schema.sql`
  - Crea las tablas, relaciones, claves primarias, claves foráneas, restricciones UNIQUE, CHECK e índices.

- `V2__insert_expertise_catalog.sql`
  - Inserta el catálogo inicial de áreas de experiencia.

- `V3__add_tracking_device_to_animal.sql`
  - Agrega el campo `tracking_device_code` a la entidad Animal y establece su restricción UNIQUE.

Hibernate utiliza:

```yaml
ddl-auto: validate
````

Esto permite que Hibernate valide que el modelo de entidades coincide con el esquema existente, mientras que Flyway se encarga de crear y evolucionar la estructura de la base de datos.

---

## 6. Ejecución del proyecto

Para compilar el proyecto:

```bash
.\mvnw.cmd clean compile
```

Para ejecutar la aplicación:

```bash
.\mvnw.cmd spring-boot:run
```

La configuración de conexión a PostgreSQL se encuentra en:

```text
src/main/resources/application.yml
```

---

## 7. Ejecución de pruebas

Las pruebas de integración se ejecutan mediante:

```bash
.\mvnw.cmd clean test
```

Las pruebas utilizan PostgreSQL mediante Testcontainers, por lo que no se utiliza H2.

El resultado final de las pruebas debe mostrar:

```text
BUILD SUCCESS
```

---

## 8. Testcontainers

Testcontainers permite ejecutar las pruebas de integración utilizando una instancia real de PostgreSQL dentro de un contenedor.

La clase de pruebas utiliza `@Testcontainers` y `@Container`, junto con `@ServiceConnection`, para conectar automáticamente Spring Boot con la base de datos utilizada durante las pruebas.

De esta manera, las pruebas se ejecutan sobre PostgreSQL real y permiten comprobar también el comportamiento de las restricciones de la base de datos.

---

## 9. Query Methods implementados

Se implementaron Query Methods mediante Spring Data JPA para resolver consultas que pueden expresarse directamente mediante los nombres de los métodos.

### RescueCenterRepository

```text
findByCode(String code)
```

Permite buscar un centro mediante su código.

### RescueCaseRepository

```text
findByCaseCode(String caseCode)

findByStatusOrderByRescueDateAsc(RescueStatus status)

findByRescueCenter_Code(String code)
```

Permiten buscar casos por código, estado y centro de rescate.

### AnimalRepository

```text
findByAnimalCode(String animalCode)

findByCommonNameContainingIgnoreCase(String commonName)

findByRescueCase_Status(RescueStatus status)

findByRescueCase_RescueCenter_Code(String centerCode)

findByRescueCase_RescueDateAfterOrderByRescueCase_RescueDateDesc(LocalDate date)
```

Permiten realizar búsquedas de animales mediante diferentes propiedades y relaciones.

### ExpertiseRepository

```text
findByNameIgnoreCase(String name)
```

Permite buscar un área de experiencia ignorando mayúsculas y minúsculas.

### TreatmentRepository

```text
findByAnimal_IdOrderByPerformedAtAsc(Long animalId)
```

Permite obtener los tratamientos de un animal ordenados cronológicamente.

---

## 10. Consultas JPQL implementadas

Se utilizaron consultas `@Query` con JPQL cuando las consultas requerían navegar múltiples relaciones.

### SpecialistRepository

```text
findActiveByExpertiseNameIgnoreCase(String expertiseName)
```

Obtiene especialistas activos que poseen una determinada área de experiencia.

Utiliza:

* `JOIN`
* `DISTINCT`
* `LOWER`
* parámetro nombrado
* `ORDER BY`

### AnimalRepository

```text
findAnimalsInStatusWithTreatmentBySpecialistExpertise(...)
```

Obtiene animales que se encuentran en un determinado estado y que han recibido tratamientos realizados por especialistas con una determinada experiencia.

Utiliza múltiples `JOIN` y `DISTINCT`.

### TreatmentRepository

```text
findByPerformedAtBetween(...)
```

Obtiene tratamientos realizados dentro de un intervalo de fechas.

```text
findByRescueCenterCode(...)
```

Obtiene tratamientos realizados a animales pertenecientes a un centro determinado, navegando desde Treatment hacia Animal, RescueCase y RescueCenter.

```text
findBySpecialistExpertise(...)
```

Obtiene tratamientos realizados por especialistas que poseen una determinada área de experiencia.

---

## 11. Integridad de datos

El proyecto implementa restricciones de integridad directamente en PostgreSQL:

* Primary Keys (`PK`)
* Foreign Keys (`FK`)
* `UNIQUE`
* `NOT NULL`
* `CHECK`

Estas restricciones fueron verificadas mediante pruebas de integración.

Se probaron específicamente:

* códigos de animales duplicados;
* claves foráneas inválidas;
* estados de rescate no permitidos.

---

## 12. Reto integrador

Se implementó y probó un escenario completo de rescate que relaciona:

```text
RescueCenter
      ↓
RescueCase
      ↓
Animal
      ↓
MedicalRecord
```

y:

```text
Animal
   ↓
Treatment
   ↓
Specialist
   ↓
Expertise
```

El escenario utiliza el centro `DB-CAR`, el caso `RES-2026-100` y el animal `AN-2026-100`.

También se implementaron consultas para:

* buscar el caso por código;
* obtener casos en rehabilitación;
* obtener animales de un centro;
* buscar animales por nombre común;
* obtener especialistas con experiencia en Trauma;
* obtener tratamientos de un animal;
* obtener tratamientos por experiencia del especialista;
* obtener tratamientos dentro de un intervalo de fechas;
* obtener animales en rehabilitación tratados por especialistas con experiencia en Trauma.

---

## 13. Resultado de las pruebas

El proyecto cuenta con pruebas de integración para validar:

* Migraciones Flyway.
* Métodos heredados de `JpaRepository`.
* Relaciones `1:N`.
* Relaciones `1:1`.
* Relaciones `N:M`.
* Query Methods.
* Consultas JPQL.
* Restricciones `UNIQUE`.
* Restricciones `FK`.
* Restricciones `CHECK`.
* Evolución del esquema mediante Flyway.
* Reto integrador.

Resultado final:

```text
Tests run: 31
Failures: 0
Errors: 0
Skipped: 0

BUILD SUCCESS
```

``


