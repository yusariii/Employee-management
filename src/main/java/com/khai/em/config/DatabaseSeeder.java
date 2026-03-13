package com.khai.em.config; 

import com.khai.em.entity.Employee;
import com.khai.em.entity.Role;
import com.khai.em.entity.User;
import com.khai.em.repository.EmployeeRepository;
import com.khai.em.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DatabaseSeeder implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Kiểm tra xem trong DB đã có tài khoản nào chưa
        if (userRepository.count() == 0) {
            System.out.println("🌱 Database trống! Bắt đầu gieo hạt dữ liệu Admin đầu tiên...");

            // ADMIN
            Employee adminEmployee = new Employee();
            adminEmployee.setName("Le Nguyen Khai");
            adminEmployee.setDepartment("GOD");
            adminEmployee.setSalary(1000000.0);
            employeeRepository.save(adminEmployee);

            User adminUser = new User();
            adminUser.setUsername("admin");
            adminUser.setPassword(passwordEncoder.encode("admin123")); 
            adminUser.setEmployee(adminEmployee);
            adminUser.setRole(Role.ADMIN);
            userRepository.save(adminUser);

            //MANAGER
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
            userRepository.save(managerUser);

             //EMPLOYEE
             Employee employee = new Employee();
             employee.setName("Tran Thi B");
             employee.setDepartment("SALES");
             employee.setSalary(500000.0);
             employeeRepository.save(employee);

             User employeeUser = new User();
             employeeUser.setUsername("employee");
             employeeUser.setPassword(passwordEncoder.encode("employee123"));
             employeeUser.setEmployee(employee);
             employeeUser.setRole(Role.EMPLOYEE);
             userRepository.save(employeeUser);

            System.out.println("✅ Đã tạo thành công tài khoản mặc định: username: admin | password: admin123");
        } else {
            System.out.println("🌳 Database đã có dữ liệu. Bỏ qua bước gieo hạt.");
        }
    }
}