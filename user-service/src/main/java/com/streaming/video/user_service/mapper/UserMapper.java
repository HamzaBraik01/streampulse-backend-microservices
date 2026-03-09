package com.streaming.video.user_service.mapper;

import com.streaming.video.user_service.dto.*;
import com.streaming.video.user_service.entity.User;
import com.streaming.video.user_service.entity.Watchlist;
import com.streaming.video.user_service.entity.WatchHistory;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

/**
 * MapStruct mapper for User, Watchlist, and WatchHistory entity ↔ DTO conversions.
 */
@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface UserMapper {

    /**
     * Convert User entity to UserDTO (excludes password).
     */
    UserDTO toUserDTO(User user);

    /**
     * Convert UserCreateDTO to User entity.
     */
    @Mapping(target = "id", ignore = true)
    User toUser(UserCreateDTO userCreateDTO);

    /**
     * Update an existing User entity from UserCreateDTO (all fields required).
     */
    @Mapping(target = "id", ignore = true)
    void updateUserFromDTO(UserCreateDTO dto, @MappingTarget User user);

    /**
     * Update an existing User entity from UserUpdateDTO (only non-null fields applied).
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true)
    void updateUserFromUpdateDTO(UserUpdateDTO dto, @MappingTarget User user);

    /**
     * Convert Watchlist entity to WatchlistDTO.
     */
    WatchlistDTO toWatchlistDTO(Watchlist watchlist);

    /**
     * Convert WatchHistory entity to WatchHistoryDTO.
     */
    WatchHistoryDTO toWatchHistoryDTO(WatchHistory watchHistory);
}
