package com.hrms.hrms.modules.department.dto;

import lombok.*;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DepartmentResponse {

    private UUID id;  // Department ID
    private String departmentName;  // Department name
    private String description;  // Department description

}