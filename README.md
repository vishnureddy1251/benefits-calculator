# Employee Benefits Calculator - Spring Boot

A REST API for managing employees, dependents, and calculating payroll benefits. Migrated from .NET 6 to Spring Boot 3.

## 🚀 Features

- Employee CRUD operations
- Dependent management with business rules
- Payroll calculation with benefits deductions
- Pagination support
- Swagger/OpenAPI documentation
- SQLite database
- Input validation
- Comprehensive error handling

## 📋 Prerequisites

- Java 17 or higher
- Maven 3.6+
- Git

## 🛠️ Tech Stack

- **Framework:** Spring Boot 3.2.1
- **Database:** SQLite with JPA/Hibernate
- **Documentation:** SpringDoc OpenAPI (Swagger)
- **Build Tool:** Maven
- **Java Version:** 17

## 📦 Installation

### 1. Clone the repository
```bash
git clone https://github.com/your-username/benefits-calculator-springboot.git
cd benefits-calculator-springboot
```

### 2. Build the project
```bash
mvn clean install
```

### 3. Run the application
```bash
mvn spring-boot:run
```

The application will start on `http://localhost:5124`

## 📚 API Documentation

Once the application is running, visit:
- **Swagger UI:** http://localhost:5124/swagger-ui.html
- **OpenAPI JSON:** http://localhost:5124/api-docs

## 🔗 API Endpoints

### Employees
- `GET /api/v1/employees` - Get all employees (paginated)
- `GET /api/v1/employees/{id}` - Get employee by ID
- `POST /api/v1/employees` - Create new employee
- `PUT /api/v1/employees` - Update employee
- `DELETE /api/v1/employees/{id}` - Delete employee
- `DELETE /api/v1/employees/list` - Delete multiple employees
- `GET /api/v1/employees/{id}/dependents` - Get employee's dependents
- `DELETE /api/v1/employees/{id}/dependents` - Delete employee's dependents
- `GET /api/v1/employees/{id}/payperiod` - Get employee paycheck

### Dependents
- `GET /api/v1/dependents` - Get all dependents (paginated)
- `GET /api/v1/dependents/{id}` - Get dependent by ID
- `POST /api/v1/dependents` - Create new dependent
- `PUT /api/v1/dependents` - Update dependent
- `DELETE /api/v1/dependents/{id}` - Delete dependent

## 🎯 Business Rules

### Benefits Calculation
- **Employee Base Cost:** $1,000 per month
- **Dependent Cost (under 50):** $600 per month
- **Dependent Cost (over 50):** $800 per month
- **High Salary Surcharge:** Additional 2% annual deduction for salaries > $80,000
- **Payment Schedule:** 26 paychecks per year (bi-weekly)

### Validation Rules
- An employee can have only ONE spouse or domestic partner
- Employees must have valid dates of birth
- All monetary values must be positive

## 📁 Project Structure
```
src/main/java/com/paylocity/benefits_calculator/
├── config/              # Configuration classes
├── controller/          # REST controllers
├── dto/                 # Data Transfer Objects
│   └── request/         # Request models
├── entity/              # JPA entities
├── enums/               # Enum types
├── exception/           # Custom exceptions
├── repository/          # JPA repositories
├── service/             # Business logic
│   └── impl/            # Service implementations
└── util/                # Utility classes
```

## 🧪 Testing

Run all tests:
```bash
mvn test
```

## 📝 Migration Progress

This project is being migrated from .NET to Spring Boot following a 20-day plan.
See `MIGRATION_PLAN.md` for details.

## 📄 License

This project is licensed under the MIT License.

---

**Note:** This is a migration from the original .NET 6 Benefits Calculator application to Spring Boot 3.
```

6. **Save** (Ctrl+S)

---

### **File 2: MIGRATION_PLAN.md**

1. **Right-click** on root → **New → File**
2. Name it: `MIGRATION_PLAN.md`
3. Copy the **entire MIGRATION_PLAN.md content** I provided earlier (it's very long - about 500+ lines)
4. Save

---

### **File 3: DAY_1_QUICK_START.md**

1. **Right-click** on root → **New → File**
2. Name it: `DAY_1_QUICK_START.md`
3. Copy the **DAY_1_QUICK_START.md content** I provided
4. Save

---

## **Quick Visual Guide:**

### **Where to Create Documentation Files:**
```
benefits-calculator/                    👈 RIGHT-CLICK HERE
├── .idea/
├── .mvn/
├── src/
├── .gitattributes
├── .gitignore                         👈 UPDATE THIS
├── HELP.md
├── mvnw
├── mvnw.cmd
├── pom.xml
├── README.md                          👈 CREATE THIS
├── MIGRATION_PLAN.md                  👈 CREATE THIS
└── DAY_1_QUICK_START.md              👈 CREATE THIS