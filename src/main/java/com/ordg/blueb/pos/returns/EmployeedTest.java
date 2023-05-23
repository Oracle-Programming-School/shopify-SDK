/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ordg.blueb.pos.returns;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

public class EmployeedTest {

    public static void main(String[] args) {
        
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("com.ordg.blueb");
        EmployeedJpaController controller = new EmployeedJpaController(emf);

        // Test the create method
        EmployeedEo newEmployee = new EmployeedEo();
        newEmployee.setEmployeeId(new BigDecimal(1));
        newEmployee.setEmployeeName("John Doe");
       // newEmployee.setHiringDate(new Date());
        newEmployee.setSalary(new BigDecimal(20));
        newEmployee.setStatusFlag('A');

        try {
            controller.create(newEmployee);
            System.out.println("Created a new employee.");
        } catch (Exception e) {
            e.printStackTrace();
        }
/*
        // Test the edit method
        newEmployee.setEmployeeName("Jane Doe");

        try {
            controller.edit(newEmployee);
            System.out.println("Updated the employee.");
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Test the findEmployeed method
        EmployeedEo foundEmployee = controller.findEmployeed(new BigDecimal(1));
        System.out.println("Found employee: " + foundEmployee.getEmployeeName());

        // Test the findEmployeedEntities method
        List<EmployeedEo> employees = controller.findEmployeedEntities();
        System.out.println("Total employees: " + employees.size());

        // Test the getEmployeedCount method
        int count = controller.getEmployeedCount();
        System.out.println("Total employee count: " + count);

        // Test the destroy method
        try {
            controller.destroy(new BigDecimal(1));
            System.out.println("Deleted the employee.");
        } catch (Exception e) {
            e.printStackTrace();
        }
*/

    }

}
