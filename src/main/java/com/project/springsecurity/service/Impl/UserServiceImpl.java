package com.project.springsecurity.service.Impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.springsecurity.domain.Account;
import com.project.springsecurity.repository.UserRepository;
import com.project.springsecurity.service.UserService;

@Service("userSerivce")
public class UserServiceImpl implements UserService {
	@Autowired
	private UserRepository userRepository;

	@Transactional
	@Override
	public void createUser(Account account){
		userRepository.save(account);
	}
}
