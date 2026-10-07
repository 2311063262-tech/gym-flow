package com.gymflow.membership.controller;

import com.gymflow.membership.dto.*;
import com.gymflow.membership.service.MembershipService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class MembershipController {
    
    private final MembershipService membershipService;
    
    // Plans endpoints
    @GetMapping("/plans")
    public ResponseEntity<List<PlanDto>> getAllPlans() {
        return ResponseEntity.ok(membershipService.getAllPlans());
    }
    
    @GetMapping("/plans/{id}")
    public ResponseEntity<PlanDto> getPlanById(@PathVariable Long id) {
        return ResponseEntity.ok(membershipService.getPlanById(id));
    }
    
    @PostMapping("/plans")
    public ResponseEntity<PlanDto> createPlan(@Valid @RequestBody CreatePlanRequest request) {
        return ResponseEntity.ok(membershipService.createPlan(request));
    }
    
    @PutMapping("/plans/{id}")
    public ResponseEntity<PlanDto> updatePlan(@PathVariable Long id, @Valid @RequestBody CreatePlanRequest request) {
        return ResponseEntity.ok(membershipService.updatePlan(id, request));
    }
    
    @DeleteMapping("/plans/{id}")
    public ResponseEntity<Void> deletePlan(@PathVariable Long id) {
        membershipService.deletePlan(id);
        return ResponseEntity.ok().build();
    }
    
    // Memberships endpoints
    @GetMapping("/memberships")
    public ResponseEntity<List<MembershipDto>> getAllMemberships() {
        return ResponseEntity.ok(membershipService.getAllMemberships());
    }
    
    @GetMapping("/memberships/{id}")
    public ResponseEntity<MembershipDto> getMembershipById(@PathVariable Long id) {
        return ResponseEntity.ok(membershipService.getMembershipById(id));
    }
    
    @GetMapping("/memberships/member/{memberId}")
    public ResponseEntity<List<MembershipDto>> getMembershipsByMemberId(@PathVariable Long memberId) {
        return ResponseEntity.ok(membershipService.getMembershipsByMemberId(memberId));
    }
    
    @PostMapping("/memberships")
    public ResponseEntity<MembershipDto> createMembership(@Valid @RequestBody CreateMembershipRequest request) {
        return ResponseEntity.ok(membershipService.createMembership(request));
    }
    
    @PutMapping("/memberships/{id}")
    public ResponseEntity<MembershipDto> updateMembership(@PathVariable Long id, @RequestBody String status) {
        return ResponseEntity.ok(membershipService.updateMembership(id, status));
    }
    
    // Payments endpoints
    @GetMapping("/payments")
    public ResponseEntity<List<PaymentDto>> getAllPayments() {
        return ResponseEntity.ok(membershipService.getAllPayments());
    }
    
    @GetMapping("/payments/{id}")
    public ResponseEntity<PaymentDto> getPaymentById(@PathVariable Long id) {
        return ResponseEntity.ok(membershipService.getPaymentById(id));
    }
    
    @PostMapping("/payments")
    public ResponseEntity<PaymentDto> createPayment(@Valid @RequestBody CreatePaymentRequest request) {
        return ResponseEntity.ok(membershipService.createPayment(request));
    }
    
    // Internal endpoints
    @GetMapping("/internal/memberships/{memberId}/validate")
    public ResponseEntity<MembershipValidationResponse> validateMembership(@PathVariable Long memberId) {
        return ResponseEntity.ok(membershipService.validateMembership(memberId));
    }
    
    @GetMapping("/internal/plans/{id}/info")
    public ResponseEntity<PlanDto> getPlanInfo(@PathVariable Long id) {
        return ResponseEntity.ok(membershipService.getPlanById(id));
    }
}
