package com.jftse.entities.database.repository.account;

import com.jftse.entities.database.model.account.Role;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Role findByRole(String role);
}
