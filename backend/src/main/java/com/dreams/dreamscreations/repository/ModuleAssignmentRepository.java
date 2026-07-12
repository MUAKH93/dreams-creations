package com.dreams.dreamscreations.repository;

import com.dreams.dreamscreations.entity.ModuleAssignment;
import com.dreams.dreamscreations.entity.ProductionBatch;
import com.dreams.dreamscreations.entity.ProductionModule;
import com.dreams.dreamscreations.entity.Supervisor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ModuleAssignmentRepository extends JpaRepository<ModuleAssignment, Long> {

    @Query("SELECT DISTINCT ma FROM ModuleAssignment ma " +
           "JOIN FETCH ma.batch b JOIN FETCH b.suit s JOIN FETCH s.design " +
           "JOIN FETCH ma.module m JOIN FETCH m.stage " +
           "JOIN FETCH ma.supervisor " +
           "LEFT JOIN FETCH ma.designingWorkType " +
           "LEFT JOIN FETCH ma.fillingWorkType " +
           "LEFT JOIN FETCH ma.skuLines sl LEFT JOIN FETCH sl.size " +
           "ORDER BY ma.assignmentId DESC")
    List<ModuleAssignment> findAllWithDetails();

    @Query("SELECT DISTINCT ma FROM ModuleAssignment ma " +
           "JOIN FETCH ma.batch b JOIN FETCH b.suit s JOIN FETCH s.design " +
           "JOIN FETCH ma.module m JOIN FETCH m.stage " +
           "LEFT JOIN FETCH ma.designingWorkType " +
           "LEFT JOIN FETCH ma.fillingWorkType " +
           "LEFT JOIN FETCH ma.skuLines sl LEFT JOIN FETCH sl.size " +
           "WHERE ma.assignmentId = :id")
    Optional<ModuleAssignment> findByIdWithDetails(@Param("id") Long id);

    @Query("SELECT DISTINCT ma FROM ModuleAssignment ma " +
           "JOIN FETCH ma.batch b JOIN FETCH b.suit s JOIN FETCH s.design " +
           "JOIN FETCH ma.module m JOIN FETCH m.stage " +
           "LEFT JOIN FETCH ma.designingWorkType " +
           "LEFT JOIN FETCH ma.fillingWorkType " +
           "LEFT JOIN FETCH ma.skuLines sl LEFT JOIN FETCH sl.size " +
           "WHERE ma.supervisor.supervisorId = :supervisorId " +
           "ORDER BY ma.assignmentId DESC")
    List<ModuleAssignment> findBySupervisorWithDetails(@Param("supervisorId") Long supervisorId);

    List<ModuleAssignment> findByBatch(ProductionBatch batch);
    List<ModuleAssignment> findByStatus(String status);
    List<ModuleAssignment> findBySupervisor(Supervisor supervisor);
    long countBySupervisor_SupervisorId(Long supervisorId);
    List<ModuleAssignment> findByBatchAndModule(ProductionBatch batch, ProductionModule module);

    // All overdue dispatches (due date passed, not yet returned)
    @Query("SELECT ma FROM ModuleAssignment ma WHERE ma.dueDate < :now AND ma.status != 'returned'")
    List<ModuleAssignment> findOverdueAssignments(@Param("now") LocalDateTime now);

    // Total pieces returned OK from a batch at a specific module
    @Query("SELECT COALESCE(SUM(ma.quantityReturnedOk), 0) FROM ModuleAssignment ma " +
           "WHERE ma.batch.batchId = :batchId AND ma.module.moduleId = :moduleId AND ma.status = 'returned'")
    Integer sumReturnedOkByBatchAndModule(@Param("batchId") Long batchId,
                                          @Param("moduleId") Long moduleId);

    // Total pieces sent to a batch at a specific module (across all dispatches)
    @Query("SELECT COALESCE(SUM(ma.quantitySent), 0) FROM ModuleAssignment ma " +
           "WHERE ma.batch.batchId = :batchId AND ma.module.moduleId = :moduleId")
    Integer sumSentByBatchAndModule(@Param("batchId") Long batchId,
                                    @Param("moduleId") Long moduleId);

    // Pieces received at a module but not yet forwarded to next module
    // = sum of quantityReturnedOk from module N - sum of quantitySent to module N+1
    // This is computed in the service layer using the two queries above
}
