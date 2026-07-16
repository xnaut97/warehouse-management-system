package com.github.xnaut97.wms.repository.user;

import com.github.xnaut97.wms.entity.user.Role;
import com.github.xnaut97.wms.enums.RoleType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByRole(RoleType role);

    boolean existsByRole(RoleType role);

}