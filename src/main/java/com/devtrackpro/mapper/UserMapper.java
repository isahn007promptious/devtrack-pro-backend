package com.devtrackpro.mapper;

import com.devtrackpro.dto.UserProfileResponse;
import com.devtrackpro.dto.UpdateProfileRequest;
import com.devtrackpro.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "skills", source = "skills")
    UserProfileResponse toUserProfileResponse(User user);

    @Mapping(target = "skills", source = "skills")
    void updateUserFromRequest(UpdateProfileRequest request, @MappingTarget User user);

    default List<String> mapSkillsToStringList(String skills) {
        if (skills == null || skills.isBlank()) {
            return Collections.emptyList();
        }
        return Arrays.asList(skills.split(",\\s*"));
    }

    default String mapStringListToSkills(List<String> skills) {
        if (skills == null || skills.isEmpty()) {
            return null;
        }
        return String.join(",", skills);
    }
}
