package com.deepblue.rescue.repository;

import com.deepblue.rescue.domain.Treatment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface TreatmentRepository extends JpaRepository<Treatment, Long> {

    List<Treatment> findByAnimal_IdOrderByPerformedAtAsc(Long animalId);

    @Query("""
            SELECT t
            FROM Treatment t
            WHERE t.performedAt BETWEEN :startDate AND :endDate
            ORDER BY t.performedAt ASC
            """)
    List<Treatment> findByPerformedAtBetween(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    @Query("""
            SELECT t
            FROM Treatment t
            JOIN t.animal a
            JOIN a.rescueCase rc
            JOIN rc.rescueCenter center
            WHERE center.code = :centerCode
            ORDER BY t.performedAt ASC
            """)
    List<Treatment> findByRescueCenterCode(
            @Param("centerCode") String centerCode
    );

    @Query("""
            SELECT DISTINCT t
            FROM Treatment t
            JOIN t.specialist s
            JOIN s.expertiseAreas e
            WHERE LOWER(e.name) = LOWER(:expertiseName)
            ORDER BY t.performedAt ASC
            """)
    List<Treatment> findBySpecialistExpertise(
            @Param("expertiseName") String expertiseName
    );
}