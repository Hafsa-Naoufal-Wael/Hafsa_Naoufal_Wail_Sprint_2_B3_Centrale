package com.centrale.service;

import java.util.List;
import java.util.Optional;

import com.centrale.model.entity.Admin;
import com.centrale.repository.AdminRepository;

public class AdminService {

    private final AdminRepository adminRepository;

    public AdminService(AdminRepository adminRepository) {
        this.adminRepository = adminRepository;
    }

    public Admin saveAdmin(Admin admin) {
        return adminRepository.save(admin);
    }

    public Optional<Admin> getAdminById(Long id) {
        return adminRepository.findById(id);
    }

    public List<Admin> getAllAdmins() {
        return adminRepository.findAll();
    }

    public void deleteAdmin(Admin admin) {
        adminRepository.delete(admin);
    }

    public List<Admin> getAllAdminsPaginated(int page, int size) {
        return adminRepository.findAllPaginated(page, size);
    }
}