package com.infosys.dummy.engine;

import java.util.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * This is a dummy class
 */
public class EmployeeSearch {
    public static final String IT = "IT";
    public static final String HR = "Human Resources";
    public static final String SALES = "Sales";
    public static final String AC = "Accounting";

    public static void main(String[] args) {
        Employee[] employees = {
                new Employee("Al", AC),
                new Employee("Bob", IT),
                new Employee("Hank", HR),
                new Employee("Bill", IT),
                new Employee("Harry", HR),
                new Employee("Barry", IT),
                new Employee("Henry", HR),
                new Employee("Allan", AC),
                new Employee("Betty", IT),
                new Employee("Arnie", AC),
                new Employee("Alice", AC),
                new Employee("Si", SALES),
                new Employee("Albert", AC),
                new Employee("Alyssa", AC),
                new Employee("Harriet", HR),
                new Employee("Atticus", AC),
                new Employee("Sally", SALES),
                new Employee("Sherry", SALES),
        };

        Map<String, List<Employee>> eMap = getEmployeesOrderedByDepartment(Arrays.asList(employees));
        for (String dept: eMap.keySet()) {
            System.out.printf("Department: %s%n  Employees:%s%n%n", dept,
                    Arrays.toString(eMap.get(dept).stream().map(Employee::getName).toArray()));
        }
    }

    public static Map<String, List<Employee>> getEmployeesOrderedByDepartment(Collection<Employee> employees) {
        Map<String, List<Employee>> employeeMap = new HashMap<>();
        employees.forEach(employee ->
            employeeMap
                    .computeIfAbsent(employee.getDepartment(), k -> new LinkedList<>())
                    .add(employee)
        );
        return employeeMap;
    }

    public static Map<String, List<Employee>> getEmployeesByTheirDepartment(Collection<Employee> employees) {
        Map<String, List<Employee>> employeeMap = new HashMap<>();
        employees.forEach(employee -> {
            String dpt = employee.getDepartment();
            List<Employee> employeeList = employeeMap.computeIfAbsent(dpt, k -> new LinkedList<>());
            employeeList.add(employee);
        });
        return employeeMap;
    }

    public static Map<String, List<Employee>> getEmployeeByDepartment(List<Employee> employees) {
        Set<String> departmentSet = employees.stream()
                .map(Employee::getDepartment)
                .collect(Collectors.toSet());
        Map<String, List<Employee>> eMap = new HashMap<>();
        for (String dept: employees.stream()
                .map(Employee::getDepartment)
                .collect(Collectors.toSet())) {
            List<Employee> eList = new LinkedList<>();
            eMap.put(dept, eList);
        }
        for (Employee employee: employees) {
            eMap.get(employee.getDepartment()).add(employee);
        }
        return eMap;

//        Collector<Employee, ?, Map<String, Employee>> employeeMapCollector
//                = Collectors.toMap(Employee::getDepartment, e -> {
//            return e;
//        });
//        return employees.stream()
//                .collect(employeeMapCollector);
////        Set<String> departmentSet = employees.stream()
////                .map(Employee::getDepartment)
////                .collect(Collectors.toSet());
////
////        Map<String, Employee> finalMap = new HashMap<>();
////            List<Map<String, Employee>> finalList = new LinkedList<>();
////            employees.stream().flatMap()
////                    .map(Employee::getDepartment)
////                    .forEach();
////            Collectors.toMap(Employee::getDepartment, Employee::getName);
//        }
    }

    public static Map<String, List<Employee>> getAllEmployeesOrderedByDepartment(Collection<Employee> employees) {
        Map<String, List<Employee>> employeeMap = new HashMap<>();
        employees.forEach(employee ->
                employeeMap
                        .computeIfAbsent(employee.getDepartment(), k -> new LinkedList<>())
                        .add(employee)
        );
        return employeeMap;
    }

    public static class Employee {
        public final String name;
        public final String department;
        Employee(String name, String department) {
            this.name = name;
            this.department = department;
        }
        public String getDepartment() { return department; }
        public String getName() { return name; }

        @Override
        public String toString() {
            return String.format("%s: %s", department, name);
        }
    }
}
