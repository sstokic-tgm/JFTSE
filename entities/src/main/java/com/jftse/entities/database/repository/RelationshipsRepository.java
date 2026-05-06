package com.jftse.entities.database.repository;

import com.jftse.entities.database.model.KStatus;
import com.jftse.entities.database.model.SRelationshipRoles;
import com.jftse.entities.database.model.SRelationshipTypes;
import com.jftse.entities.database.model.SRelationships;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RelationshipsRepository extends JpaRepository<SRelationships, Long> {
    List<SRelationships> findAllByRole(SRelationshipRoles role);
    List<SRelationships> findAllByRelationship(SRelationshipTypes relationship);
    List<SRelationships> findAllByRoleAndRelationship(SRelationshipRoles role, SRelationshipTypes relationship);
    List<SRelationships> findAllByRoleAndRelationshipAndStatus(SRelationshipRoles role, SRelationshipTypes relationship, KStatus status);


}
