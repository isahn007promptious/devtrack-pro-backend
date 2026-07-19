package com.devtrackpro.repository;

import com.devtrackpro.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByUsername(String username);
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);

    @org.springframework.data.jpa.repository.Query("SELECT DISTINCT m.user FROM OrganizationMember m WHERE m.organization.id IN " +
           "(SELECT om.organization.id FROM OrganizationMember om WHERE om.user.id = :userId) " +
           "AND (m.user.username LIKE %:q% OR m.user.firstName LIKE %:q% OR m.user.lastName LIKE %:q% OR m.user.email LIKE %:q%)")
    List<User> searchUsersInMyOrganizations(@org.springframework.data.repository.query.Param("userId") Long userId, @org.springframework.data.repository.query.Param("q") String q);
}
