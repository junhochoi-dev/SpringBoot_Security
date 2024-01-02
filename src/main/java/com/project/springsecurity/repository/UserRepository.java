package com.project.springsecurity.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.project.springsecurity.domain.Account;

public interface UserRepository extends JpaRepository<Account, Long> {
	Account findByUsername(String username);
}
