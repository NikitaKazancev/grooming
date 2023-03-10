package ru.nk.grooming.documents.salary;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.nk.grooming.authentication.routes.components.AuthService;
import ru.nk.grooming.components.employees.EmployeeRepo;
import ru.nk.grooming.components.salons.SalonEntity;
import ru.nk.grooming.components.salons.SalonRepo;
import ru.nk.grooming.documents.salary.dto.SalaryEmployee;
import ru.nk.grooming.documents.salary.dto.SalaryEmployeeFullData;
import ru.nk.grooming.documents.salary.dto.SalaryFullData;
import ru.nk.grooming.general.date.DateService;
import ru.nk.grooming.general.requests.ServiceFunctions;
import ru.nk.grooming.types.ResponseWithStatus;
import ru.nk.grooming.types.StatusCode;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SalaryService {
    private final ServiceFunctions functions;
    private final AuthService authService;
    private final SalaryRepo salaryRepo;
    private final SalonRepo salonRepo;
    private final EmployeeRepo employeeRepo;

    public boolean fieldsNotExist(SalaryEntity salary) {
        SalonEntity salon = salonRepo.findById(salary.getSalonId()).orElse(null);
        if (salon == null) {
            return true;
        }

        return false;
    }
    public ResponseWithStatus<List<SalaryEntity>> findAll(HttpServletRequest request) {
        return functions.findAllWithAuth(
                salaryRepo::findAll,
                request
        );
    }
    public ResponseWithStatus<SalaryFullData> findById(Long id, HttpServletRequest request) {
        if (authService.isNotAdmin(request)) {
            return ResponseWithStatus.empty(403);
        }

        SalaryEntity salary = salaryRepo.findById(id).orElse(null);
        if (salary == null) {
            return ResponseWithStatus.empty(404);
        }

        SalaryFullData res = SalaryFullData.builder()
                .month(salary.getMonth())
                .salon(salonRepo.findById(salary.getSalonId()).orElse(null))
                .id(salary.getId())
                .employees(new ArrayList<>())
                .build();

        for (SalaryEmployee salaryEmployees : salary.getEmployees()) {
            res.getEmployees().add(
                    new SalaryEmployeeFullData(
                            employeeRepo.findById(salaryEmployees.getEmployeeId()).orElse(null),
                            salaryEmployees.getSalary()
                    )
            );
        }

        return ResponseWithStatus.create(200, res);
    }
    public StatusCode save(SalaryEntity salary, HttpServletRequest request) {
        salary.setMonth(DateService.startOfMonth(new Date()));

        return functions.saveWithCheckFieldsWithAuth(
                salary,
                this::fieldsNotExist,
                salary.getMonth(),
                salary.getSalonId(),
                salaryRepo::findByMonthAndSalonId,
                salaryRepo::save,
                request
        );
    }
    public StatusCode deleteById(Long id, HttpServletRequest request) {
        return functions.deleteByWithAuth(
                id,
                salaryRepo::findById,
                salaryRepo::deleteById,
                request
        );
    }
}
