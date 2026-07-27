package com.dreams.dreamscreations.controller.finance;

import com.dreams.dreamscreations.dto.finance.CreateFinancePayablePaymentRequest;
import com.dreams.dreamscreations.dto.finance.CreateFinancePayableRequest;
import com.dreams.dreamscreations.dto.finance.CreateFinanceVendorRequest;
import com.dreams.dreamscreations.dto.finance.FinancePayableDTO;
import com.dreams.dreamscreations.dto.finance.FinanceVendorDTO;
import com.dreams.dreamscreations.service.finance.FinancePayableService;
import com.dreams.dreamscreations.service.finance.FinanceVendorService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/finance")
@ConditionalOnProperty(name = "modules.finance.enabled", havingValue = "true")
public class FinancePayableController {

    private final FinanceVendorService vendorService;
    private final FinancePayableService payableService;

    public FinancePayableController(FinanceVendorService vendorService,
                                    FinancePayableService payableService) {
        this.vendorService = vendorService;
        this.payableService = payableService;
    }

    @GetMapping("/vendors")
    public ResponseEntity<List<FinanceVendorDTO>> getVendors(
            @RequestParam(defaultValue = "true") boolean activeOnly) {
        return ResponseEntity.ok(vendorService.getAll(activeOnly));
    }

    @GetMapping("/vendors/{id}")
    public ResponseEntity<FinanceVendorDTO> getVendor(@PathVariable Long id) {
        return ResponseEntity.ok(vendorService.getById(id));
    }

    @PostMapping("/vendors")
    public ResponseEntity<FinanceVendorDTO> createVendor(@RequestBody CreateFinanceVendorRequest request) {
        return ResponseEntity.ok(vendorService.create(request));
    }

    @PutMapping("/vendors/{id}")
    public ResponseEntity<FinanceVendorDTO> updateVendor(@PathVariable Long id,
                                                         @RequestBody CreateFinanceVendorRequest request) {
        return ResponseEntity.ok(vendorService.update(id, request));
    }

    @DeleteMapping("/vendors/{id}")
    public ResponseEntity<Void> deactivateVendor(@PathVariable Long id) {
        vendorService.deactivate(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/payables")
    public ResponseEntity<List<FinancePayableDTO>> getPayables() {
        return ResponseEntity.ok(payableService.getAll());
    }

    @GetMapping("/payables/{id}")
    public ResponseEntity<FinancePayableDTO> getPayable(@PathVariable Long id) {
        return ResponseEntity.ok(payableService.getById(id));
    }

    @PostMapping("/payables")
    public ResponseEntity<FinancePayableDTO> createPayable(@RequestBody CreateFinancePayableRequest request) {
        return ResponseEntity.ok(payableService.create(request));
    }

    @PostMapping("/payables/{id}/payments")
    public ResponseEntity<FinancePayableDTO> recordPayment(@PathVariable Long id,
                                                           @RequestBody CreateFinancePayablePaymentRequest request) {
        return ResponseEntity.ok(payableService.recordPayment(id, request));
    }
}
