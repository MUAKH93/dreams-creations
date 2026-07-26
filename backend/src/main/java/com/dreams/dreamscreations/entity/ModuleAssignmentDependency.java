package com.dreams.dreamscreations.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Handles the "Stitching can't start until Cutting is done" requirement.
 *
 * Each row says: "assignment X cannot begin until assignment Y is returned."
 *
 * For parallel/independent assignments, no row exists — they proceed freely.
 *
 * Example:
 *   Assignment #3 (Stitching) depends on Assignment #1 (Cutting)
 *   → row: assignment_id=3, depends_on_assignment_id=1
 *
 * The service checks this before allowing a dispatch to move to in_progress.
 */
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Entity
@Table(name = "module_assignment_dependency",
        uniqueConstraints = @UniqueConstraint(columnNames = {"assignment_id", "depends_on_assignment_id"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ModuleAssignmentDependency {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /**
     * The assignment that is BLOCKED until its dependency is resolved.
     * e.g. Stitching assignment
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignment_id", nullable = false)
    private ModuleAssignment assignment;

    /**
     * The assignment that must be RETURNED first.
     * e.g. Cutting assignment
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "depends_on_assignment_id", nullable = false)
    private ModuleAssignment dependsOnAssignment;
}
