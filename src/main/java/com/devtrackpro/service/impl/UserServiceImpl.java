package com.devtrackpro.service.impl;

import com.devtrackpro.dto.UpdateProfileRequest;
import com.devtrackpro.dto.UserProfileResponse;
import com.devtrackpro.entity.User;
import com.devtrackpro.exception.ResourceNotFoundException;
import com.devtrackpro.mapper.UserMapper;
import com.devtrackpro.repository.UserRepository;
import com.devtrackpro.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public UserServiceImpl(UserRepository userRepository, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public UserProfileResponse getCurrentUserProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
        return userMapper.toUserProfileResponse(user);
    }

    @Override
    public UserProfileResponse updateCurrentUserProfile(String email, UpdateProfileRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        userMapper.updateUserFromRequest(request, user);
        User updatedUser = userRepository.save(user);

        return userMapper.toUserProfileResponse(updatedUser);
    }
}
