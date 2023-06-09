package com.example.fasns.domain.follow.service;

import com.example.fasns.domain.follow.dto.FollowDto;
import com.example.fasns.domain.follow.entity.Follow;
import com.example.fasns.domain.follow.repository.FollowRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class  FollowReadService {

    private final FollowRepository followRepository;

    public List<FollowDto> getFollowings(Long memberId) {
        return followRepository.findAllByFromMemberId(memberId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public List<FollowDto> getFollowers(Long memberId) {
        return followRepository.findAllByToMemberId(memberId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    private FollowDto toDto(Follow follow) {
        return FollowDto.builder()
                .id(follow.getId())
                .fromMemberId(follow.getFromMemberId())
                .toMemberId(follow.getToMemberId())
                .createdAt(follow.getCreatedAt())
                .build();
    }
}
