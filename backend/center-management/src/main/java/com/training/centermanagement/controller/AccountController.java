package com.training.centermanagement.controller;

import com.training.centermanagement.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/accounts")
@CrossOrigin
public class AccountController {

    @Autowired
    private AccountService accountService;

    @GetMapping
    public List<com.training.centermanagement.entity.Account> getAllAccounts() {
        return accountService.getAllAccounts();
    }

    @PostMapping("/pay")
    public String makePayment(@RequestBody Map<String, Object> payload) {
        Integer studentId = (Integer) payload.get("studentId");
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
}