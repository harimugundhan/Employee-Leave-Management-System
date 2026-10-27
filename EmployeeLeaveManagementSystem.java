import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.*;

/* ===================== ENUMS ===================== */
enum LeaveType   { CASUAL, SICK, EARNED }
enum LeaveStatus { PENDING, APPROVED, REJECTED }

/* ===================== INPUT HELPER ===================== */
class InputUtil {
    static int readInt(Scanner sc, String msg) {
        while (true) {
            System.out.print(msg);
            try { return Integer.parseInt(sc.nextLine().trim()); }
            catch (NumberFormatException e) { System.out.println("  Enter a valid number."); }
        }
    }

    static LocalDate readDate(Scanner sc, String msg) {
        while (true) {
            System.out.print(msg + " (yyyy-MM-dd): ");
            try { return LocalDate.parse(sc.nextLine().trim()); }
            catch (DateTimeParseException e) { System.out.println("  Invalid date format."); }
        }
    }

    static String readText(Scanner sc, String msg) {
        System.out.print(msg);
        return sc.nextLine().trim();
    }
}

/* ===================== ABSTRACT BASE CLASS (Abstraction + Encapsulation) ===================== */
abstract class User {
    private int userId;
    private String name;
    private String username;
    private String password;

    public User(int userId, String name, String username, String password) {
        this.userId = userId;
        this.name = name;
        this.username = username;
        this.password = password;
    }

    public int getUserId()      { return userId; }
    public String getName()     { return name; }
    public String getUsername() { return username; }
    public void setName(String name) { this.name = name; }

    public boolean login(String username, String password) {
        return this.username.equals(username) && this.password.equals(password);
    }

    // Polymorphism: each user type has its own menu
    public abstract void showMenu(Scanner sc, LeaveManagement lm);
}

/* ===================== LEAVE ===================== */
class Leave {
    private int leaveId;
    private int employeeId;
    private LeaveType leaveType;
    private LocalDate startDate;
    private LocalDate endDate;
    private String reason;
    private LeaveStatus status;

    public Leave(int leaveId, int employeeId, LeaveType leaveType,
                 LocalDate startDate, LocalDate endDate, String reason) {
        this.leaveId = leaveId;
        this.employeeId = employeeId;
        this.leaveType = leaveType;
        this.startDate = startDate;
        this.endDate = endDate;
        this.reason = reason;
        this.status = LeaveStatus.PENDING;
    }

    public int getLeaveId()         { return leaveId; }
    public int getEmployeeId()      { return employeeId; }
    public LeaveType getLeaveType() { return leaveType; }
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getEndDate()   { return endDate; }
    public String getReason()       { return reason; }
    public LeaveStatus getStatus()  { return status; }
    public void setStatus(LeaveStatus status) { this.status = status; }

    public long getDays() { return ChronoUnit.DAYS.between(startDate, endDate) + 1; }

    @Override
    public String toString() {
        return String.format("Leave#%d | Emp:%d | %-6s | %s to %s | %d day(s) | %-8s | %s",
                leaveId, employeeId, leaveType, startDate, endDate, getDays(), status, reason);
    }
}

/* ===================== EMPLOYEE (Inheritance) ===================== */
class Employee extends User {
    private String department;
    private String designation;
    private int leaveBalance;

    public Employee(int id, String name, String username, String password,
                    String department, String designation, int leaveBalance) {
        super(id, name, username, password);
        this.department = department;
        this.designation = designation;
        this.leaveBalance = leaveBalance;
    }

    public String getDepartment()  { return department; }
    public String getDesignation() { return designation; }
    public int getLeaveBalance()   { return leaveBalance; }
    public void setDepartment(String d)  { this.department = d; }
    public void setDesignation(String d) { this.designation = d; }

    public void deductBalance(int days) { this.leaveBalance -= days; }

    public void viewProfile() {
        System.out.println("\n--- PROFILE ---");
        System.out.println("ID          : " + getUserId());
        System.out.println("Name        : " + getName());
        System.out.println("Department  : " + department);
        System.out.println("Designation : " + designation);
        System.out.println("Balance     : " + leaveBalance + " day(s)");
    }

    @Override
    public void showMenu(Scanner sc, LeaveManagement lm) {
        while (true) {
            System.out.println("\n===== EMPLOYEE MENU (" + getName() + ") =====");
            System.out.println("1. View Profile");
            System.out.println("2. Apply for Leave");
            System.out.println("3. View Leave Balance");
            System.out.println("4. View Leave History / Status");
            System.out.println("0. Logout");
            int ch = InputUtil.readInt(sc, "Choice: ");
            switch (ch) {
                case 1 -> viewProfile();
                case 2 -> lm.applyLeave(sc, this);
                case 3 -> System.out.println("Leave balance: " + leaveBalance + " day(s)");
                case 4 -> lm.viewLeaveHistory(getUserId());
                case 0 -> { return; }
                default -> System.out.println("Invalid choice.");
            }
        }
    }
}

/* ===================== ADMIN (Inheritance) ===================== */
class Admin extends User {

    public Admin(int id, String name, String username, String password) {
        super(id, name, username, password);
    }

    public void approveLeave(LeaveManagement lm, int leaveId) {
        lm.processLeave(leaveId, true);
    }

    public void rejectLeave(LeaveManagement lm, int leaveId) {
        lm.processLeave(leaveId, false);
    }

    public void manageEmployees(Scanner sc, LeaveManagement lm) {
        while (true) {
            System.out.println("\n--- MANAGE EMPLOYEES ---");
            System.out.println("1. Add  2. Update  3. Delete  4. List  0. Back");
            int ch = InputUtil.readInt(sc, "Choice: ");
            switch (ch) {
                case 1 -> {
                    String name = InputUtil.readText(sc, "Name: ");
                    String user = InputUtil.readText(sc, "Username: ");
                    if (lm.usernameExists(user)) { System.out.println("Username already taken."); break; }
                    String pass = InputUtil.readText(sc, "Password: ");
                    String dept = InputUtil.readText(sc, "Department: ");
                    String desg = InputUtil.readText(sc, "Designation: ");
                    int bal = InputUtil.readInt(sc, "Initial leave balance: ");
                    lm.addEmployee(name, user, pass, dept, desg, bal);
                    System.out.println("Employee added.");
                }
                case 2 -> {
                    int id = InputUtil.readInt(sc, "Employee ID: ");
                    Employee e = lm.findEmployee(id);
                    if (e == null) { System.out.println("Employee not found."); break; }
                    e.setDepartment(InputUtil.readText(sc, "New department: "));
                    e.setDesignation(InputUtil.readText(sc, "New designation: "));
                    System.out.println("Employee updated.");
                }
                case 3 -> {
                    int id = InputUtil.readInt(sc, "Employee ID: ");
                    System.out.println(lm.deleteEmployee(id) ? "Employee deleted." : "Employee not found.");
                }
                case 4 -> lm.listEmployees();
                case 0 -> { return; }
                default -> System.out.println("Invalid choice.");
            }
        }
    }

    @Override
    public void showMenu(Scanner sc, LeaveManagement lm) {
        while (true) {
            System.out.println("\n===== ADMIN MENU (" + getName() + ") =====");
            System.out.println("1. View Pending Requests");
            System.out.println("2. Approve Leave");
            System.out.println("3. Reject Leave");
            System.out.println("4. Manage Employees");
            System.out.println("5. Reports");
            System.out.println("0. Logout");
            int ch = InputUtil.readInt(sc, "Choice: ");
            switch (ch) {
                case 1 -> lm.viewPendingLeaves();
                case 2 -> approveLeave(lm, InputUtil.readInt(sc, "Leave ID: "));
                case 3 -> rejectLeave(lm, InputUtil.readInt(sc, "Leave ID: "));
                case 4 -> manageEmployees(sc, lm);
                case 5 -> reportMenu(sc, lm);
                case 0 -> { return; }
                default -> System.out.println("Invalid choice.");
            }
        }
    }

    private void reportMenu(Scanner sc, LeaveManagement lm) {
        System.out.println("\n--- REPORTS ---");
        System.out.println("1. Employee-wise leave report");
        System.out.println("2. Approved / Rejected report");
        System.out.println("3. Leave balance report");
        int ch = InputUtil.readInt(sc, "Choice: ");
        switch (ch) {
            case 1 -> lm.viewLeaveHistory(InputUtil.readInt(sc, "Employee ID: "));
            case 2 -> lm.approvedRejectedReport();
            case 3 -> lm.balanceReport();
            default -> System.out.println("Invalid choice.");
        }
    }
}

/* ===================== LEAVE MANAGEMENT (core logic) ===================== */
class LeaveManagement {
    private final List<Employee> employees = new ArrayList<>();
    private final List<Admin> admins = new ArrayList<>();
    private final List<Leave> leaves = new ArrayList<>();
    private int nextEmpId = 101;
    private int nextLeaveId = 1;

    public LeaveManagement() {
        admins.add(new Admin(1, "System Admin", "admin", "admin123"));
        addEmployee("Ravi Kumar", "ravi", "ravi123", "IT", "Developer", 12);
        addEmployee("Priya S", "priya", "priya123", "HR", "Manager", 15);
    }

    /* ---- authentication ---- */
    public User authenticate(String username, String password, boolean adminLogin) {
        if (adminLogin) {
            for (Admin a : admins) if (a.login(username, password)) return a;
        } else {
            for (Employee e : employees) if (e.login(username, password)) return e;
        }
        return null;
    }

    /* ---- employee management ---- */
    public void addEmployee(String name, String user, String pass, String dept, String desg, int bal) {
        employees.add(new Employee(nextEmpId++, name, user, pass, dept, desg, bal));
    }

    public boolean usernameExists(String username) {
        for (Employee e : employees) if (e.getUsername().equals(username)) return true;
        return false;
    }

    public Employee findEmployee(int id) {
        for (Employee e : employees) if (e.getUserId() == id) return e;
        return null;
    }

    public boolean deleteEmployee(int id) {
        return employees.removeIf(e -> e.getUserId() == id);
    }

    public void listEmployees() {
        System.out.println("\nID   | Name            | Dept       | Designation  | Balance");
        for (Employee e : employees)
            System.out.printf("%-4d | %-15s | %-10s | %-12s | %d%n", e.getUserId(), e.getName(),
                    e.getDepartment(), e.getDesignation(), e.getLeaveBalance());
    }

    /* ---- leave operations ---- */
    public long calculateLeaveDays(LocalDate start, LocalDate end) {
        return ChronoUnit.DAYS.between(start, end) + 1;
    }

    public void applyLeave(Scanner sc, Employee emp) {
        System.out.println("Leave types: 1.CASUAL  2.SICK  3.EARNED");
        int t = InputUtil.readInt(sc, "Select type: ");
        if (t < 1 || t > 3) { System.out.println("Invalid leave type."); return; }
        LeaveType type = LeaveType.values()[t - 1];

        LocalDate start = InputUtil.readDate(sc, "Start date");
        LocalDate end   = InputUtil.readDate(sc, "End date");
        if (end.isBefore(start))            { System.out.println("End date cannot be before start date."); return; }
        if (start.isBefore(LocalDate.now())) { System.out.println("Cannot apply for past dates."); return; }

        long days = calculateLeaveDays(start, end);
        if (days > emp.getLeaveBalance()) {
            System.out.println("Insufficient balance. Requested " + days + ", available " + emp.getLeaveBalance());
            return;
        }
        String reason = InputUtil.readText(sc, "Reason: ");
        Leave l = new Leave(nextLeaveId++, emp.getUserId(), type, start, end, reason);
        leaves.add(l);
        System.out.println("Leave applied. Leave ID = " + l.getLeaveId() + " (PENDING)");
    }

    public void processLeave(int leaveId, boolean approve) {
        Leave l = findLeave(leaveId);
        if (l == null) { System.out.println("Leave not found."); return; }
        if (l.getStatus() != LeaveStatus.PENDING) { System.out.println("Already " + l.getStatus()); return; }

        if (approve) {
            Employee e = findEmployee(l.getEmployeeId());
            if (e == null) { System.out.println("Employee no longer exists."); return; }
            if (l.getDays() > e.getLeaveBalance()) {
                System.out.println("Cannot approve: employee balance is insufficient.");
                return;
            }
            updateLeaveBalance(e, (int) l.getDays());
            l.setStatus(LeaveStatus.APPROVED);
            System.out.println("Leave approved. Balance updated.");
        } else {
            l.setStatus(LeaveStatus.REJECTED);
            System.out.println("Leave rejected.");
        }
    }

    public void updateLeaveBalance(Employee e, int days) {
        e.deductBalance(days);
    }

    private Leave findLeave(int id) {
        for (Leave l : leaves) if (l.getLeaveId() == id) return l;
        return null;
    }

    /* ---- views & reports ---- */
    public void viewLeaveHistory(int employeeId) {
        boolean found = false;
        for (Leave l : leaves)
            if (l.getEmployeeId() == employeeId) { System.out.println(l); found = true; }
        if (!found) System.out.println("No leave records.");
    }

    public void viewPendingLeaves() {
        boolean found = false;
        for (Leave l : leaves)
            if (l.getStatus() == LeaveStatus.PENDING) { System.out.println(l); found = true; }
        if (!found) System.out.println("No pending requests.");
    }

    public void approvedRejectedReport() {
        boolean found = false;
        for (Leave l : leaves)
            if (l.getStatus() != LeaveStatus.PENDING) { System.out.println(l); found = true; }
        if (!found) System.out.println("No processed requests.");
    }

    public void balanceReport() {
        System.out.println("\nEmpID | Name            | Balance");
        for (Employee e : employees)
            System.out.printf("%-5d | %-15s | %d%n", e.getUserId(), e.getName(), e.getLeaveBalance());
    }
}

/* ===================== MAIN ===================== */
public class EmployeeLeaveManagementSystem {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        LeaveManagement lm = new LeaveManagement();

        while (true) {
            System.out.println("\n===== EMPLOYEE LEAVE MANAGEMENT SYSTEM =====");
            System.out.println("1. Employee Login");
            System.out.println("2. Admin Login");
            System.out.println("0. Exit");
            int ch = InputUtil.readInt(sc, "Choice: ");

            if (ch == 0) { System.out.println("Goodbye!"); break; }
            if (ch != 1 && ch != 2) { System.out.println("Invalid choice."); continue; }

            String u = InputUtil.readText(sc, "Username: ");
            String p = InputUtil.readText(sc, "Password: ");
            User user = lm.authenticate(u, p, ch == 2);

            if (user == null) System.out.println("Invalid credentials.");
            else user.showMenu(sc, lm);   // Polymorphic call
        }
        sc.close();
    }
}