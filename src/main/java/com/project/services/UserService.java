package com.project.services;

import java.util.List;

import com.project.entities.User;
import com.project.module.dto.UserDto;

public interface UserService {
	
	UserDto createUser(UserDto user);
	UserDto updateUser(UserDto user, long userId);
	UserDto getUserById(long userId);
	List<UserDto> getAllUser();
	void deleteUser(Long userId);
	User findByEnrollementAndPassword(String enrollment, String password);
}
