package io.plan.mate.expense.tracker.backend.member.service.impl;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.plan.mate.expense.tracker.backend.group.jpa.entity.Group;
import io.plan.mate.expense.tracker.backend.group.jpa.repository.GroupRepository;
import io.plan.mate.expense.tracker.backend.member.controller.payload.event.MemberChangedEvent;
import io.plan.mate.expense.tracker.backend.member.controller.payload.request.AddUserRequest;
import io.plan.mate.expense.tracker.backend.member.jpa.entity.Member;
import io.plan.mate.expense.tracker.backend.member.jpa.repository.MemberRepository;
import io.plan.mate.expense.tracker.backend.member.service.dto.MemberDto;
import io.plan.mate.expense.tracker.backend.settlement.controller.payload.event.SettlementsChangedEvent;
import io.plan.mate.expense.tracker.backend.settlement.service.SettlementService;
import io.plan.mate.expense.tracker.backend.user.jpa.entity.User;
import io.plan.mate.expense.tracker.backend.user.jpa.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
@DisplayName("MemberServiceImpl")
class MemberServiceImplTest {

  @Mock private UserRepository userRepository;
  @Mock private MemberRepository memberRepository;
  @Mock private GroupRepository groupRepository;
  @Mock private ModelMapper modelMapper;
  @Mock private SettlementService settlementService;
  @Mock private ApplicationEventPublisher eventPublisher;

  @InjectMocks private MemberServiceImpl memberService;

  @Nested
  @DisplayName("addUserToGroup")
  class AddUserToGroup {

    @Test
    @DisplayName("clears the settlement cache and publishes member + settlements events")
    void addUserToGroup_shouldClearCacheAndPublishEvents_whenMemberAdded() {
      User user = User.builder().id(1L).username("alice").build();
      Group group = Group.builder().id(1L).name("Trip").build();
      Member member = Member.builder().id(1L).user(user).group(group).build();

      when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));
      when(groupRepository.findById(1L)).thenReturn(Optional.of(group));
      when(memberRepository.findByGroupIdAndUserId(1L, 1L)).thenReturn(Optional.empty());
      when(memberRepository.save(any())).thenReturn(member);
      when(modelMapper.map(member, MemberDto.class)).thenReturn(MemberDto.builder().id(1L).build());

      memberService.addUserToGroup(1L, new AddUserRequest("alice"));

      verify(settlementService).clearSettlementCache(1L);
      verify(eventPublisher).publishEvent(any(MemberChangedEvent.class));
      verify(eventPublisher).publishEvent(any(SettlementsChangedEvent.class));
    }
  }

  @Nested
  @DisplayName("removeUserFromGroup")
  class RemoveUserFromGroup {

    @Test
    @DisplayName("clears the settlement cache and publishes member + settlements events")
    void removeUserFromGroup_shouldClearCacheAndPublishEvents_whenMemberRemoved() {
      Member member = Member.builder().id(1L).build();
      when(memberRepository.findByGroupIdAndId(1L, 1L)).thenReturn(Optional.of(member));

      memberService.removeUserFromGroup(1L, 1L);

      verify(settlementService).clearSettlementCache(1L);
      verify(eventPublisher).publishEvent(any(MemberChangedEvent.class));
      verify(eventPublisher).publishEvent(any(SettlementsChangedEvent.class));
    }
  }
}
