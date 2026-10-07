package com.gymflow.membership.repository;

import com.gymflow.membership.entity.Membership;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface MembershipRepository extends JpaRepository<Membership, Long> {
    Optional<Membership> findByMemberIdAndStatus(Long memberId, Membership.Status status);
    List<Membership> findByMemberId(Long memberId);
    boolean existsByMemberIdAndStatus(Long memberId, Membership.Status status);
}
