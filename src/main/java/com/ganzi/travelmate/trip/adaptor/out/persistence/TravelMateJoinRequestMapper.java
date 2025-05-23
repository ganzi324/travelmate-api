package com.ganzi.travelmate.trip.adaptor.out.persistence;

import com.ganzi.travelmate.trip.domain.TravelMateJoinRequest;
import com.ganzi.travelmate.user.adaptor.out.persistence.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TravelMateJoinRequestMapper {

    private final TravelMatePostMapper travelMatePostMapper;
    private final UserMapper userMapper;

    public TravelMateJoinRequest mapToDomainEntity(com.ganzi.travelmate.trip.adaptor.out.persistence.TravelMateJoinRequestJpaEntity travelMateJoinRequest) {
        return TravelMateJoinRequest.withId(
                TravelMateJoinRequest.Id.of(travelMateJoinRequest.getId()),
                travelMatePostMapper.mapToDomainEntity(travelMateJoinRequest.getTravelMatePost()),
                userMapper.mapToDomainEntity(travelMateJoinRequest.getRequester()),
                travelMateJoinRequest.getMessage(),
                travelMateJoinRequest.getStatus(),
                travelMateJoinRequest.getCreatedAt()
        );
    }

    public com.ganzi.travelmate.trip.adaptor.out.persistence.TravelMateJoinRequestJpaEntity mapToJpaEntity(TravelMateJoinRequest travelMateJoinRequest) {
        return new com.ganzi.travelmate.trip.adaptor.out.persistence.TravelMateJoinRequestJpaEntity(
                travelMateJoinRequest.getId().map(TravelMateJoinRequest.Id::value).orElse(null),
                travelMatePostMapper.mapToJpaEntity(travelMateJoinRequest.getTravelMatePost()),
                userMapper.mapToJpaEntity(travelMateJoinRequest.getRequester()),
                travelMateJoinRequest.getMessage(),
                travelMateJoinRequest.getStatus()
        );
    }
}
