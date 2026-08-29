# Secure Software Development Lab 07 – Vulnerability Detection and Remediation

## 1. Overview

This lab focuses on identifying and remediating security vulnerabilities in the Java Servlet-based `dbsample` web application.

The application includes:

- User login functionality
- Product search functionality
- MySQL database connectivity

Static code analysis was performed using the IntelliJ IDEA **QApug / FindBugs** plugin.

The initial analysis identified **9 problems**. The identified issues were remediated and the application was analyzed again.

---

## 2. Objectives

The objectives of this lab are to:

1. Identify security issues using static code analysis.
2. Understand the causes and impact of the identified issues.
3. Fix SQL Injection vulnerabilities.
4. Remove hardcoded database credentials.
5. Properly manage database resources.
6. Perform dependency vulnerability analysis using OWASP Dependency-Check.
7. Re-run static analysis after remediation.
8. Verify the final analysis results.

---

## 3. Technologies and Tools

| Technology / Tool | Purpose |
|---|---|
| Java | Application development |
| Java Servlets | Web application backend |
| MySQL | Database |
| Apache Tomcat | Application server |
| IntelliJ IDEA 2024.2.6 | Development environment |
| QApug / FindBugs | Static code analysis |
| OWASP Dependency-Check | Dependency vulnerability analysis |
| Maven | Build and dependency management |
| Git / GitHub | Version control |

---

## 4. Project Structure

```text
dbsample/
├── src/
│   └── main/
│       ├── java/
│       │   ├── Login.java
│       │   ├── Search.java
│       │   └── SearchPost.java
│       └── webapp/
├── pom.xml
└── README.md
```

### Main Java Classes

- **Login.java** – Handles user authentication.
- **Search.java** – Handles product searching.
- **SearchPost.java** – Handles POST-based product searching.

---

# 5. Initial Static Analysis

The initial QApug / FindBugs analysis identified **9 problems**.

| Finding | Count | Affected Classes |
|---|---:|---|
| Hardcoded database password | 3 | Login, Search, SearchPost |
| Nonconstant string passed to SQL execution | 3 | Login, Search, SearchPost |
| Database resource may not be closed | 3 | Login, Search, SearchPost |
| **Total** | **9** | |

These findings were reviewed and the corresponding source code was modified.

---

# 6. Vulnerability 1 – SQL Injection

## Description

The original application constructed SQL queries by directly concatenating user input.

Example:

```java
String sql = "SELECT * FROM users WHERE username='" + username +
             "' AND password='" + password + "'";
```

The values received from the HTTP request were inserted directly into the SQL statement.

## Security Impact

SQL Injection may allow an attacker to manipulate database queries and potentially:

- Bypass authentication
- Read unauthorized information
- Modify database records
- Delete database records

## Remediation

The vulnerable `Statement` approach was replaced with `PreparedStatement`.

### Before

```java
String sql = "SELECT * FROM users WHERE username='" + username +
             "' AND password='" + password + "'";

Statement st = conn.createStatement();
ResultSet rs = st.executeQuery(sql);
```

### After

```java
String sql = "SELECT * FROM users WHERE username=? AND password=?";

PreparedStatement st = conn.prepareStatement(sql);

st.setString(1, username);
st.setString(2, password);

ResultSet rs = st.executeQuery();
```

Parameterized queries prevent user input from being interpreted as part of the SQL command.

The same approach was applied to the product search functionality.

---

# 7. Vulnerability 2 – Hardcoded Database Password

## Description

The original source code contained the database password directly in the connection code.

```java
DriverManager.getConnection(
    "jdbc:mysql://localhost:3306/webapp",
    "root",
    "mypassword"
);
```

Hardcoded credentials can expose sensitive information if the source code is shared or uploaded to a repository.

## Remediation

The password was removed from the Java source code and retrieved from an environment variable.

```java
String dbPassword = System.getenv("DB_PASSWORD");
```

The connection uses:

```java
Connection conn = DriverManager.getConnection(
    "jdbc:mysql://localhost:3306/webapp",
    "root",
    dbPassword
);
```

### Security Note

**Do not commit the actual database password to GitHub or include it in this README.**

---

# 8. Vulnerability 3 – Database Resource Management

## Description

The initial implementation did not consistently guarantee that database resources were closed when exceptions occurred.

The static analysis identified resource-management problems in:

- `Login.java`
- `Search.java`
- `SearchPost.java`

## Remediation

The database code was changed to use **try-with-resources**.

Example:

```java
try (Connection conn = DriverManager.getConnection(
        "jdbc:mysql://localhost:3306/webapp",
        "root",
        dbPassword
);
     PreparedStatement st = conn.prepareStatement(sql)) {

    st.setString(1, product_name);

    try (ResultSet rs = st.executeQuery()) {
        // Process results
    }
}
```

This allows Java to automatically close the database resources.

---

# 9. Environment Variable Configuration

The application expects the database password to be stored in:

```text
DB_PASSWORD
```

## Windows Command Prompt

For the current terminal session:

```cmd
set DB_PASSWORD=YOUR_DATABASE_PASSWORD
```

For a persistent user environment variable:

```cmd
setx DB_PASSWORD "YOUR_DATABASE_PASSWORD"
```

After using `setx`, restart IntelliJ IDEA if the application is run from IntelliJ.

## IntelliJ IDEA / Tomcat

The environment variable can also be configured in the Tomcat Run/Debug Configuration:

```text
DB_PASSWORD=YOUR_DATABASE_PASSWORD
```

The real password must not be committed to the repository.

---

# 10. OWASP Dependency-Check

OWASP Dependency-Check was added to the Maven project to identify known vulnerabilities in third-party dependencies.

The configured plugin version is:

```xml
<plugin>
    <groupId>org.owasp</groupId>
    <artifactId>dependency-check-maven</artifactId>
    <version>12.1.0</version>
</plugin>
```

The plugin should be placed inside the Maven `<build><plugins>` section, not directly inside `<dependencies>`.

## Run Dependency-Check

From the project directory:

```cmd
mvn org.owasp:dependency-check-maven:check
```

The report is normally generated under:

```text
target/dependency-check-report.html
```

Other report formats may also be generated depending on the Dependency-Check configuration.

### Java Version Requirement

Dependency-Check 12.1.0 requires a newer Java runtime than Java 8.

If Maven reports an error similar to:

```text
class file version 55.0
this version of the Java Runtime only recognizes class file versions up to 52.0
```

it means Maven is running with **Java 8**, while the Dependency-Check plugin requires **Java 11 or newer**.

Check the Java version used by Maven:

```cmd
mvn -version
```

If necessary, configure Maven/IntelliJ to use a compatible JDK and run the command again.

---

# 11. IntelliJ IDEA Static Analysis Reports

After running the QApug / FindBugs analysis in IntelliJ IDEA, the results can be archived as evidence for the lab.

Recommended evidence includes:

1. Initial analysis showing the detected problems.
2. Source code showing the vulnerable implementation.
3. Source code showing the fixed implementation.
4. Final analysis showing the remediated results.

The exact report/export options depend on the installed QApug / FindBugs plugin version.

---

# 12. Dependency Vulnerability Report

After successfully running Dependency-Check, locate:

```text
target/
└── dependency-check-report.html
```

Open the HTML report in a browser and use it as evidence in the lab report.

For GitHub submission, the generated report can be archived in a suitable project folder if required by the lab instructions.

Example:

```text
reports/
└── dependency-check-report.html
```

Do not include passwords, API keys, or other secrets in archived reports.

---

# 13. Final Secure Implementation

The final implementation applies the following security improvements:

### Login

```java
String dbPassword = System.getenv("DB_PASSWORD");

String sql = "SELECT * FROM users WHERE username=? AND password=?";

try (Connection conn = DriverManager.getConnection(
        "jdbc:mysql://localhost:3306/webapp",
        "root",
        dbPassword
);
     PreparedStatement st = conn.prepareStatement(sql)) {

    st.setString(1, username);
    st.setString(2, password);

    try (ResultSet rs = st.executeQuery()) {
        // Login processing
    }
}
```

### Search

```java
String sql = "SELECT product_name, product_price " +
             "FROM products WHERE product_name=?";

try (Connection conn = DriverManager.getConnection(
        "jdbc:mysql://localhost:3306/webapp",
        "root",
        dbPassword
);
     PreparedStatement st = conn.prepareStatement(sql)) {

    st.setString(1, product_name);

    try (ResultSet rs = st.executeQuery()) {
        // Search processing
    }
}
```

The same security improvements were applied to `SearchPost.java`.

---

# 14. Verification

After applying the fixes:

1. The application was rebuilt.
2. QApug / FindBugs analysis was executed again.
3. The final static-analysis result was checked.
4. OWASP Dependency-Check was configured/executed for dependency vulnerability analysis.
5. The generated reports were reviewed.

The final static-analysis result was:

```text
Problems found: 0
FindBugs: 0
```

This confirms that the issues reported by the initial static analysis were addressed.

---

# 15. Initial vs Final Results

| Stage | Result |
|---|---|
| Initial QApug / FindBugs analysis | 9 problems |
| SQL Injection remediation | SQL-related findings addressed |
| Hardcoded password remediation | Credential findings addressed |
| Resource-management remediation | Resource findings addressed |
| Final QApug / FindBugs analysis | **0 problems** |
| OWASP Dependency-Check | Dependency vulnerability report generated after successful execution |

> Note: The exact Dependency-Check vulnerability count should be taken from the generated HTML report rather than assumed from the static-analysis result.

---

# 16. Recommended Screenshots for the Lab Report

Only include screenshots that are relevant to the lab tasks.

### Screenshot 01 – Initial Static Analysis

Show the QApug / FindBugs window containing the initial findings.

**Caption:**

> Figure 1: Initial static analysis showing the identified problems.

### Screenshot 02 – Vulnerable Login Code

Show the original SQL query using string concatenation.

**Caption:**

> Figure 2: Original Login.java containing the SQL Injection vulnerability.

### Screenshot 03 – Hardcoded Password

Show the original database connection containing the hardcoded password.

**Caption:**

> Figure 3: Original database connection containing a hardcoded database password.

### Screenshot 04 – Fixed Login Code

Show `PreparedStatement` and `DB_PASSWORD`.

**Caption:**

> Figure 4: Remediated Login.java using parameterized SQL and an environment variable.

### Screenshot 05 – Fixed Search Code

Show the parameterized SQL query and resource handling.

**Caption:**

> Figure 5: Remediated Search.java using PreparedStatement and try-with-resources.

### Screenshot 06 – Fixed SearchPost Code

Show the secure implementation.

**Caption:**

> Figure 6: Remediated SearchPost.java.

### Screenshot 07 – Final Static Analysis

Show the final QApug / FindBugs result.

```text
Problems found: 0
FindBugs: 0
```

**Caption:**

> Figure 7: Final static analysis showing zero reported problems.

### Screenshot 08 – Dependency-Check Report

Show the generated `dependency-check-report.html`.

**Caption:**

> Figure 8: OWASP Dependency-Check vulnerability report.

---

# 17. Testing Checklist

- [ ] Application starts successfully on Tomcat.
- [ ] Valid login works.
- [ ] Invalid login is rejected.
- [ ] Product search works.
- [ ] POST product search works.
- [ ] Database connection works using `DB_PASSWORD`.
- [ ] Database password is not stored in Java source code.
- [ ] SQL queries use `PreparedStatement`.
- [ ] Database resources use try-with-resources.
- [ ] QApug / FindBugs analysis completes.
- [ ] Final QApug / FindBugs result shows 0 problems.
- [ ] OWASP Dependency-Check report is generated successfully.
- [ ] No real passwords or secrets are committed to GitHub.

---

# 18. GitHub Security Checklist

Before pushing the project to GitHub:

- [ ] Remove hardcoded passwords.
- [ ] Do not commit the real `DB_PASSWORD`.
- [ ] Do not include passwords in screenshots.
- [ ] Do not commit sensitive IntelliJ/Tomcat configuration.
- [ ] Review the repository for accidentally exposed credentials.

Suggested `.gitignore` entries:

```gitignore
.idea/
target/
*.iml
```

If Dependency-Check reports are required as submission evidence, archive only the required report files in a dedicated folder rather than committing the entire `target` directory.

---

# 19. Conclusion

This lab demonstrated the identification and remediation of security vulnerabilities in a Java Servlet-based web application.

The initial static analysis reported **9 problems**, involving:

1. SQL Injection.
2. Hardcoded database credentials.
3. Database resource-management issues.

The vulnerabilities were addressed using:

- `PreparedStatement` for parameterized SQL queries.
- The `DB_PASSWORD` environment variable for database credentials.
- Try-with-resources for automatic database resource management.

OWASP Dependency-Check was also configured to analyze third-party dependencies for known vulnerabilities.

After remediation, the final QApug / FindBugs analysis reported:

```text
Problems found: 0
FindBugs: 0
```

The Dependency-Check HTML report should be reviewed separately for the dependency vulnerability results.

---

## 20. Final Status

```text
Initial Static Analysis
        |
        v
9 Problems Found
        |
        +----> SQL Injection ----------------> Fixed
        |
        +----> Hardcoded DB Password --------> Fixed
        |
        +----> Resource Management ----------> Fixed
        |
        v
Final Static Analysis
        |
        v
Problems Found: 0
        |
        v
OWASP Dependency-Check
        |
        v
Dependency Vulnerability Report
```

**Lab Status: COMPLETED**
