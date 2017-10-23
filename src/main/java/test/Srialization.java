package test;

import java.awt.event.ComponentAdapter;
import java.io.*;

/**
 * This program uses Java's default Srialization/deserialization methods
 * to operate on self-defined class.
 *
 * Created by john on 2/7/17.
 */
public class Srialization {

    private static class Employee implements Serializable {
        public String name;
        public String address;
        public int SSN;
        public int number;
        public transient Company company;

        public Employee() {
            this.name = "Default";
            this.address = "Default";
            this.SSN = 0;
            this.number = 0;
            this.company = new Company();
        }

        private void writeObject(ObjectOutputStream out) throws IOException {
            out.defaultWriteObject();

            if (company == null) company = new Company();
            out.writeObject(company.name);
            out.writeObject(company.regNumber);

        }
        private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
            in.defaultReadObject();

            String name = (String)in.readObject();
            int regNumber = (int)in.readObject();

            this.company = new Company(name, regNumber);
        }
    }

    private static class Company {
        public String name;
        public int regNumber;

        public Company() {
            this.name = "Default";
            this.regNumber = 0;
        }
        public Company(String name, int regNumber) {
            this.name = name;
            this.regNumber = regNumber;
        }
    }

    public static void main(String[] args) {
        Company company = new Company();
        company.name = "ADSC";
        company.regNumber = 123456;

        Employee employee = new Employee();
        employee.name = "Reyan Ali";
        employee.address = "Phokka Kuan, Ambehta Peer";
        employee.SSN = 654321;
        employee.number = 111111;
        employee.company = company;

        try {
            FileOutputStream fos = new FileOutputStream("employee.ser");
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(employee);
            oos.close();
            fos.close();
            System.out.println("Serialized data is saved in employee.ser");
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }

        Employee employeeNew = null;
        try {
            FileInputStream fis = new FileInputStream("employee.ser");
            ObjectInputStream ois = new ObjectInputStream(fis);
            employeeNew = (Employee)ois.readObject();
            ois.close();
            fis.close();

            System.out.println("Deserialized Employee:");
            System.out.println("Name: " + employeeNew.name);
            System.out.println("Adress: " + employeeNew.address);
            System.out.println("SSN: " + employeeNew.SSN);
            System.out.println("Number: " + employeeNew.number);
            System.out.println("Company's name: " + employeeNew.company.name);
            System.out.println("Company's regNumber: " + employeeNew.company.regNumber);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}