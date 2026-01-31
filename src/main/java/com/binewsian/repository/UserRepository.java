package com.binewsian.repository;

import com.binewsian.enums.Role;
import com.binewsian.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsernameAllIgnoreCase(String username);

    Optional<User> findByEmail(String email);

    boolean existsByUsernameAllIgnoreCase(String username);

    boolean existsByEmailAllIgnoreCase(String email);

    int countByRoleAndEnabled(Role role, boolean enabled);

    Page<User> findByRole(Role role, Pageable pageable);

    List<User> findByRoleOrderByCreatedAtDesc(Role role);

    List<User> findByRoleAndEnabledTrue(Role role);
}
