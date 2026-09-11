# DeepBlue Rescue

## Información del estudiante

**Nombre:** William Elias Gonzalez Solano  
**Código:** 2022114040

## Repositorio

[Ver proyecto en GitHub](https://github.com/Donteo29/deepblue-rescue)

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

- `RescueCenter`
- `RescueCase`
- `Animal`
- `MedicalRecord`
- `Specialist`
- `Expertise`
- `Treatment`

También se utiliza la tabla intermedia `specialist_expertise` para representar la relación entre especialistas y áreas de experiencia.

---

## 4. Relaciones entre entidades

### RescueCenter 1:N RescueCase

Un centro de rescate puede gestionar múltiples casos de rescate y cada caso pertenece a un centro.

### RescueCase 1:1 Animal

Cada caso de rescate se relaciona con un animal.

### Animal 1:1 MedicalRecord

Cada animal puede tener un expediente médico.

### Specialist N:M Expertise

Un especialista puede tener varias áreas de experiencia y una misma área puede pertenecer a varios especialistas.

La relación se representa mediante la tabla intermedia:

```text
specialist_expertise
Animal 1:N Treatment

Un animal puede recibir múltiples tratamientos.

Specialist 1:N Treatment

Un especialista puede realizar múltiples tratamientos.

5. Base de datos y migraciones

El esquema de la base de datos es administrado mediante Flyway.

V1 - Creación del esquema

Archivo:

V1__create_schema.sql

Crea las tablas, relaciones, claves primarias, claves foráneas, restricciones UNIQUE, CHECK e índices.

V2 - Catálogo de especialidades

Archivo:

V2__insert_expertise_catalog.sql

Inserta el catálogo inicial de áreas de experiencia:

Marine Reptiles
Marine Mammals
Marine Birds
Trauma
Rehabilitation
Toxicology
V3 - Dispositivo de seguimiento

Archivo:

V3__add_tracking_device_to_animal.sql

Agrega el campo tracking_device_code a Animal y establece una restricción UNIQUE.

Hibernate utiliza:

ddl-auto: validate

Esto permite que Hibernate valide que el modelo de entidades coincide con el esquema de la base de datos, mientras que Flyway se encarga de crear y evolucionar dicho esquema.

6. Ejecución del proyecto

Para compilar el proyecto:

.\mvnw.cmd clean compile

Para ejecutar la aplicación:

.\mvnw.cmd spring-boot:run

La configuración de PostgreSQL se encuentra en:

src/main/resources/application.yml
7. Ejecución de pruebas

Las pruebas de integración se ejecutan mediante:

.\mvnw.cmd clean test

Las pruebas utilizan PostgreSQL mediante Testcontainers, por lo que no se utiliza H2.

Resultado final:

Tests run: 31
Failures: 0
Errors: 0
Skipped: 0

BUILD SUCCESS
8. Testcontainers

Testcontainers permite ejecutar las pruebas de integración utilizando una instancia real de PostgreSQL dentro de un contenedor.

La clase de pruebas utiliza:

@Testcontainers
@Container
@ServiceConnection

Esto permite conectar automáticamente Spring Boot con la base de datos utilizada durante las pruebas.

De esta manera, las pruebas se ejecutan sobre PostgreSQL real y permiten comprobar también el comportamiento de las restricciones de la base de datos.

9. Query Methods implementados

Se implementaron Query Methods mediante Spring Data JPA.

RescueCenterRepository
findByCode(String code)

Permite buscar un centro mediante su código.

RescueCaseRepository
findByCaseCode(String caseCode)

findByStatusOrderByRescueDateAsc(RescueStatus status)

findByRescueCenter_Code(String code)

Permiten buscar casos por código, estado y centro de rescate.

AnimalRepository
findByAnimalCode(String animalCode)

findByCommonNameContainingIgnoreCase(String commonName)

findByRescueCase_Status(RescueStatus status)

findByRescueCase_RescueCenter_Code(String centerCode)

findByRescueCase_RescueDateAfterOrderByRescueCase_RescueDateDesc(LocalDate date)

Permiten realizar búsquedas de animales mediante diferentes propiedades y relaciones.

ExpertiseRepository
findByNameIgnoreCase(String name)

Permite buscar un área de experiencia ignorando mayúsculas y minúsculas.

TreatmentRepository
findByAnimal_IdOrderByPerformedAtAsc(Long animalId)

Permite obtener los tratamientos de un animal ordenados cronológicamente.

10. Consultas JPQL implementadas

Se utilizaron consultas @Query con JPQL cuando las consultas requerían navegar múltiples relaciones.

SpecialistRepository
findActiveByExpertiseNameIgnoreCase(String expertiseName)

Obtiene especialistas activos que poseen una determinada área de experiencia.

Utiliza:

JOIN
DISTINCT
LOWER
Parámetros nombrados
ORDER BY
AnimalRepository
findAnimalsInStatusWithTreatmentBySpecialistExpertise(...)

Obtiene animales que se encuentran en un determinado estado y que han recibido tratamientos realizados por especialistas con una determinada área de experiencia.

Utiliza múltiples JOIN y DISTINCT.

TreatmentRepository
findByPerformedAtBetween(...)

Obtiene tratamientos realizados dentro de un intervalo de fechas.

findByRescueCenterCode(...)

Obtiene tratamientos realizados a animales pertenecientes a un centro determinado, navegando desde Treatment hacia Animal, RescueCase y RescueCenter.

findBySpecialistExpertise(...)

Obtiene tratamientos realizados por especialistas que poseen una determinada área de experiencia.

11. Integridad de datos

El proyecto implementa restricciones de integridad directamente en PostgreSQL:

Primary Keys (PK)
Foreign Keys (FK)
UNIQUE
NOT NULL
CHECK

Estas restricciones fueron verificadas mediante pruebas de integración.

Se probaron específicamente:

Códigos de animales duplicados.
Claves foráneas inválidas.
Estados de rescate no permitidos.
12. Reto integrador

Se implementó y probó un escenario completo de rescate que relaciona:

RescueCenter
      ↓
RescueCase
      ↓
Animal
      ↓
MedicalRecord

Y:

Animal
   ↓
Treatment
   ↓
Specialist
   ↓
Expertise

El escenario utiliza:

Centro: DB-CAR
Caso: RES-2026-100
Animal: AN-2026-100

También se implementaron consultas para:

Buscar el caso por código.
Obtener casos en rehabilitación.
Obtener animales de un centro.
Buscar animales por nombre común.
Obtener especialistas con experiencia en Trauma.
Obtener tratamientos de un animal.
Obtener tratamientos por experiencia del especialista.
Obtener tratamientos dentro de un intervalo de fechas.
Obtener animales en rehabilitación tratados por especialistas con experiencia en Trauma.
13. Resultado final

El proyecto cuenta con pruebas de integración para validar:

Migraciones Flyway.
Métodos heredados de JpaRepository.
Relaciones 1:N.
Relaciones 1:1.
Relaciones N:M.
Query Methods.
Consultas JPQL.
Restricciones UNIQUE.
Restricciones FK.
Restricciones CHECK.
Evolución del esquema mediante Flyway.
Reto integrador.
Resultado de las pruebas
Tests run: 31
Failures: 0
Errors: 0
Skipped: 0

BUILD SUCCESS
Repositorio

El proyecto completo se encuentra disponible en:

https://github.com/Donteo29/deepblue-rescue