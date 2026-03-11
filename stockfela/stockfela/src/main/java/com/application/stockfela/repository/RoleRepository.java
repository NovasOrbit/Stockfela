package com.application.stockfela.repository;

import com.application.stockfela.entity.Role;
import com.application.stockfela.entity.Role.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role,Long> {

    Optional<Role> findByName(RoleName name);
}
