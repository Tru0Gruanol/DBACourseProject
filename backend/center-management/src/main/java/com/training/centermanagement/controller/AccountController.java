package com.training.centermanagement.controller;

import com.training.centermanagement.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    @Autowired
    private AccountService accountService;

    @GetMapping
    public List<com.training.centermanagement.entity.Account> getAllAccounts() {
        return accountService.getAllAccounts();
    }

    @PostMapping("/pay")
    public String makePayment(@RequestBody Map<String, Object> payload) {
        Integer studentId = Integer.parseInt(payload.get("studentId").toString());
        String classCode = (String) payload.get("classCode");
        BigDecimal amount = new BigDecimal(payload.get("amount").toString());
        return accountService.makePayment(studentId, classCode, amount);
    }

    @GetMapping("/invoice")
    public Map<String, Object> getInvoice(@RequestParam Integer studentId, @RequestParam String classCode) {
        return accountService.getInvoice(studentId, classCode);
    }

    @GetMapping("/debtors")
    public List<Map<String, Object>> getDebtors() {
        return accountService.getDebtors();
    }

    @PostMapping("/refund")
    public String refund(@RequestBody Map<String, Object> payload) {
        Integer studentId = Integer.parseInt(payload.get("studentId").toString());
        String classCode = (String) payload.get("classCode");
        BigDecimal amount = new BigDecimal(payload.get("amount").toString());
        return accountService.refund(studentId, classCode, amount);
    }

    @GetMapping("/totalPaid")
    public BigDecimal getTotalPaid(@RequestParam Integer studentId, @RequestParam String classCode) {
        return accountService.getTotalPaid(studentId, classCode);
    }

    @GetMapping("/student-summary")
    public Map<String, Object> getStudentSummary(@RequestParam Integer studentId) {
        return accountService.getStudentSummary(studentId);
    }
}