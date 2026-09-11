package com.deepblue.rescue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.springframework.dao.DataIntegrityViolationException;
import com.deepblue.rescue.domain.Specialist;
import com.deepblue.rescue.domain.Treatment;
import com.deepblue.rescue.domain.Expertise;
import com.deepblue.rescue.domain.Specialist;
import com.deepblue.rescue.domain.MedicalRecord;
import com.deepblue.rescue.domain.Animal;
import com.deepblue.rescue.domain.AnimalSex;
import com.deepblue.rescue.domain.RescueCase;
import com.deepblue.rescue.domain.RescueCenter;
import com.deepblue.rescue.domain.RescueStatus;
import com.deepblue.rescue.repository.AnimalRepository;
import com.deepblue.rescue.repository.ExpertiseRepository;
import com.deepblue.rescue.repository.MedicalRecordRepository;
import com.deepblue.rescue.repository.RescueCaseRepository;
import com.deepblue.rescue.repository.RescueCenterRepository;
import com.deepblue.rescue.repository.SpecialistRepository;
import com.deepblue.rescue.repository.TreatmentRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Testcontainers
@SpringBootTest
@Transactional
class PersistenceIntegrationTest {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer postgres =
            new PostgreSQLContainer("postgres:18-alpine")
                    .withDatabaseName("deepblue_test")
                    .withUsername("deepblue")
                    .withPassword("deepblue");

    @Autowired
    RescueCenterRepository rescueCenterRepository;

    @Autowired
    RescueCaseRepository rescueCaseRepository;

    @Autowired
    AnimalRepository animalRepository;

    @Autowired
    MedicalRecordRepository medicalRecordRepository;

    @Autowired
    SpecialistRepository specialistRepository;

    @Autowired
    ExpertiseRepository expertiseRepository;

    @Autowired
    TreatmentRepository treatmentRepository;

    @Autowired
    JdbcTemplate jdbcTemplate;


    // =========================================================
    // PASO 47 - Verificar que el contexto de Spring carga
    // =========================================================

    @Test
    void contextLoads() {
    }


    // =========================================================
    // PASO 47 - Verificar que Flyway ejecutó V1 y V2
    // =========================================================

    @Test
    void flywayShouldHaveExecutedV1AndV2() {

        List<String> versions = jdbcTemplate.queryForList(
                """
                SELECT version
                FROM flyway_schema_history
                WHERE version IN ('1', '2')
                ORDER BY installed_rank
                """,
                String.class
        );

        assertTrue(versions.contains("1"));
        assertTrue(versions.contains("2"));
    }


    // =========================================================
    // PASO 48 - Métodos heredados de JpaRepository
    // =========================================================

    @Test
    void shouldUseInheritedJpaRepositoryMethods() {

        RescueCenter center = new RescueCenter();

        center.setCode("DB-CAR");
        center.setName("DeepBlue Caribbean Center");
        center.setCity("Santa Marta");

        RescueCenter savedCenter =
                rescueCenterRepository.save(center);

        assertTrue(savedCenter.getId() != null);

        assertTrue(
                rescueCenterRepository
                        .findById(savedCenter.getId())
                        .isPresent()
        );

        assertTrue(
                rescueCenterRepository
                        .existsById(savedCenter.getId())
        );

        assertTrue(
                rescueCenterRepository.count() >= 1
        );
    }


    // =========================================================
    // PASO 49 - RescueCenter 1:N RescueCase
    // =========================================================

    @Test
    void shouldPersistRescueCenterWithTwoRescueCases() {

        RescueCenter center = new RescueCenter();

        center.setCode("DB-CAR-2");
        center.setName("DeepBlue Caribbean Center 2");
        center.setCity("Santa Marta");


        RescueCase rescueCase1 = new RescueCase();

        rescueCase1.setCaseCode("RES-2026-101");
        rescueCase1.setRescueDate(
                LocalDate.of(2026, 9, 1)
        );
        rescueCase1.setRescueLocation(
                "Bahía de Santa Marta"
        );
        rescueCase1.setStatus(
                RescueStatus.ADMITTED
        );


        RescueCase rescueCase2 = new RescueCase();

        rescueCase2.setCaseCode("RES-2026-102");
        rescueCase2.setRescueDate(
                LocalDate.of(2026, 9, 2)
        );
        rescueCase2.setRescueLocation(
                "Playa Grande"
        );
        rescueCase2.setStatus(
                RescueStatus.UNDER_EVALUATION
        );


        center.addCase(rescueCase1);
        center.addCase(rescueCase2);

        rescueCenterRepository.save(center);

        assertTrue(center.getId() != null);

        assertTrue(rescueCase1.getId() == null);
        assertTrue(rescueCase2.getId() == null);

        rescueCaseRepository.save(rescueCase1);
        rescueCaseRepository.save(rescueCase2);

        assertTrue(rescueCase1.getId() != null);
        assertTrue(rescueCase2.getId() != null);

        assertEquals(
                center.getId(),
                rescueCase1.getRescueCenter().getId()
        );

        assertEquals(
                center.getId(),
                rescueCase2.getRescueCenter().getId()
        );
    }



    // =========================================================
// PASO 50 - RescueCase 1:1 Animal
// =========================================================

    @Test
    void shouldPersistRescueCaseWithAnimal() {

        // Crear RescueCenter
        RescueCenter center = new RescueCenter();

        center.setCode("DB-CAR-50");
        center.setName("DeepBlue Caribbean Center 50");
        center.setCity("Santa Marta");

        rescueCenterRepository.save(center);


        // Crear RescueCase
        RescueCase rescueCase = new RescueCase();

        rescueCase.setCaseCode("RES-2026-001");
        rescueCase.setRescueDate(
                LocalDate.of(2026, 9, 3)
        );
        rescueCase.setRescueLocation(
                "Playa Grande"
        );
        rescueCase.setStatus(
                RescueStatus.ADMITTED
        );

        // Asociar el RescueCase con el RescueCenter
        center.addCase(rescueCase);


        // Crear Animal
        Animal animal = new Animal();

        animal.setAnimalCode("AN-2026-001");
        animal.setCommonName("Green Sea Turtle");
        animal.setScientificName("Chelonia mydas");
        animal.setSex(AnimalSex.UNKNOWN);


        // Asociar Animal al RescueCase
        rescueCase.assignAnimal(animal);


        // Guardar RescueCase
        // El cascade ALL de RescueCase -> Animal
        // permite guardar también el Animal
        rescueCaseRepository.save(rescueCase);


        // Verificar que RescueCase fue guardado
        assertTrue(rescueCase.getId() != null);


        // Verificar que Animal fue guardado por cascade
        assertTrue(animal.getId() != null);


        // Verificar la relación RescueCase -> Animal
        assertEquals(
                animal.getId(),
                rescueCase.getAnimal().getId()
        );


        // Verificar la relación Animal -> RescueCase
        assertEquals(
                rescueCase.getId(),
                animal.getRescueCase().getId()
        );
    }
    // =========================================================
// PASO 51 - Animal 1:1 MedicalRecord
// =========================================================

    @Test
    void shouldPersistAnimalWithMedicalRecord() {

        // Crear RescueCenter
        RescueCenter center = new RescueCenter();

        center.setCode("DB-CAR-51");
        center.setName("DeepBlue Caribbean Center 51");
        center.setCity("Santa Marta");

        rescueCenterRepository.save(center);


        // Crear RescueCase
        RescueCase rescueCase = new RescueCase();

        rescueCase.setCaseCode("RES-2026-002");
        rescueCase.setRescueDate(
                LocalDate.of(2026, 9, 4)
        );
        rescueCase.setRescueLocation(
                "Bahía de Santa Marta"
        );
        rescueCase.setStatus(
                RescueStatus.ADMITTED
        );


        // Asociar RescueCase con RescueCenter
        center.addCase(rescueCase);


        // Crear Animal
        Animal animal = new Animal();

        animal.setAnimalCode("AN-2026-002");
        animal.setCommonName("Green Sea Turtle");
        animal.setScientificName("Chelonia mydas");
        animal.setSex(AnimalSex.UNKNOWN);


        // Asociar Animal al RescueCase
        rescueCase.assignAnimal(animal);


        // Crear MedicalRecord
        MedicalRecord medicalRecord = new MedicalRecord();

        medicalRecord.setInitialWeight(
                new java.math.BigDecimal("28.40")
        );

        medicalRecord.setInitialCondition(
                "STABLE"
        );

        medicalRecord.setInjuries(
                "Left front flipper injury"
        );


        // Asociar MedicalRecord al Animal
        animal.assignMedicalRecord(medicalRecord);


        // Guardar RescueCase
        // Cascade:
        // RescueCase -> Animal -> MedicalRecord
        rescueCaseRepository.save(rescueCase);


        // Verificar que Animal fue guardado
        assertTrue(animal.getId() != null);


        // Verificar que MedicalRecord fue guardado
        assertTrue(medicalRecord.getId() != null);


        // Verificar relación Animal -> MedicalRecord
        assertEquals(
                medicalRecord.getId(),
                animal.getMedicalRecord().getId()
        );


        // Verificar relación MedicalRecord -> Animal
        assertEquals(
                animal.getId(),
                medicalRecord.getAnimal().getId()
        );
    }
    // =========================================================
// PASO 52 - Specialist N:M Expertise
// =========================================================

    @Test
    void shouldPersistSpecialistWithTwoExpertiseAreas() {

        // Buscar las especialidades creadas por Flyway V2
        Expertise trauma = expertiseRepository
                .findByNameIgnoreCase("Trauma")
                .orElseThrow();

        Expertise rehabilitation = expertiseRepository
                .findByNameIgnoreCase("Rehabilitation")
                .orElseThrow();


        // Crear Specialist
        Specialist specialist = new Specialist();

        specialist.setProfessionalCode("SP-2026-001");
        specialist.setFirstName("Elena");
        specialist.setLastName("Vargas");
        specialist.setEmail("elena.vargas@deepblue.com");
        specialist.setActive(true);


        // Asociar las dos especialidades
        specialist.getExpertiseAreas().add(trauma);
        specialist.getExpertiseAreas().add(rehabilitation);


        // Guardar Specialist
        specialistRepository.save(specialist);


        // Verificar que Specialist fue guardado
        assertTrue(specialist.getId() != null);


        // Verificar que tiene las dos especialidades
        assertEquals(
                2,
                specialist.getExpertiseAreas().size()
        );


        // Verificar que las especialidades son las correctas
        assertTrue(
                specialist.getExpertiseAreas()
                        .stream()
                        .anyMatch(e -> e.getName().equals("Trauma"))
        );

        assertTrue(
                specialist.getExpertiseAreas()
                        .stream()
                        .anyMatch(e -> e.getName().equals("Rehabilitation"))
        );
    }
    // =========================================================
// PASO 53 - Query Method: findByCode()
// =========================================================

    @Test
    void shouldFindRescueCenterByCode() {

        // Crear RescueCenter
        RescueCenter center = new RescueCenter();

        center.setCode("DB-QUERY-01");
        center.setName("DeepBlue Query Center");
        center.setCity("Santa Marta");

        rescueCenterRepository.save(center);


        // Buscar utilizando Query Method
        RescueCenter foundCenter = rescueCenterRepository
                .findByCode("DB-QUERY-01")
                .orElseThrow();


        // Verificar que encontró el centro correcto
        assertEquals(
                "DB-QUERY-01",
                foundCenter.getCode()
        );

        assertEquals(
                "DeepBlue Query Center",
                foundCenter.getName()
        );

        assertEquals(
                "Santa Marta",
                foundCenter.getCity()
        );
    }
    @Test
    void shouldFindRescueCaseByCaseCode() {
        RescueCenter center = new RescueCenter();
        center.setCode("DB-QUERY-02");
        center.setName("DeepBlue Query Center 2");
        center.setCity("Santa Marta");

        rescueCenterRepository.save(center);

        RescueCase rescueCase = new RescueCase();
        rescueCase.setCaseCode("RES-QUERY-001");
        rescueCase.setRescueDate(LocalDate.of(2026, 9, 5));
        rescueCase.setRescueLocation("Playa Grande");
        rescueCase.setStatus(RescueStatus.ADMITTED);

        center.addCase(rescueCase);

        rescueCaseRepository.save(rescueCase);

        RescueCase foundCase = rescueCaseRepository
                .findByCaseCode("RES-QUERY-001")
                .orElseThrow();

        assertEquals("RES-QUERY-001", foundCase.getCaseCode());
        assertEquals(RescueStatus.ADMITTED, foundCase.getStatus());
        assertEquals("Playa Grande", foundCase.getRescueLocation());
    }
    @Test
    void shouldFindRescueCasesByStatusOrderedByDate() {
        RescueCenter center = new RescueCenter();
        center.setCode("DB-QUERY-03");
        center.setName("DeepBlue Query Center 3");
        center.setCity("Santa Marta");

        rescueCenterRepository.save(center);

        RescueCase rescueCase1 = new RescueCase();
        rescueCase1.setCaseCode("RES-QUERY-003");
        rescueCase1.setRescueDate(LocalDate.of(2026, 9, 10));
        rescueCase1.setRescueLocation("Playa Grande");
        rescueCase1.setStatus(RescueStatus.IN_REHABILITATION);

        RescueCase rescueCase2 = new RescueCase();
        rescueCase2.setCaseCode("RES-QUERY-002");
        rescueCase2.setRescueDate(LocalDate.of(2026, 9, 6));
        rescueCase2.setRescueLocation("Bahía de Santa Marta");
        rescueCase2.setStatus(RescueStatus.IN_REHABILITATION);

        RescueCase rescueCase3 = new RescueCase();
        rescueCase3.setCaseCode("RES-QUERY-004");
        rescueCase3.setRescueDate(LocalDate.of(2026, 9, 8));
        rescueCase3.setRescueLocation("Taganga");
        rescueCase3.setStatus(RescueStatus.ADMITTED);

        center.addCase(rescueCase1);
        center.addCase(rescueCase2);
        center.addCase(rescueCase3);

        rescueCaseRepository.saveAll(
                List.of(rescueCase1, rescueCase2, rescueCase3)
        );

        List<RescueCase> cases =
                rescueCaseRepository.findByStatusOrderByRescueDateAsc(
                        RescueStatus.IN_REHABILITATION
                );

        assertEquals(2, cases.size());
        assertEquals("RES-QUERY-002", cases.get(0).getCaseCode());
        assertEquals("RES-QUERY-003", cases.get(1).getCaseCode());
    }
    @Test
    void shouldFindRescueCasesByRescueCenterCode() {
        RescueCenter center = new RescueCenter();
        center.setCode("DB-QUERY-04");
        center.setName("DeepBlue Query Center 4");
        center.setCity("Santa Marta");

        rescueCenterRepository.save(center);

        RescueCase rescueCase1 = new RescueCase();
        rescueCase1.setCaseCode("RES-QUERY-005");
        rescueCase1.setRescueDate(LocalDate.of(2026, 9, 11));
        rescueCase1.setRescueLocation("Taganga");
        rescueCase1.setStatus(RescueStatus.ADMITTED);

        RescueCase rescueCase2 = new RescueCase();
        rescueCase2.setCaseCode("RES-QUERY-006");
        rescueCase2.setRescueDate(LocalDate.of(2026, 9, 12));
        rescueCase2.setRescueLocation("Playa Grande");
        rescueCase2.setStatus(RescueStatus.UNDER_EVALUATION);

        center.addCase(rescueCase1);
        center.addCase(rescueCase2);

        rescueCaseRepository.saveAll(
                List.of(rescueCase1, rescueCase2)
        );

        List<RescueCase> cases =
                rescueCaseRepository.findByRescueCenter_Code("DB-QUERY-04");

        assertEquals(2, cases.size());
        assertEquals("RES-QUERY-005", cases.get(0).getCaseCode());
        assertEquals("RES-QUERY-006", cases.get(1).getCaseCode());
    }
    @Test
    void shouldFindAnimalsByRescueCaseStatus() {
        RescueCenter center = new RescueCenter();
        center.setCode("DB-QUERY-05");
        center.setName("DeepBlue Query Center 5");
        center.setCity("Santa Marta");

        rescueCenterRepository.save(center);

        RescueCase rescueCase = new RescueCase();
        rescueCase.setCaseCode("RES-QUERY-007");
        rescueCase.setRescueDate(LocalDate.of(2026, 9, 13));
        rescueCase.setRescueLocation("Taganga");
        rescueCase.setStatus(RescueStatus.IN_REHABILITATION);

        center.addCase(rescueCase);

        Animal animal = new Animal();
        animal.setAnimalCode("AN-QUERY-001");
        animal.setCommonName("Green Sea Turtle");
        animal.setScientificName("Chelonia mydas");
        animal.setSex(AnimalSex.UNKNOWN);

        rescueCase.assignAnimal(animal);

        rescueCaseRepository.save(rescueCase);

        List<Animal> animals =
                animalRepository.findByRescueCase_Status(
                        RescueStatus.IN_REHABILITATION
                );

        assertEquals(1, animals.size());
        assertEquals("AN-QUERY-001", animals.get(0).getAnimalCode());
        assertEquals(
                RescueStatus.IN_REHABILITATION,
                animals.get(0).getRescueCase().getStatus()
        );
    }
    @Test
    void shouldFindAnimalsByRescueCenterCode() {
        RescueCenter center = new RescueCenter();
        center.setCode("DB-QUERY-06");
        center.setName("DeepBlue Query Center 6");
        center.setCity("Santa Marta");

        rescueCenterRepository.save(center);

        RescueCase rescueCase1 = new RescueCase();
        rescueCase1.setCaseCode("RES-QUERY-008");
        rescueCase1.setRescueDate(LocalDate.of(2026, 9, 14));
        rescueCase1.setRescueLocation("Taganga");
        rescueCase1.setStatus(RescueStatus.ADMITTED);

        RescueCase rescueCase2 = new RescueCase();
        rescueCase2.setCaseCode("RES-QUERY-009");
        rescueCase2.setRescueDate(LocalDate.of(2026, 9, 15));
        rescueCase2.setRescueLocation("Playa Grande");
        rescueCase2.setStatus(RescueStatus.IN_REHABILITATION);

        center.addCase(rescueCase1);
        center.addCase(rescueCase2);

        Animal animal1 = new Animal();
        animal1.setAnimalCode("AN-QUERY-002");
        animal1.setCommonName("Green Sea Turtle");
        animal1.setScientificName("Chelonia mydas");
        animal1.setSex(AnimalSex.UNKNOWN);

        Animal animal2 = new Animal();
        animal2.setAnimalCode("AN-QUERY-003");
        animal2.setCommonName("Brown Pelican");
        animal2.setScientificName("Pelecanus occidentalis");
        animal2.setSex(AnimalSex.UNKNOWN);

        rescueCase1.assignAnimal(animal1);
        rescueCase2.assignAnimal(animal2);

        rescueCaseRepository.saveAll(
                List.of(rescueCase1, rescueCase2)
        );

        List<Animal> animals =
                animalRepository.findByRescueCase_RescueCenter_Code(
                        "DB-QUERY-06"
                );

        assertEquals(2, animals.size());
        assertTrue(animals.stream()
                .anyMatch(a -> a.getAnimalCode().equals("AN-QUERY-002")));
        assertTrue(animals.stream()
                .anyMatch(a -> a.getAnimalCode().equals("AN-QUERY-003")));
    }
    @Test
    void shouldFindExpertiseByNameIgnoreCase() {
        Expertise expertise = expertiseRepository
                .findByNameIgnoreCase("trauma")
                .orElseThrow();

        assertEquals("Trauma", expertise.getName());
    }
    @Test
    void shouldFindActiveSpecialistsByExpertise() {
        Expertise trauma = expertiseRepository
                .findByNameIgnoreCase("Trauma")
                .orElseThrow();

        Specialist specialist = new Specialist();
        specialist.setProfessionalCode("SP-QUERY-001");
        specialist.setFirstName("Carlos");
        specialist.setLastName("Mendoza");
        specialist.setEmail("carlos.mendoza@deepblue.com");
        specialist.setActive(true);

        specialist.getExpertiseAreas().add(trauma);

        specialistRepository.save(specialist);

        List<Specialist> specialists =
                specialistRepository.findActiveByExpertiseNameIgnoreCase(
                        "trauma"
                );

        assertEquals(1, specialists.size());
        assertEquals("Carlos", specialists.get(0).getFirstName());
        assertEquals("Mendoza", specialists.get(0).getLastName());
        assertTrue(specialists.get(0).isActive());
    }
    @Test
    void shouldFindTreatmentsByAnimalOrderedByDate() {
        RescueCenter center = new RescueCenter();
        center.setCode("DB-QUERY-07");
        center.setName("DeepBlue Query Center 7");
        center.setCity("Santa Marta");

        rescueCenterRepository.save(center);

        RescueCase rescueCase = new RescueCase();
        rescueCase.setCaseCode("RES-QUERY-010");
        rescueCase.setRescueDate(LocalDate.of(2026, 9, 16));
        rescueCase.setRescueLocation("Taganga");
        rescueCase.setStatus(RescueStatus.IN_REHABILITATION);

        center.addCase(rescueCase);

        Animal animal = new Animal();
        animal.setAnimalCode("AN-QUERY-004");
        animal.setCommonName("Green Sea Turtle");
        animal.setScientificName("Chelonia mydas");
        animal.setSex(AnimalSex.UNKNOWN);

        rescueCase.assignAnimal(animal);

        Specialist specialist = new Specialist();
        specialist.setProfessionalCode("SP-QUERY-002");
        specialist.setFirstName("Laura");
        specialist.setLastName("Gómez");
        specialist.setEmail("laura.gomez@deepblue.com");
        specialist.setActive(true);

        specialistRepository.save(specialist);

        Treatment treatment1 = new Treatment();
        treatment1.setAnimal(animal);
        treatment1.setSpecialist(specialist);
        treatment1.setPerformedAt(
                java.time.LocalDateTime.of(2026, 9, 16, 10, 0)
        );
        treatment1.setType(
                com.deepblue.rescue.domain.TreatmentType.WOUND_CARE
        );
        treatment1.setDescription("Limpieza de herida");

        Treatment treatment2 = new Treatment();
        treatment2.setAnimal(animal);
        treatment2.setSpecialist(specialist);
        treatment2.setPerformedAt(
                java.time.LocalDateTime.of(2026, 9, 16, 8, 0)
        );
        treatment2.setType(
                com.deepblue.rescue.domain.TreatmentType.OBSERVATION
        );
        treatment2.setDescription("Evaluación inicial");

        rescueCaseRepository.save(rescueCase);

        treatmentRepository.saveAll(
                List.of(treatment1, treatment2)
        );

        List<Treatment> treatments =
                treatmentRepository.findByAnimal_IdOrderByPerformedAtAsc(
                        animal.getId()
                );

        assertEquals(2, treatments.size());
        assertEquals(
                java.time.LocalDateTime.of(2026, 9, 16, 8, 0),
                treatments.get(0).getPerformedAt()
        );
        assertEquals(
                java.time.LocalDateTime.of(2026, 9, 16, 10, 0),
                treatments.get(1).getPerformedAt()
        );
    }
    @Test
    void shouldFindTreatmentsByDateInterval() {
        RescueCenter center = new RescueCenter();
        center.setCode("DB-QUERY-08");
        center.setName("DeepBlue Query Center 8");
        center.setCity("Santa Marta");

        rescueCenterRepository.save(center);

        RescueCase rescueCase = new RescueCase();
        rescueCase.setCaseCode("RES-QUERY-011");
        rescueCase.setRescueDate(LocalDate.of(2026, 9, 17));
        rescueCase.setRescueLocation("Taganga");
        rescueCase.setStatus(RescueStatus.IN_REHABILITATION);

        center.addCase(rescueCase);

        Animal animal = new Animal();
        animal.setAnimalCode("AN-QUERY-005");
        animal.setCommonName("Green Sea Turtle");
        animal.setScientificName("Chelonia mydas");
        animal.setSex(AnimalSex.UNKNOWN);

        rescueCase.assignAnimal(animal);

        Specialist specialist = new Specialist();
        specialist.setProfessionalCode("SP-QUERY-003");
        specialist.setFirstName("Andrés");
        specialist.setLastName("Pérez");
        specialist.setEmail("andres.perez@deepblue.com");
        specialist.setActive(true);

        specialistRepository.save(specialist);
        rescueCaseRepository.save(rescueCase);

        Treatment treatment1 = new Treatment();
        treatment1.setAnimal(animal);
        treatment1.setSpecialist(specialist);
        treatment1.setPerformedAt(
                java.time.LocalDateTime.of(2026, 9, 17, 9, 0)
        );
        treatment1.setType(
                com.deepblue.rescue.domain.TreatmentType.HYDRATION
        );
        treatment1.setDescription("Hidratación");

        Treatment treatment2 = new Treatment();
        treatment2.setAnimal(animal);
        treatment2.setSpecialist(specialist);
        treatment2.setPerformedAt(
                java.time.LocalDateTime.of(2026, 9, 18, 10, 0)
        );
        treatment2.setType(
                com.deepblue.rescue.domain.TreatmentType.NUTRITION
        );
        treatment2.setDescription("Control nutricional");

        Treatment treatmentOutside = new Treatment();
        treatmentOutside.setAnimal(animal);
        treatmentOutside.setSpecialist(specialist);
        treatmentOutside.setPerformedAt(
                java.time.LocalDateTime.of(2026, 9, 20, 10, 0)
        );
        treatmentOutside.setType(
                com.deepblue.rescue.domain.TreatmentType.OBSERVATION
        );
        treatmentOutside.setDescription("Observación posterior");

        treatmentRepository.saveAll(
                List.of(treatment1, treatment2, treatmentOutside)
        );

        List<Treatment> treatments =
                treatmentRepository.findByPerformedAtBetween(
                        java.time.LocalDateTime.of(2026, 9, 17, 0, 0),
                        java.time.LocalDateTime.of(2026, 9, 18, 23, 59)
                );

        assertEquals(2, treatments.size());

        assertEquals(
                java.time.LocalDateTime.of(2026, 9, 17, 9, 0),
                treatments.get(0).getPerformedAt()
        );

        assertEquals(
                java.time.LocalDateTime.of(2026, 9, 18, 10, 0),
                treatments.get(1).getPerformedAt()
        );
    }
    @Test
    void shouldFindTreatmentsByRescueCenterCode() {
        RescueCenter center = new RescueCenter();
        center.setCode("DB-QUERY-09");
        center.setName("DeepBlue Query Center 9");
        center.setCity("Santa Marta");

        rescueCenterRepository.save(center);

        RescueCase rescueCase = new RescueCase();
        rescueCase.setCaseCode("RES-QUERY-012");
        rescueCase.setRescueDate(LocalDate.of(2026, 9, 19));
        rescueCase.setRescueLocation("Taganga");
        rescueCase.setStatus(RescueStatus.IN_REHABILITATION);

        center.addCase(rescueCase);

        Animal animal = new Animal();
        animal.setAnimalCode("AN-QUERY-006");
        animal.setCommonName("Green Sea Turtle");
        animal.setScientificName("Chelonia mydas");
        animal.setSex(AnimalSex.UNKNOWN);

        rescueCase.assignAnimal(animal);

        Specialist specialist = new Specialist();
        specialist.setProfessionalCode("SP-QUERY-004");
        specialist.setFirstName("María");
        specialist.setLastName("Rodríguez");
        specialist.setEmail("maria.rodriguez@deepblue.com");
        specialist.setActive(true);

        specialistRepository.save(specialist);
        rescueCaseRepository.save(rescueCase);

        Treatment treatment1 = new Treatment();
        treatment1.setAnimal(animal);
        treatment1.setSpecialist(specialist);
        treatment1.setPerformedAt(
                java.time.LocalDateTime.of(2026, 9, 19, 9, 0)
        );
        treatment1.setType(
                com.deepblue.rescue.domain.TreatmentType.WOUND_CARE
        );
        treatment1.setDescription("Limpieza de herida");

        Treatment treatment2 = new Treatment();
        treatment2.setAnimal(animal);
        treatment2.setSpecialist(specialist);
        treatment2.setPerformedAt(
                java.time.LocalDateTime.of(2026, 9, 19, 11, 0)
        );
        treatment2.setType(
                com.deepblue.rescue.domain.TreatmentType.HYDRATION
        );
        treatment2.setDescription("Hidratación");

        treatmentRepository.saveAll(
                List.of(treatment1, treatment2)
        );

        List<Treatment> treatments =
                treatmentRepository.findByRescueCenterCode(
                        "DB-QUERY-09"
                );

        assertEquals(2, treatments.size());

        assertEquals(
                java.time.LocalDateTime.of(2026, 9, 19, 9, 0),
                treatments.get(0).getPerformedAt()
        );

        assertEquals(
                java.time.LocalDateTime.of(2026, 9, 19, 11, 0),
                treatments.get(1).getPerformedAt()
        );
    }
    @Test
    void shouldFindTreatmentsBySpecialistExpertise() {
        Expertise trauma = expertiseRepository
                .findByNameIgnoreCase("Trauma")
                .orElseThrow();

        RescueCenter center = new RescueCenter();
        center.setCode("DB-QUERY-10");
        center.setName("DeepBlue Query Center 10");
        center.setCity("Santa Marta");

        rescueCenterRepository.save(center);

        RescueCase rescueCase = new RescueCase();
        rescueCase.setCaseCode("RES-QUERY-013");
        rescueCase.setRescueDate(LocalDate.of(2026, 9, 20));
        rescueCase.setRescueLocation("Taganga");
        rescueCase.setStatus(RescueStatus.IN_REHABILITATION);

        center.addCase(rescueCase);

        Animal animal = new Animal();
        animal.setAnimalCode("AN-QUERY-007");
        animal.setCommonName("Green Sea Turtle");
        animal.setScientificName("Chelonia mydas");
        animal.setSex(AnimalSex.UNKNOWN);

        rescueCase.assignAnimal(animal);

        Specialist specialist = new Specialist();
        specialist.setProfessionalCode("SP-QUERY-005");
        specialist.setFirstName("Juan");
        specialist.setLastName("Martínez");
        specialist.setEmail("juan.martinez@deepblue.com");
        specialist.setActive(true);

        specialist.getExpertiseAreas().add(trauma);

        specialistRepository.save(specialist);
        rescueCaseRepository.save(rescueCase);

        Treatment treatment = new Treatment();
        treatment.setAnimal(animal);
        treatment.setSpecialist(specialist);
        treatment.setPerformedAt(
                java.time.LocalDateTime.of(2026, 9, 20, 10, 0)
        );
        treatment.setType(
                com.deepblue.rescue.domain.TreatmentType.WOUND_CARE
        );
        treatment.setDescription("Tratamiento de trauma");

        treatmentRepository.save(treatment);

        List<Treatment> treatments =
                treatmentRepository.findBySpecialistExpertise("trauma");

        assertEquals(1, treatments.size());
        assertEquals(
                "Tratamiento de trauma",
                treatments.get(0).getDescription()
        );
        assertEquals(
                "Juan",
                treatments.get(0).getSpecialist().getFirstName()
        );
    }
    @Test
    void shouldRejectDuplicateAnimalCode() {
        RescueCenter center = new RescueCenter();
        center.setCode("DB-CONSTRAINT-01");
        center.setName("DeepBlue Constraint Center");
        center.setCity("Santa Marta");

        rescueCenterRepository.save(center);

        RescueCase rescueCase1 = new RescueCase();
        rescueCase1.setCaseCode("RES-CONSTRAINT-001");
        rescueCase1.setRescueDate(LocalDate.of(2026, 9, 21));
        rescueCase1.setRescueLocation("Taganga");
        rescueCase1.setStatus(RescueStatus.ADMITTED);

        RescueCase rescueCase2 = new RescueCase();
        rescueCase2.setCaseCode("RES-CONSTRAINT-002");
        rescueCase2.setRescueDate(LocalDate.of(2026, 9, 22));
        rescueCase2.setRescueLocation("Playa Grande");
        rescueCase2.setStatus(RescueStatus.ADMITTED);

        center.addCase(rescueCase1);
        center.addCase(rescueCase2);

        rescueCaseRepository.saveAll(
                List.of(rescueCase1, rescueCase2)
        );

        Animal animal1 = new Animal();
        animal1.setAnimalCode("AN-100");
        animal1.setCommonName("Green Sea Turtle");
        animal1.setScientificName("Chelonia mydas");
        animal1.setSex(AnimalSex.UNKNOWN);
        rescueCase1.assignAnimal(animal1);

        animalRepository.saveAndFlush(animal1);

        Animal animal2 = new Animal();
        animal2.setAnimalCode("AN-100");
        animal2.setCommonName("Brown Pelican");
        animal2.setScientificName("Pelecanus occidentalis");
        animal2.setSex(AnimalSex.UNKNOWN);
        rescueCase2.assignAnimal(animal2);

        org.junit.jupiter.api.Assertions.assertThrows(
                DataIntegrityViolationException.class,
                () -> animalRepository.saveAndFlush(animal2)
        );
    }
    @Test
    void shouldRejectRescueCaseWithInvalidRescueCenter() {
        assertThrows(
                DataIntegrityViolationException.class,
                () -> jdbcTemplate.update("""
                    INSERT INTO rescue_cases
                    (case_code, rescue_date, rescue_location, status, rescue_center_id)
                    VALUES (?, ?, ?, ?, ?)
                    """,
                        "RES-FK-001",
                        java.sql.Date.valueOf(LocalDate.of(2026, 9, 23)),
                        "Taganga",
                        "ADMITTED",
                        999999L
                )
        );
    }
    @Test
    void shouldRejectInvalidRescueStatus() {
        RescueCenter center = new RescueCenter();
        center.setCode("DB-CHECK-01");
        center.setName("DeepBlue Check Center");
        center.setCity("Santa Marta");

        rescueCenterRepository.saveAndFlush(center);

        assertThrows(
                DataIntegrityViolationException.class,
                () -> jdbcTemplate.update("""
                    INSERT INTO rescue_cases
                    (case_code, rescue_date, rescue_location, status, rescue_center_id)
                    VALUES (?, ?, ?, ?, ?)
                    """,
                        "RES-CHECK-001",
                        java.sql.Date.valueOf(LocalDate.of(2026, 9, 24)),
                        "Taganga",
                        "INVALID_STATUS",
                        center.getId()
                )
        );
    }
    @Test
    void shouldPersistIntegratorScenario() {
        // =========================
        // 1. RescueCenter
        // =========================
        RescueCenter center = new RescueCenter();
        center.setCode("DB-CAR");
        center.setName("DeepBlue Caribbean");
        center.setCity("Santa Marta");

        rescueCenterRepository.save(center);

        // =========================
        // 2. RescueCase
        // =========================
        RescueCase rescueCase = new RescueCase();
        rescueCase.setCaseCode("RES-2026-100");
        rescueCase.setRescueDate(LocalDate.of(2026, 8, 18));
        rescueCase.setRescueLocation("Bahía Concha");
        rescueCase.setStatus(RescueStatus.IN_REHABILITATION);

        center.addCase(rescueCase);

        // =========================
        // 3. Animal
        // =========================
        Animal animal = new Animal();
        animal.setAnimalCode("AN-2026-100");
        animal.setCommonName("Green Sea Turtle");
        animal.setScientificName("Chelonia mydas");
        animal.setSex(AnimalSex.FEMALE);

        rescueCase.assignAnimal(animal);

        // =========================
        // 4. MedicalRecord
        // =========================
        MedicalRecord medicalRecord = new MedicalRecord();
        medicalRecord.setInitialWeight(
                new java.math.BigDecimal("27.80")
        );
        medicalRecord.setInitialCondition("STABLE");
        medicalRecord.setInjuries(
                "Injury caused by fishing net"
        );
        medicalRecord.setObservations(
                "Possible plastic ingestion"
        );

        animal.assignMedicalRecord(medicalRecord);

        // =========================
        // 5. Specialist
        // =========================
        Expertise marineReptiles = expertiseRepository
                .findByNameIgnoreCase("Marine Reptiles")
                .orElseThrow();

        Expertise trauma = expertiseRepository
                .findByNameIgnoreCase("Trauma")
                .orElseThrow();

        Expertise rehabilitation = expertiseRepository
                .findByNameIgnoreCase("Rehabilitation")
                .orElseThrow();

        Specialist specialist = new Specialist();
        specialist.setProfessionalCode("SPEC-001");
        specialist.setFirstName("Elena");
        specialist.setLastName("Vargas");
        specialist.setEmail("elena@deepblue.org");
        specialist.setActive(true);

        specialist.addExpertise(marineReptiles);
        specialist.addExpertise(trauma);
        specialist.addExpertise(rehabilitation);

        specialistRepository.save(specialist);

        // =========================
        // 6. Persist RescueCase
        // =========================
        rescueCaseRepository.save(rescueCase);

        // =========================
        // 7. Treatments
        // =========================
        Treatment treatment1 = new Treatment();
        treatment1.setAnimal(animal);
        treatment1.setSpecialist(specialist);
        treatment1.setPerformedAt(
                java.time.LocalDateTime.of(2026, 8, 18, 10, 0)
        );
        treatment1.setType(
                com.deepblue.rescue.domain.TreatmentType.WOUND_CARE
        );
        treatment1.setDescription(
                "Cleaning of left front flipper"
        );

        Treatment treatment2 = new Treatment();
        treatment2.setAnimal(animal);
        treatment2.setSpecialist(specialist);
        treatment2.setPerformedAt(
                java.time.LocalDateTime.of(2026, 8, 18, 14, 0)
        );
        treatment2.setType(
                com.deepblue.rescue.domain.TreatmentType.HYDRATION
        );
        treatment2.setDescription(
                "Subcutaneous fluid therapy"
        );

        treatmentRepository.saveAll(
                List.of(treatment1, treatment2)
        );

        // =========================
        // 8. Verificaciones
        // =========================
        assertTrue(center.getId() != null);
        assertTrue(rescueCase.getId() != null);
        assertTrue(animal.getId() != null);
        assertTrue(medicalRecord.getId() != null);
        assertTrue(specialist.getId() != null);
        assertTrue(treatment1.getId() != null);
        assertTrue(treatment2.getId() != null);

        assertEquals(
                center.getId(),
                rescueCase.getRescueCenter().getId()
        );

        assertEquals(
                rescueCase.getId(),
                animal.getRescueCase().getId()
        );

        assertEquals(
                animal.getId(),
                medicalRecord.getAnimal().getId()
        );

        assertEquals(
                3,
                specialist.getExpertiseAreas().size()
        );

        assertEquals(
                animal.getId(),
                treatment1.getAnimal().getId()
        );

        assertEquals(
                specialist.getId(),
                treatment1.getSpecialist().getId()
        );

        assertEquals(
                animal.getId(),
                treatment2.getAnimal().getId()
        );

        assertEquals(
                specialist.getId(),
                treatment2.getSpecialist().getId()
        );
    }
    @Test
    void shouldFindIntegratorRescueCaseByCode() {
        RescueCenter center = new RescueCenter();
        center.setCode("DB-CAR");
        center.setName("DeepBlue Caribbean");
        center.setCity("Santa Marta");
        rescueCenterRepository.save(center);

        RescueCase rescueCase = new RescueCase();
        rescueCase.setCaseCode("RES-2026-100");
        rescueCase.setRescueDate(LocalDate.of(2026, 8, 18));
        rescueCase.setRescueLocation("Bahía Concha");
        rescueCase.setStatus(RescueStatus.IN_REHABILITATION);
        center.addCase(rescueCase);

        rescueCaseRepository.save(rescueCase);

        RescueCase found = rescueCaseRepository
                .findByCaseCode("RES-2026-100")
                .orElseThrow();

        assertEquals(
                "RES-2026-100",
                found.getCaseCode()
        );

        assertEquals(
                RescueStatus.IN_REHABILITATION,
                found.getStatus()
        );

        assertEquals(
                "Bahía Concha",
                found.getRescueLocation()
        );
    }
    @Test
    void shouldFindCasesInRehabilitation() {
        RescueCenter center = new RescueCenter();
        center.setCode("DB-REHAB");
        center.setName("DeepBlue Rehabilitation Center");
        center.setCity("Santa Marta");
        rescueCenterRepository.save(center);

        RescueCase rescueCase1 = new RescueCase();
        rescueCase1.setCaseCode("RES-REHAB-001");
        rescueCase1.setRescueDate(LocalDate.of(2026, 8, 18));
        rescueCase1.setRescueLocation("Bahía Concha");
        rescueCase1.setStatus(RescueStatus.IN_REHABILITATION);
        center.addCase(rescueCase1);

        RescueCase rescueCase2 = new RescueCase();
        rescueCase2.setCaseCode("RES-REHAB-002");
        rescueCase2.setRescueDate(LocalDate.of(2026, 8, 20));
        rescueCase2.setRescueLocation("Taganga");
        rescueCase2.setStatus(RescueStatus.IN_REHABILITATION);
        center.addCase(rescueCase2);

        rescueCaseRepository.save(rescueCase1);
        rescueCaseRepository.save(rescueCase2);

        List<RescueCase> cases =
                rescueCaseRepository.findByStatusOrderByRescueDateAsc(
                        RescueStatus.IN_REHABILITATION
                );

        assertEquals(2, cases.size());
        assertEquals("RES-REHAB-001", cases.get(0).getCaseCode());
        assertEquals("RES-REHAB-002", cases.get(1).getCaseCode());
    }
    @Test
    void shouldFindAnimalsByCommonNameContainingIgnoreCase() {
        RescueCenter center = new RescueCenter();
        center.setCode("DB-TURTLE");
        center.setName("DeepBlue Turtle Center");
        center.setCity("Santa Marta");

        rescueCenterRepository.save(center);

        RescueCase rescueCase1 = new RescueCase();
        rescueCase1.setCaseCode("RES-TURTLE-001");
        rescueCase1.setRescueDate(LocalDate.of(2026, 9, 25));
        rescueCase1.setRescueLocation("Bahía Concha");
        rescueCase1.setStatus(RescueStatus.IN_REHABILITATION);
        center.addCase(rescueCase1);

        RescueCase rescueCase2 = new RescueCase();
        rescueCase2.setCaseCode("RES-TURTLE-002");
        rescueCase2.setRescueDate(LocalDate.of(2026, 9, 26));
        rescueCase2.setRescueLocation("Taganga");
        rescueCase2.setStatus(RescueStatus.ADMITTED);
        center.addCase(rescueCase2);

        RescueCase rescueCase3 = new RescueCase();
        rescueCase3.setCaseCode("RES-TURTLE-003");
        rescueCase3.setRescueDate(LocalDate.of(2026, 9, 27));
        rescueCase3.setRescueLocation("Playa Grande");
        rescueCase3.setStatus(RescueStatus.ADMITTED);
        center.addCase(rescueCase3);

        Animal animal1 = new Animal();
        animal1.setAnimalCode("AN-TURTLE-001");
        animal1.setCommonName("Green Sea Turtle");
        animal1.setScientificName("Chelonia mydas");
        animal1.setSex(AnimalSex.FEMALE);
        rescueCase1.assignAnimal(animal1);

        Animal animal2 = new Animal();
        animal2.setAnimalCode("AN-TURTLE-002");
        animal2.setCommonName("Leatherback Turtle");
        animal2.setScientificName("Dermochelys coriacea");
        animal2.setSex(AnimalSex.FEMALE);
        rescueCase2.assignAnimal(animal2);

        Animal animal3 = new Animal();
        animal3.setAnimalCode("AN-TURTLE-003");
        animal3.setCommonName("Dolphin");
        animal3.setScientificName("Delphinus delphis");
        animal3.setSex(AnimalSex.UNKNOWN);
        rescueCase3.assignAnimal(animal3);

        rescueCaseRepository.saveAll(
                List.of(rescueCase1, rescueCase2, rescueCase3)
        );

        List<Animal> animals =
                animalRepository.findByCommonNameContainingIgnoreCase("turtle");

        assertEquals(2, animals.size());

        assertTrue(animals.stream()
                .anyMatch(a -> a.getCommonName().equals("Green Sea Turtle")));

        assertTrue(animals.stream()
                .anyMatch(a -> a.getCommonName().equals("Leatherback Turtle")));
    }
    @Test
    void shouldFindIntegratorAnimalTreatmentsChronologically() {
        RescueCenter center = new RescueCenter();
        center.setCode("DB-TREATMENT");
        center.setName("DeepBlue Treatment Center");
        center.setCity("Santa Marta");

        rescueCenterRepository.save(center);

        RescueCase rescueCase = new RescueCase();
        rescueCase.setCaseCode("RES-TREATMENT-001");
        rescueCase.setRescueDate(LocalDate.of(2026, 8, 18));
        rescueCase.setRescueLocation("Bahía Concha");
        rescueCase.setStatus(RescueStatus.IN_REHABILITATION);

        center.addCase(rescueCase);

        Animal animal = new Animal();
        animal.setAnimalCode("AN-2026-100");
        animal.setCommonName("Green Sea Turtle");
        animal.setScientificName("Chelonia mydas");
        animal.setSex(AnimalSex.FEMALE);

        rescueCase.assignAnimal(animal);

        Specialist specialist = new Specialist();
        specialist.setProfessionalCode("SPEC-TREATMENT-001");
        specialist.setFirstName("Elena");
        specialist.setLastName("Vargas");
        specialist.setEmail("elena.treatment@deepblue.org");
        specialist.setActive(true);

        specialistRepository.save(specialist);
        rescueCaseRepository.save(rescueCase);

        Treatment treatment1 = new Treatment();
        treatment1.setAnimal(animal);
        treatment1.setSpecialist(specialist);
        treatment1.setPerformedAt(
                java.time.LocalDateTime.of(2026, 8, 18, 10, 0)
        );
        treatment1.setType(
                com.deepblue.rescue.domain.TreatmentType.WOUND_CARE
        );
        treatment1.setDescription(
                "Cleaning of left front flipper"
        );

        Treatment treatment2 = new Treatment();
        treatment2.setAnimal(animal);
        treatment2.setSpecialist(specialist);
        treatment2.setPerformedAt(
                java.time.LocalDateTime.of(2026, 8, 18, 14, 0)
        );
        treatment2.setType(
                com.deepblue.rescue.domain.TreatmentType.HYDRATION
        );
        treatment2.setDescription(
                "Subcutaneous fluid therapy"
        );

        treatmentRepository.saveAll(
                List.of(treatment1, treatment2)
        );

        List<Treatment> treatments =
                treatmentRepository.findByAnimal_IdOrderByPerformedAtAsc(
                        animal.getId()
                );

        assertEquals(2, treatments.size());

        assertEquals(
                java.time.LocalDateTime.of(2026, 8, 18, 10, 0),
                treatments.get(0).getPerformedAt()
        );

        assertEquals(
                java.time.LocalDateTime.of(2026, 8, 18, 14, 0),
                treatments.get(1).getPerformedAt()
        );
    }
    @Test
    void shouldFindIntegratorTreatmentsByRehabilitationExpertise() {
        Expertise rehabilitation = expertiseRepository
                .findByNameIgnoreCase("Rehabilitation")
                .orElseThrow();

        RescueCenter center = new RescueCenter();
        center.setCode("DB-REHAB-TREAT");
        center.setName("DeepBlue Rehabilitation");
        center.setCity("Santa Marta");

        rescueCenterRepository.save(center);

        RescueCase rescueCase = new RescueCase();
        rescueCase.setCaseCode("RES-REHAB-TREAT-001");
        rescueCase.setRescueDate(LocalDate.of(2026, 8, 18));
        rescueCase.setRescueLocation("Bahía Concha");
        rescueCase.setStatus(RescueStatus.IN_REHABILITATION);

        center.addCase(rescueCase);

        Animal animal = new Animal();
        animal.setAnimalCode("AN-REHAB-TREAT-001");
        animal.setCommonName("Green Sea Turtle");
        animal.setScientificName("Chelonia mydas");
        animal.setSex(AnimalSex.FEMALE);

        rescueCase.assignAnimal(animal);

        Specialist specialist = new Specialist();
        specialist.setProfessionalCode("SPEC-REHAB-001");
        specialist.setFirstName("Elena");
        specialist.setLastName("Vargas");
        specialist.setEmail("elena.rehab@deepblue.org");
        specialist.setActive(true);

        specialist.addExpertise(rehabilitation);

        specialistRepository.save(specialist);
        rescueCaseRepository.save(rescueCase);

        Treatment treatment = new Treatment();
        treatment.setAnimal(animal);
        treatment.setSpecialist(specialist);
        treatment.setPerformedAt(
                java.time.LocalDateTime.of(2026, 8, 18, 10, 0)
        );
        treatment.setType(
                com.deepblue.rescue.domain.TreatmentType.PHYSIOTHERAPY
        );
        treatment.setDescription("Rehabilitation therapy");

        treatmentRepository.save(treatment);

        List<Treatment> treatments =
                treatmentRepository.findBySpecialistExpertise(
                        "rehabilitation"
                );

        assertEquals(1, treatments.size());

        assertEquals(
                "Rehabilitation therapy",
                treatments.get(0).getDescription()
        );

        assertEquals(
                "Elena",
                treatments.get(0).getSpecialist().getFirstName()
        );
    }
    @Test
    void shouldFindIntegratorTreatmentsBetweenDates() {
        RescueCenter center = new RescueCenter();
        center.setCode("DB-DATE-TREAT");
        center.setName("DeepBlue Date Center");
        center.setCity("Santa Marta");

        rescueCenterRepository.save(center);

        RescueCase rescueCase = new RescueCase();
        rescueCase.setCaseCode("RES-DATE-TREAT-001");
        rescueCase.setRescueDate(LocalDate.of(2026, 8, 18));
        rescueCase.setRescueLocation("Bahía Concha");
        rescueCase.setStatus(RescueStatus.IN_REHABILITATION);

        center.addCase(rescueCase);

        Animal animal = new Animal();
        animal.setAnimalCode("AN-DATE-TREAT-001");
        animal.setCommonName("Green Sea Turtle");
        animal.setScientificName("Chelonia mydas");
        animal.setSex(AnimalSex.FEMALE);

        rescueCase.assignAnimal(animal);

        Specialist specialist = new Specialist();
        specialist.setProfessionalCode("SPEC-DATE-001");
        specialist.setFirstName("Elena");
        specialist.setLastName("Vargas");
        specialist.setEmail("elena.date@deepblue.org");
        specialist.setActive(true);

        specialistRepository.save(specialist);
        rescueCaseRepository.save(rescueCase);

        Treatment treatment1 = new Treatment();
        treatment1.setAnimal(animal);
        treatment1.setSpecialist(specialist);
        treatment1.setPerformedAt(
                java.time.LocalDateTime.of(2026, 8, 18, 10, 0)
        );
        treatment1.setType(
                com.deepblue.rescue.domain.TreatmentType.WOUND_CARE
        );
        treatment1.setDescription("Tratamiento dentro del intervalo");

        Treatment treatment2 = new Treatment();
        treatment2.setAnimal(animal);
        treatment2.setSpecialist(specialist);
        treatment2.setPerformedAt(
                java.time.LocalDateTime.of(2026, 8, 20, 14, 0)
        );
        treatment2.setType(
                com.deepblue.rescue.domain.TreatmentType.HYDRATION
        );
        treatment2.setDescription("Otro tratamiento dentro del intervalo");

        Treatment treatmentOutside = new Treatment();
        treatmentOutside.setAnimal(animal);
        treatmentOutside.setSpecialist(specialist);
        treatmentOutside.setPerformedAt(
                java.time.LocalDateTime.of(2026, 8, 25, 10, 0)
        );
        treatmentOutside.setType(
                com.deepblue.rescue.domain.TreatmentType.OBSERVATION
        );
        treatmentOutside.setDescription("Tratamiento fuera del intervalo");

        treatmentRepository.saveAll(
                List.of(treatment1, treatment2, treatmentOutside)
        );

        List<Treatment> treatments =
                treatmentRepository.findByPerformedAtBetween(
                        java.time.LocalDateTime.of(2026, 8, 18, 0, 0),
                        java.time.LocalDateTime.of(2026, 8, 20, 23, 59)
                );

        assertEquals(2, treatments.size());

        assertEquals(
                java.time.LocalDateTime.of(2026, 8, 18, 10, 0),
                treatments.get(0).getPerformedAt()
        );

        assertEquals(
                java.time.LocalDateTime.of(2026, 8, 20, 14, 0),
                treatments.get(1).getPerformedAt()
        );
    }
    @Test
    void shouldFindAnimalsInRehabilitationWithTraumaTreatment() {
        Expertise trauma = expertiseRepository
                .findByNameIgnoreCase("Trauma")
                .orElseThrow();

        RescueCenter center = new RescueCenter();
        center.setCode("DB-CHALLENGE");
        center.setName("DeepBlue Challenge Center");
        center.setCity("Santa Marta");

        rescueCenterRepository.save(center);

        RescueCase rescueCase = new RescueCase();
        rescueCase.setCaseCode("RES-CHALLENGE-001");
        rescueCase.setRescueDate(LocalDate.of(2026, 9, 1));
        rescueCase.setRescueLocation("Bahía Concha");
        rescueCase.setStatus(RescueStatus.IN_REHABILITATION);

        center.addCase(rescueCase);

        Animal animal = new Animal();
        animal.setAnimalCode("AN-CHALLENGE-001");
        animal.setCommonName("Green Sea Turtle");
        animal.setScientificName("Chelonia mydas");
        animal.setSex(AnimalSex.FEMALE);

        rescueCase.assignAnimal(animal);

        Specialist specialist = new Specialist();
        specialist.setProfessionalCode("SPEC-CHALLENGE-001");
        specialist.setFirstName("Elena");
        specialist.setLastName("Vargas");
        specialist.setEmail("elena.challenge@deepblue.org");
        specialist.setActive(true);

        specialist.addExpertise(trauma);

        specialistRepository.save(specialist);
        rescueCaseRepository.save(rescueCase);

        Treatment treatment = new Treatment();
        treatment.setAnimal(animal);
        treatment.setSpecialist(specialist);
        treatment.setPerformedAt(
                java.time.LocalDateTime.of(2026, 9, 1, 10, 0)
        );
        treatment.setType(
                com.deepblue.rescue.domain.TreatmentType.WOUND_CARE
        );
        treatment.setDescription("Trauma treatment");

        treatmentRepository.save(treatment);

        List<Animal> animals =
                animalRepository.findAnimalsInStatusWithTreatmentBySpecialistExpertise(
                        RescueStatus.IN_REHABILITATION,
                        "trauma"
                );

        assertEquals(1, animals.size());

        assertEquals(
                "AN-CHALLENGE-001",
                animals.get(0).getAnimalCode()
        );

        assertEquals(
                "Green Sea Turtle",
                animals.get(0).getCommonName()
        );
    }
}