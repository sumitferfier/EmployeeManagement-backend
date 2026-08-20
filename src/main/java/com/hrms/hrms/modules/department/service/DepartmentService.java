package com.hrms.hrms.modules.department.service;

import com.hrms.hrms.common.exception.DuplicateResourceException;
import com.hrms.hrms.common.exception.ResourceNotFoundException;
import com.hrms.hrms.modules.department.dto.DepartmentRequest;
import com.hrms.hrms.modules.department.dto.DepartmentResponse;
import com.hrms.hrms.modules.department.entity.Department;
import com.hrms.hrms.modules.department.repository.DepartmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.UUID;

@Service
public class DepartmentService {

    private final DepartmentRepository departmentRepository;
    public DepartmentService(DepartmentRepository departmentRepository) {
        this.departmentRepository = departmentRepository;
    }

    // CREATE DEPARTMENT
    @Transactional
    public DepartmentResponse createDepartment(DepartmentRequest request) {

        // Clean the department name.
        String departmentName = request.getDepartmentName().trim();

        // Check whether department already exists.
        if (departmentRepository.existsByDepartmentName(departmentName)) {
            throw new DuplicateResourceException("Department already exists: " + departmentName);
        }

        // Create Department entity.
        Department department = Department.builder().departmentName(departmentName).description(request.getDescription()).build();

        // Save to PostgreSQL.
        Department savedDepartment = departmentRepository.save(department);

        // Convert entity to DTO.
        return mapToResponse(savedDepartment);
    }

    // GET ALL DEPARTMENTS
    public List<DepartmentResponse> getAllDepartments() {
        return departmentRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    // GET DEPARTMENT BY ID
    public DepartmentResponse getDepartmentById(UUID id) {
        Department department = departmentRepository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Department not found with ID: " + id));
        return mapToResponse(department);
    }

    // UPDATE DEPARTMENT
    @Transactional
    public DepartmentResponse updateDepartment(UUID id, DepartmentRequest request) {

        // Find existing department.
        Department department = departmentRepository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Department not found with ID: " + id));

        // Clean new department name.
        String departmentName = request.getDepartmentName().trim();

        // Check if another department already
        // has this name.
        departmentRepository.findByDepartmentName(departmentName)
                .ifPresent(existingDepartment -> {
                    if (!existingDepartment.getId().equals(id)) {
                        throw new DuplicateResourceException("Department already exists: " + departmentName);
                    }
                });

        // Update department.
        department.setDepartmentName(departmentName);
        department.setDescription(request.getDescription());

        // Save updated department.
        Department updatedDepartment = departmentRepository.save(department);
        return mapToResponse(updatedDepartment);
    }

    // DELETE DEPARTMENT
    @Transactional
    public void deleteDepartment(UUID id) {

        // Check whether department exists.
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with ID: " + id));


        // Delete department.
        // Later, when Employee management is implemented,
        // we will prevent deletion if employees are assigned
        // to this department.
        departmentRepository.delete(department);
    }
    // ENTITY → DTO
    private DepartmentResponse mapToResponse(
            Department department
    ) {
        return DepartmentResponse.builder()
                .id(department.getId())
                .departmentName(department.getDepartmentName())
                .description(department.getDescription())
                .build();
    }
}