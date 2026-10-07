package com.gymflow.membership.service;

import com.gymflow.membership.dto.*;
import com.gymflow.membership.entity.Membership;
import com.gymflow.membership.entity.Payment;
import com.gymflow.membership.entity.Plan;
import com.gymflow.membership.repository.MembershipRepository;
import com.gymflow.membership.repository.PaymentRepository;
import com.gymflow.membership.repository.PlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MembershipService {
    
    private final PlanRepository planRepository;
    private final MembershipRepository membershipRepository;
    private final PaymentRepository paymentRepository;
    
    public List<PlanDto> getAllPlans() {
        return planRepository.findAll().stream()
                .map(this::toPlanDto)
                .collect(Collectors.toList());
    }
    
    public PlanDto getPlanById(Long id) {
        Plan plan = planRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Plan not found"));
        return toPlanDto(plan);
    }
    
    @Transactional
    public PlanDto createPlan(CreatePlanRequest request) {
        Plan plan = new Plan();
        plan.setName(request.getName());
        plan.setPrice(request.getPrice());
        plan.setDuration(request.getDuration());
        plan.setFeatures(request.getFeatures());
        
        plan = planRepository.save(plan);
        return toPlanDto(plan);
    }
    
    @Transactional
    public PlanDto updatePlan(Long id, CreatePlanRequest request) {
        Plan plan = planRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Plan not found"));
        
        plan.setName(request.getName());
        plan.setPrice(request.getPrice());
        plan.setDuration(request.getDuration());
        plan.setFeatures(request.getFeatures());
        
        plan = planRepository.save(plan);
        return toPlanDto(plan);
    }
    
    @Transactional
    public void deletePlan(Long id) {
        if (!planRepository.existsById(id)) {
            throw new RuntimeException("Plan not found");
        }
        planRepository.deleteById(id);
    }
    
    public List<MembershipDto> getAllMemberships() {
        return membershipRepository.findAll().stream()
                .map(this::toMembershipDto)
                .collect(Collectors.toList());
    }
    
    public MembershipDto getMembershipById(Long id) {
        Membership membership = membershipRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Membership not found"));
        return toMembershipDto(membership);
    }
    
    public List<MembershipDto> getMembershipsByMemberId(Long memberId) {
        return membershipRepository.findByMemberId(memberId).stream()
                .map(this::toMembershipDto)
                .collect(Collectors.toList());
    }
    
    @Transactional
    public MembershipDto createMembership(CreateMembershipRequest request) {
        if (membershipRepository.existsByMemberIdAndStatus(request.getMemberId(), Membership.Status.ACTIVE)) {
            throw new RuntimeException("Member already has an active membership");
        }
        
        Plan plan = planRepository.findById(request.getPlanId())
                .orElseThrow(() -> new RuntimeException("Plan not found"));
        
        Membership membership = new Membership();
        membership.setMemberId(request.getMemberId());
        membership.setPlanId(request.getPlanId());
        membership.setStartDate(LocalDate.now());
        membership.setEndDate(LocalDate.now().plusDays(plan.getDuration()));
        membership.setStatus(Membership.Status.ACTIVE);
        
        membership = membershipRepository.save(membership);
        
        // Create payment
        Payment payment = new Payment();
        payment.setMembershipId(membership.getId());
        payment.setAmount(plan.getPrice());
        payment.setStatus(Payment.Status.PENDING);
        paymentRepository.save(payment);
        
        return toMembershipDto(membership);
    }
    
    @Transactional
    public MembershipDto updateMembership(Long id, String status) {
        Membership membership = membershipRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Membership not found"));
        
        membership.setStatus(Membership.Status.valueOf(status.toUpperCase()));
        membership = membershipRepository.save(membership);
        
        return toMembershipDto(membership);
    }
    
    public MembershipValidationResponse validateMembership(Long memberId) {
        Membership membership = membershipRepository.findByMemberIdAndStatus(memberId, Membership.Status.ACTIVE)
                .orElse(null);
        
        if (membership == null) {
            return new MembershipValidationResponse(false, "NO_ACTIVE", null, "No active membership found");
        }
        
        if (membership.getEndDate().isBefore(LocalDate.now())) {
            membership.setStatus(Membership.Status.EXPIRED);
            membershipRepository.save(membership);
            return new MembershipValidationResponse(false, "EXPIRED", membership.getEndDate(), "Membership has expired");
        }
        
        return new MembershipValidationResponse(true, membership.getStatus().name(), membership.getEndDate(), "Membership is valid");
    }
    
    public List<PaymentDto> getAllPayments() {
        return paymentRepository.findAll().stream()
                .map(this::toPaymentDto)
                .collect(Collectors.toList());
    }
    
    public PaymentDto getPaymentById(Long id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Payment not found"));
        return toPaymentDto(payment);
    }
    
    @Transactional
    public PaymentDto createPayment(CreatePaymentRequest request) {
        Payment payment = new Payment();
        payment.setMembershipId(request.getMembershipId());
        payment.setAmount(request.getAmount());
        payment.setStatus(Payment.Status.PENDING);
        payment.setNotes(request.getNotes());
        
        payment = paymentRepository.save(payment);
        return toPaymentDto(payment);
    }
    
    private PlanDto toPlanDto(Plan plan) {
        return new PlanDto(plan.getId(), plan.getName(), plan.getPrice(), plan.getDuration(), plan.getFeatures(), plan.getCreatedAt(), plan.getUpdatedAt());
    }
    
    private MembershipDto toMembershipDto(Membership membership) {
        Plan plan = planRepository.findById(membership.getPlanId()).orElse(null);
        String planName = plan != null ? plan.getName() : "Unknown";
        return new MembershipDto(membership.getId(), membership.getMemberId(), membership.getPlanId(), planName, membership.getStartDate(), membership.getEndDate(), membership.getStatus().name(), membership.getCreatedAt(), membership.getUpdatedAt());
    }
    
    private PaymentDto toPaymentDto(Payment payment) {
        return new PaymentDto(payment.getId(), payment.getMembershipId(), payment.getAmount(), payment.getStatus().name(), payment.getNotes(), payment.getCreatedAt());
    }
}
