package com.khai.em.config;

import com.khai.em.entity.Employee;
import com.khai.em.entity.Role;
import com.khai.em.entity.User;
import com.khai.em.repository.EmployeeRepository;
import com.khai.em.repository.UserRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DatabaseSeeder implements CommandLineRunner {

    private final UserRepository userRepository;

    private final EmployeeRepository employeeRepository;

    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.count() == 0) {
            System.out.println("🌱 Database trống! Bắt đầu gieo hạt dữ liệu Admin đầu tiên...");

            Employee adminEmployee = new Employee();
            adminEmployee.setName("Le Nguyen Khai");
            adminEmployee.setDepartment("GOD");
            adminEmployee.setSalary(1000000.0);
            employeeRepository.save(adminEmployee);

            User adminUser = new User();
            adminUser.setUsername("admin");
            adminUser.setPassword(passwordEncoder.encode("admin123"));
            adminUser.setEmployee(adminEmployee);
            adminUser.setEmail("admin@gmail.com");
            adminUser.setRole(Role.ADMIN);
            userRepository.save(adminUser);

            Employee managerEmployee = new Employee();
            managerEmployee.setName("Nguyen Van A");
            managerEmployee.setDepartment("MANAGEMENT");
            managerEmployee.setSalary(800000.0);
            employeeRepository.save(managerEmployee);

            User managerUser = new User();
            managerUser.setUsername("manager");
            managerUser.setPassword(passwordEncoder.encode("manager123"));
            managerUser.setEmployee(managerEmployee);
            managerUser.setRole(Role.MANAGER);
            managerUser.setEmail("manager@gmail.com");
            userRepository.save(managerUser);

            Employee employee = new Employee();
            employee.setName("Tran Thi B");
            employee.setDepartment("SALES");
            employee.setSalary(500000.0);
            employeeRepository.save(employee);

            User employeeUser = new User();
            employeeUser.setUsername("employee");
            employeeUser.setPassword(passwordEncoder.encode("employee123"));
            employeeUser.setEmployee(employee);
            employeeUser.setEmail("employee@gmail.com");
            employeeUser.setRole(Role.EMPLOYEE);
            userRepository.save(employeeUser);

            System.out.println("username: admin | password: admin123");
            System.out.println("username: manager | password: manager123");
            System.out.println("username: employee | password: employee123");
        } else {
            System.out.println("Database đã có dữ liệu!");
        }
    }
}