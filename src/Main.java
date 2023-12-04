
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

class Main {

    public static void main(String[] args) {
        int elementsCount = 10;
        List<Agency.Vacancy> vacancies = new ArrayList<>();
        for (int i = 0; i < elementsCount; i++) {
            vacancies.add(generateVacancy());
        }

        long startTime = System.nanoTime();
        int avgSalaryConcurrent = avgSalaryConcurrent(vacancies);
        long elapsedAvgSalaryConcurrent = System.nanoTime() - startTime;

        startTime = System.nanoTime();
        int maxSalaryConcurrent = maxSalaryConcurrent(vacancies);
        long elapsedMaxSalaryConcurrent = System.nanoTime() - startTime;

        startTime = System.nanoTime();
        int avgSalarySequential = avgSalarySequential(vacancies);
        long elapsedAvgSalarySequential = System.nanoTime() - startTime;

        startTime = System.nanoTime();
        int maxSalarySequential = maxSalarySequential(vacancies);
        long elapsedMaxSalarySequential = System.nanoTime() - startTime;

        System.out.println("\n\n\n\t\t\t\t\tConcurrent\tSequential");
        System.out.println("Max salary (ns)\t\t" + elapsedMaxSalaryConcurrent + "\t\t" + elapsedMaxSalarySequential);
        System.out.println("Avg salary (ns)\t\t" + elapsedAvgSalaryConcurrent + "\t\t" + elapsedAvgSalarySequential);
    }

    private static Agency.Vacancy generateVacancy() {
        // Assuming that WorkerRequirements and Education classes are properly defined
        // in their respective files.
        // Also assuming that the necessary imports are present.
        try {
            return new Agency.Vacancy(
                    "Company",
                    "Specialization",
                    "Conditions",
                    Integer.toString((int) (Math.random() * 10000)),
                    "IT",
                    Short.toString((short) (Math.random() * 65535)),
                    "",
                    false
            );
        }catch(Exception e){
            return null;
        }
    }

    private static int avgSalaryConcurrent(List<Agency.Vacancy> vacancies) {
        AtomicInteger sumSalaries = new AtomicInteger(0);
        List<Thread> allThreads = new ArrayList<>();

        for (Agency.Vacancy vacancy : vacancies) {
            Thread thread = new Thread(() -> sumSalaries.getAndAdd(vacancy.getSalary()));
            allThreads.add(thread);
            thread.start();
        }

        allThreads.forEach(thread -> {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        return sumSalaries.get() / vacancies.size();
    }

    private static int maxSalaryConcurrent(List<Agency.Vacancy> vacancies) {
        Lock maxSalaryLock = new ReentrantLock();
        AtomicInteger maxSalary = new AtomicInteger(0);
        List<Thread> allThreads = new ArrayList<>();

        for (Agency.Vacancy vacancy : vacancies) {
            int salary = vacancy.getSalary();
            Thread thread = new Thread(() -> {
                    if (salary > maxSalary.get()) {
                        maxSalary.set(salary);
                    }
            });
            allThreads.add(thread);
            thread.start();
        }

        allThreads.forEach(thread -> {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        return maxSalary.get();
    }

    private static int avgSalarySequential(List<Agency.Vacancy> vacancies) {
        int sumSalaries = 0;
        for (Agency.Vacancy vacancy : vacancies) {
            sumSalaries += vacancy.getSalary();
        }

        return sumSalaries / vacancies.size();
    }

    private static int maxSalarySequential(List<Agency.Vacancy> vacancies) {
        int maxSalary = 0;
        for (Agency.Vacancy vacancy : vacancies) {
            if (vacancy.getSalary() > maxSalary) {
                maxSalary = vacancy.getSalary();
            }
        }

        return maxSalary;
    }
}



class Agency {

    public static class Vacancy implements Comparable<Vacancy>, Serializable {
        private final String companyName;
        private final String specialization;
        private final String conditions;
        private final int salary;
        private final WorkerRequirements workerRequirements;

        public Vacancy(String companyName, String specialization, String conditions,
                       String salaryString, String workerSpecializationName,
                       String workExpYearsString, String educationString,boolean car)
        {
            this.companyName = companyName.trim();
            this.specialization = specialization.trim();
            this.conditions = conditions.trim();
            this.salary = parseSalary(salaryString);

            WorkerSpecialization workerSpecialization = parseWorkerSpecialization(workerSpecializationName, workExpYearsString);
            Education education = parseEducation(educationString);

            this.workerRequirements = new WorkerRequirements(workerSpecialization, education,car);
        }

        private int parseSalary(String salaryString) {
            try {
                return Integer.parseInt(salaryString.trim());
            } catch (NumberFormatException e) {
                return 0;
            }
        }

        private WorkerSpecialization parseWorkerSpecialization(String workerSpecializationName, String workExpYearsString)  {
            if (workerSpecializationName == null || workerSpecializationName.isEmpty()) {
                return null;
            }

            try {
                int workExpYears = Integer.parseInt(workExpYearsString.trim());
                return new WorkerSpecialization(workerSpecializationName.trim(), workExpYears);
            } catch (NumberFormatException e) {
                return null;
            }
        }

        private Education parseEducation(String educationString)  {
            switch (educationString.toLowerCase()) {
                case "":
                    return Education.NONE;
                case "school":
                    return Education.SCHOOL;
                case "university":
                    return Education.UNIVERSITY;
                default:
                   return null;
            }
        }

        public String getCompanyName() {
            return companyName;
        }

        public String getSpecialization() {
            return specialization;
        }

        public Education getEducation() {
            return workerRequirements.education;
        }

        public String getConditions() {
            return conditions;
        }

        public int getSalary() {
            return salary;
        }

        public WorkerRequirements getWorkerRequirements() {
            return workerRequirements;
        }

        @Override
        public String toString() {
            return String.format("%s: %s(%s) - %s$ %s car: %s", companyName, specialization, conditions, salary, workerRequirements.getEducation(),workerRequirements.car);
        }

        @Override
        public int compareTo(Vacancy other) {
            return companyName.compareTo(other.companyName);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Vacancy vacancy = (Vacancy) o;
            return salary == vacancy.salary &&
                    companyName.equals(vacancy.companyName) &&
                    specialization.equals(vacancy.specialization) &&
                    conditions.equals(vacancy.conditions) &&
                    workerRequirements.equals(vacancy.workerRequirements);
        }

        @Override
        public int hashCode() {
            return Objects.hash(companyName, specialization, conditions, salary, workerRequirements);
        }
    }

    public static class WorkerRequirements implements  Serializable {
        private final WorkerSpecialization specialization;
        private final Education education;
        private final boolean car;


        public WorkerRequirements(WorkerSpecialization specialization, Education education,boolean car) {
            this.specialization = specialization;
            this.education = education;
            this.car=car;
        }
        public WorkerRequirements(WorkerSpecialization specialization, Education education) {
            this.specialization = specialization;
            this.education = education;
            this.car=false;
        }

        public WorkerSpecialization getSpecialization() {
            return specialization;
        }

        public boolean getCar() {
            return car;
        }


        public Education getEducation() {
            return education;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            WorkerRequirements that = (WorkerRequirements) o;
            return Objects.equals(specialization, that.specialization) &&
                    education == that.education;
        }

        @Override
        public int hashCode() {
            return Objects.hash(specialization, education);
        }
    }

    public static class WorkerSpecialization implements  Serializable{
        private final String specializationName;
        private final int workExpYears;

        public WorkerSpecialization(String specializationName, int workExpYears) {
            this.specializationName = specializationName;
            this.workExpYears = workExpYears;
        }

        public String getSpecializationName() {
            return specializationName;
        }

        public int getWorkExpYears() {
            return workExpYears;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            WorkerSpecialization that = (WorkerSpecialization) o;
            return workExpYears == that.workExpYears &&
                    specializationName.equals(that.specializationName);
        }

    }

    public enum Education {
        NONE,
        SCHOOL,
        UNIVERSITY
    }


}
