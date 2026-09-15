# DeepBlue Rescue

## Información del estudiante

**Nombre:** William Elias Gonzalez Solano  
**Código:** 2022114040

## Repositorio

[Ver proyecto en GitHub](https://github.com/Donteo29/deepblue-rescue)

---

## 1. Descripción del proyecto

DeepBlue Rescue es un proyecto desarrollado para implementar una plataforma orientada al rescate y rehabilitación de fauna marina.

El proyecto utiliza Java 21, Spring Boot 4, Spring Data JPA, Hibernate, Flyway, PostgreSQL, Testcontainers, JUnit 5, Mockito, AssertJ y MapStruct.

El proyecto se ha desarrollado por capas. La primera parte corresponde a la **capa de persistencia** y la segunda incorpora la **capa de servicio**, DTOs, mappers, manejo de excepciones y pruebas unitarias.

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
- JUnit 5
- Mockito
- AssertJ
- MapStruct

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

La relación se representa mediante la tabla intermedia `specialist_expertise`.

### Animal 1:N Treatment

Un animal puede recibir múltiples tratamientos.

### Specialist 1:N Treatment

Un especialista puede realizar múltiples tratamientos.

---

## 5. Base de datos y migraciones

El esquema de la base de datos es administrado mediante Flyway.

### V1 - Creación del esquema

Archivo:

`V1__create_schema.sql`

Crea las tablas, relaciones, claves primarias, claves foráneas, restricciones `UNIQUE`, `CHECK` e índices.

### V2 - Catálogo de especialidades

Archivo:

`V2__insert_expertise_catalog.sql`

Inserta el catálogo inicial de áreas de experiencia:

- Marine Reptiles
- Marine Mammals
- Marine Birds
- Trauma
- Rehabilitation
- Toxicology

### V3 - Dispositivo de seguimiento

Archivo:

`V3__add_tracking_device_to_animal.sql`

Agrega el campo `tracking_device_code` a `Animal` y establece una restricción `UNIQUE`.

Hibernate utiliza:

`ddl-auto: validate`

Esto permite que Hibernate valide que el modelo de entidades coincide con el esquema de la base de datos, mientras que Flyway se encarga de crear y evolucionar dicho esquema.

---

## 6. Capa de servicio

En esta segunda parte se implementó la lógica de negocio de la aplicación mediante servicios con inyección por constructor y anotación `@Service`.

### RescueCaseService

Se implementaron las operaciones:

- Buscar un caso por código.
- Obtener casos por estado.
- Cambiar el estado de un caso.

Las transiciones de estado permitidas son:

`ADMITTED → UNDER_EVALUATION → IN_REHABILITATION → READY_FOR_RELEASE → RELEASED`

También se validan las reglas de negocio y se generan excepciones cuando el recurso no existe o la transición no es válida.

### TreatmentService

Se implementaron operaciones para registrar y consultar tratamientos.

Antes de crear un tratamiento se validan, entre otras, las siguientes reglas:

- El especialista debe estar activo.
- El especialista debe existir.
- El animal y el caso de rescate deben existir.
- El caso no puede encontrarse en estado `RELEASED` o `CLOSED`.
- La fecha del tratamiento (`performedAt`) no puede ser anterior a la fecha del rescate.

Las operaciones de consulta utilizan transacciones de solo lectura mediante `@Transactional(readOnly = true)` cuando corresponde.

### AnimalService

Se implementó el método:

`canReceiveTreatment(String animalCode)`

El método determina si un animal puede recibir tratamiento según el estado de su caso de rescate. Se permite tratamiento cuando el caso está en:

- `UNDER_EVALUATION`
- `IN_REHABILITATION`

Si el animal no existe o no tiene un caso de rescate asociado, se genera `ResourceNotFoundException`.

---

## 7. DTOs y MapStruct

Se implementaron DTOs para separar los objetos de entrada y salida de la capa de dominio.

### Request DTOs

- `ChangeRescueStatusRequest`
- `CreateTreatmentRequest`

### Response DTOs

- `RescueCaseResponse`
- `TreatmentResponse`

También se implementaron mappers con MapStruct:

- `RescueCaseMapper`
- `TreatmentMapper`

Estos mappers permiten convertir de manera tipada entre entidades y DTOs.

---

## 8. Manejo de excepciones

Se implementaron excepciones personalizadas para representar errores de la aplicación:

- `ResourceNotFoundException`: utilizada cuando un recurso requerido no existe.
- `BusinessRuleException`: utilizada cuando se viola una regla de negocio.

Estas excepciones son utilizadas desde los servicios para controlar escenarios como recursos inexistentes, transiciones inválidas y operaciones no permitidas.

---

## 9. Ejecución del proyecto

Para compilar el proyecto:

```powershell
.\mvnw.cmd clean compile
```

Para ejecutar la aplicación:

```powershell
.\mvnw.cmd spring-boot:run
```

La configuración de PostgreSQL se encuentra en:

`src/main/resources/application.yml`

---

## 10. Ejecución de pruebas

Todas las pruebas se ejecutan mediante:

```powershell
.\mvnw.cmd clean test
```

### Pruebas de integración

`PersistenceIntegrationTest` valida la persistencia, migraciones Flyway, relaciones, Query Methods, consultas JPQL y restricciones de base de datos utilizando PostgreSQL mediante Testcontainers.

### Pruebas unitarias

Se implementaron pruebas unitarias para:

- `AnimalServiceImplTest`
- `RescueCaseServiceImplTest`
- `TreatmentServiceImplTest`

Estas pruebas utilizan Mockito para aislar dependencias y AssertJ para las verificaciones.

Se prueban tanto escenarios exitosos como reglas de negocio y excepciones.

### Resultado actual

```text
Tests run: 46
Failures: 0
Errors: 0
Skipped: 0
BUILD SUCCESS
```

---

## 11. Testcontainers

Testcontainers permite ejecutar las pruebas de integración utilizando una instancia real de PostgreSQL dentro de un contenedor.

La clase de pruebas utiliza:

```java
@Testcontainers
@Container
@ServiceConnection
```

Esto permite conectar automáticamente Spring Boot con la base de datos utilizada durante las pruebas.

De esta manera, las pruebas se ejecutan sobre PostgreSQL real y permiten comprobar también el comportamiento de las restricciones de la base de datos.

---

## 12. Query Methods implementados

Se implementaron Query Methods mediante Spring Data JPA.

### RescueCenterRepository

`findByCode(String code)`

Permite buscar un centro mediante su código.

### RescueCaseRepository

`findByCaseCode(String caseCode)`  
`findByStatusOrderByRescueDateAsc(RescueStatus status)`  
`findByRescueCenter_Code(String code)`

Permiten buscar casos por código, estado y centro de rescate.

### AnimalRepository

`findByAnimalCode(String animalCode)`  
`findByCommonNameContainingIgnoreCase(String commonName)`  
`findByRescueCase_Status(RescueStatus status)`  
`findByRescueCase_RescueCenter_Code(String centerCode)`  
`findByRescueCase_RescueDateAfterOrderByRescueCase_RescueDateDesc(LocalDate date)`

Permiten realizar búsquedas de animales mediante diferentes propiedades y relaciones.

### ExpertiseRepository

`findByNameIgnoreCase(String name)`

Permite buscar un área de experiencia ignorando mayúsculas y minúsculas.

### TreatmentRepository

`findByAnimal_IdOrderByPerformedAtAsc(Long animalId)`

Permite obtener los tratamientos de un animal ordenados cronológicamente.

---

## 13. Consultas JPQL implementadas

Se utilizaron consultas `@Query` con JPQL cuando las consultas requerían navegar múltiples relaciones.

### SpecialistRepository

`findActiveByExpertiseNameIgnoreCase(String expertiseName)`

Obtiene especialistas activos que poseen una determinada área de experiencia.

Utiliza:

- `JOIN`
- `DISTINCT`
- `LOWER`
- Parámetros nombrados
- `ORDER BY`

### AnimalRepository

`findAnimalsInStatusWithTreatmentBySpecialistExpertise(...)`

Obtiene animales que se encuentran en un determinado estado y que han recibido tratamientos realizados por especialistas con una determinada área de experiencia.

Utiliza múltiples `JOIN` y `DISTINCT`.

### TreatmentRepository

`findByPerformedAtBetween(...)`

Obtiene tratamientos realizados dentro de un intervalo de fechas.

`findByRescueCenterCode(...)`

Obtiene tratamientos realizados a animales pertenecientes a un centro determinado, navegando desde `Treatment` hacia `Animal`, `RescueCase` y `RescueCenter`.

`findBySpecialistExpertise(...)`

Obtiene tratamientos realizados por especialistas que poseen una determinada área de experiencia.

---

## 14. Integridad de datos

El proyecto implementa restricciones de integridad directamente en PostgreSQL:

- Primary Keys (PK)
- Foreign Keys (FK)
- `UNIQUE`
- `NOT NULL`
- `CHECK`

Estas restricciones fueron verificadas mediante pruebas de integración.

Se probaron específicamente:

- Códigos de animales duplicados.
- Claves foráneas inválidas.
- Estados de rescate no permitidos.

---

## 15. Reto integrador

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

Y:

```text
Animal
   ↓
Treatment
   ↓
Specialist
   ↓
Expertise
```

El escenario utiliza:

- Centro: `DB-CAR`
- Caso: `RES-2026-100`
- Animal: `AN-2026-100`

También se implementaron consultas para:

- Buscar el caso por código.
- Obtener casos en rehabilitación.
- Obtener animales de un centro.
- Buscar animales por nombre común.
- Obtener especialistas con experiencia en Trauma.
- Obtener tratamientos de un animal.
- Obtener tratamientos por experiencia del especialista.
- Obtener tratamientos dentro de un intervalo de fechas.
- Obtener animales en rehabilitación tratados por especialistas con experiencia en Trauma.

La nueva capa de servicio permite además aplicar las reglas de negocio sobre los casos y tratamientos antes de ejecutar las operaciones correspondientes.

---

## 16. Estructura de la segunda parte

Se agregaron principalmente los siguientes componentes:

```text
src/main/java/com/deepblue/rescue/
├── dto/
│   ├── request/
│   │   ├── ChangeRescueStatusRequest.java
│   │   └── CreateTreatmentRequest.java
│   └── response/
│       ├── RescueCaseResponse.java
│       └── TreatmentResponse.java
├── exception/
│   ├── BusinessRuleException.java
│   └── ResourceNotFoundException.java
├── mapper/
│   ├── RescueCaseMapper.java
│   └── TreatmentMapper.java
└── service/
    ├── AnimalService.java
    ├── RescueCaseService.java
    ├── TreatmentService.java
    └── impl/
        ├── AnimalServiceImpl.java
        ├── RescueCaseServiceImpl.java
        └── TreatmentServiceImpl.java
```

Las pruebas unitarias correspondientes se encuentran en:

```text
src/test/java/com/deepblue/rescue/service/impl/
├── AnimalServiceImplTest.java
├── RescueCaseServiceImplTest.java
└── TreatmentServiceImplTest.java
```

---

## 17. Resultado final

El proyecto cuenta con pruebas para validar:

- Migraciones Flyway.
- Métodos heredados de `JpaRepository`.
- Relaciones 1:N.
- Relaciones 1:1.
- Relaciones N:M.
- Query Methods.
- Consultas JPQL.
- Restricciones `UNIQUE`.
- Restricciones FK.
- Restricciones `CHECK`.
- Evolución del esquema mediante Flyway.
- Reto integrador.
- Lógica de negocio de la capa de servicio.
- Transiciones de estado de los casos de rescate.
- Validación de especialistas activos.
- Validación del estado del caso antes de registrar tratamientos.
- Validación de fechas de tratamiento.
- Manejo de `ResourceNotFoundException`.
- Manejo de `BusinessRuleException`.
- Mapeo entre entidades y DTOs mediante MapStruct.
- Pruebas unitarias con Mockito y AssertJ.

### Resultado de las pruebas

```text
Tests run: 46
Failures: 0
Errors: 0
Skipped: 0

BUILD SUCCESS
```

## Repositorio

El proyecto completo se encuentra disponible en:

https://github.com/Donteo29/deepblue-rescue
