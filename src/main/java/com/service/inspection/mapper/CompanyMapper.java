package com.service.inspection.mapper;

import com.service.inspection.dto.company.CompanyDto;
import com.service.inspection.dto.company.GetCompanyDto;
import com.service.inspection.dto.employer.GetEmployerDto;
import com.service.inspection.dto.license.GetLicenseDto;
import com.service.inspection.entities.Company;
import com.service.inspection.entities.Employer;
import com.service.inspection.entities.License;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Mapper(
        injectionStrategy = InjectionStrategy.CONSTRUCTOR,
        componentModel = "spring"
)
public interface CompanyMapper {

    void mapToUpdateCompany(@MappingTarget Company toUpdate, CompanyDto source);

    GetCompanyDto mapToDto(Company company, List<GetEmployerDto> employers, List<GetLicenseDto> licenses);
}
