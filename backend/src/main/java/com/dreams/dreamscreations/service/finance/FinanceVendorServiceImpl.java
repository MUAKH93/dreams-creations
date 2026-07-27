package com.dreams.dreamscreations.service.finance;

import com.dreams.dreamscreations.dto.finance.CreateFinanceVendorRequest;
import com.dreams.dreamscreations.dto.finance.FinanceVendorDTO;
import com.dreams.dreamscreations.entity.finance.FinanceVendor;
import com.dreams.dreamscreations.repository.finance.FinanceVendorRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@ConditionalOnProperty(name = "modules.finance.enabled", havingValue = "true")
public class FinanceVendorServiceImpl implements FinanceVendorService {

    private final FinanceVendorRepository vendorRepo;

    public FinanceVendorServiceImpl(FinanceVendorRepository vendorRepo) {
        this.vendorRepo = vendorRepo;
    }

    @Override
    @Transactional(readOnly = true)
    public List<FinanceVendorDTO> getAll(boolean activeOnly) {
        List<FinanceVendor> vendors = activeOnly
                ? vendorRepo.findByIsActiveTrueOrderByVendorNameAsc()
                : vendorRepo.findAllByOrderByVendorNameAsc();
        return vendors.stream().map(this::toDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public FinanceVendorDTO getById(Long id) {
        return toDto(requireVendor(id));
    }

    @Override
    @Transactional
    public FinanceVendorDTO create(CreateFinanceVendorRequest request) {
        validateName(request);
        FinanceVendor vendor = FinanceVendor.builder()
                .vendorName(request.getVendorName().trim())
                .phone(trimOrNull(request.getPhone()))
                .email(trimOrNull(request.getEmail()))
                .notes(trimOrNull(request.getNotes()))
                .isActive(true)
                .build();
        return toDto(vendorRepo.save(vendor));
    }

    @Override
    @Transactional
    public FinanceVendorDTO update(Long id, CreateFinanceVendorRequest request) {
        validateName(request);
        FinanceVendor vendor = requireVendor(id);
        vendor.setVendorName(request.getVendorName().trim());
        vendor.setPhone(trimOrNull(request.getPhone()));
        vendor.setEmail(trimOrNull(request.getEmail()));
        vendor.setNotes(trimOrNull(request.getNotes()));
        return toDto(vendorRepo.save(vendor));
    }

    @Override
    @Transactional
    public void deactivate(Long id) {
        FinanceVendor vendor = requireVendor(id);
        vendor.setIsActive(false);
        vendorRepo.save(vendor);
    }

    private FinanceVendor requireVendor(Long id) {
        return vendorRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Vendor not found: " + id));
    }

    private void validateName(CreateFinanceVendorRequest request) {
        if (request == null || request.getVendorName() == null || request.getVendorName().isBlank()) {
            throw new RuntimeException("Vendor name is required");
        }
    }

    private String trimOrNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private FinanceVendorDTO toDto(FinanceVendor vendor) {
        return FinanceVendorDTO.builder()
                .vendorId(vendor.getVendorId())
                .vendorName(vendor.getVendorName())
                .phone(vendor.getPhone())
                .email(vendor.getEmail())
                .notes(vendor.getNotes())
                .isActive(vendor.getIsActive())
                .build();
    }
}
