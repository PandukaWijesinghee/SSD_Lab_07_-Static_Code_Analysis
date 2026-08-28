# Secure Software Development Lab 07 – Vulnerability Detection and Remediation

## 1. Overview

This lab demonstrates the identification and remediation of common security vulnerabilities in the Java Servlet-based `dbsample` web application.

The application contains:
- User login functionality
- Product search functionality
- MySQL database connectivity

Static analysis was performed using **QApug / FindBugs** in IntelliJ IDEA.

The initial analysis identified **9 problems**. The issues were fixed and the application was analyzed again. The final analysis reported **0 problems**.

---

## 2. Objectives

The objectives of this lab were to:

1. Identify vulnerabilities using static code analysis.
2. Understand the causes and impact of the identified vulnerabilities.
3. Fix SQL Injection vulnerabilities.
4. Remove hardcoded database credentials.
5. Properly manage database resources.
6. Re-run QApug / FindBugs after remediation.
7. Verify that the final analysis reports zero problems.

---

## 3. Technologies and Tools

| Technology / Tool | Purpose |
|---|---|
| Java | Application development |
| Java Servlets | Web application backend |
| MySQL | Database |
| Apache Tomcat | Application server |
| IntelliJ IDEA | Development environment |
| QApug / FindBugs | Static analysis |
| Git / GitHub | Version control |

---

## 4. Main Source Files

```text
dbsample/
├── src/
│   └── main/
│       └── java/
│           ├── Login.java
│           ├── Search.java
│           └── SearchPost.java
├── target/
├── pom.xml
└── README.md
```

### `Login.java`

Handles user authentication using the `users` database table.

### `Search.java`

Handles product searching using the `products` table.

### `SearchPost.java`

Handles POST-based product searching.

---

# 5. Initial Static Analysis

The first QApug / FindBugs analysis identified **9 problems**.

| Finding | Count | Affected Classes |
|---|---:|---|
| Hardcoded constant database password | 3 | Login, Search, SearchPost |
| Nonconstant string passed to SQL execution | 3 | Login, Search, SearchPost |
| Method may fail to close database resource | 3 | Login, Search, SearchPost |
| **Total** | **9** | |

---

# 6. Vulnerability 1 – SQL Injection

## Description

The original application constructed SQL statements by concatenating user input.

Example from the original `Login.java`:

```java
String sql = "SELECT * FROM users WHERE username='" + username +
             "' AND password='" + password + "'";
```

The `username` and `password` values came from HTTP requests and were directly inserted into the SQL statement.

The product search servlets had the same type of problem.

## Security Impact

SQL Injection can allow an attacker to manipulate a SQL query and potentially:

- Bypass authentication
- Read unauthorized data
- Modify database records
- Delete database records
- Access sensitive information

## Remediation

The vulnerable `Statement` implementation was replaced with `PreparedStatement`.

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

For product searching:

```java
String sql = "SELECT product_name, product_price " +
             "FROM products WHERE product_name=?";

PreparedStatement st = conn.prepareStatement(sql);

st.setString(1, product_name);
```

Using parameterized queries separates SQL commands from user-supplied values.

---

# 7. Vulnerability 2 – Hardcoded Database Password

## Description

The original application stored the database password directly in source code:

```java
DriverManager.getConnection(
    "jdbc:mysql://localhost:3306/webapp",
    "root",
    "mypassword"
);
```

This exposes sensitive credentials to anyone who can access the source code.

## Remediation

The password was removed from the source code and replaced with an environment variable:

```java
String dbPassword = System.getenv("DB_PASSWORD");
```

The database connection now uses:

```java
Connection conn = DriverManager.getConnection(
    "jdbc:mysql://localhost:3306/webapp",
    "root",
    dbPassword
);
```

### Important

**Never commit the actual `DB_PASSWORD` value to GitHub.**

---

# 8. Vulnerability 3 – Database Resource Management

## Description

The initial implementation did not consistently guarantee that database resources would be closed when exceptions occurred.

QApug / FindBugs reported:

```text
Bad practice - Method may fail to close database resource
```

This finding occurred in:

- `Login.java`
- `Search.java`
- `SearchPost.java`

## Remediation

The application was changed to use Java **try-with-resources**.

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
        while (rs.next()) {
            // Process results
        }
    }
}
```

This automatically closes:

- `Connection`
- `PreparedStatement`
- `ResultSet`

and prevents database resource leaks.

---

# 9. Environment Variable Configuration

The application expects the database password to be available through:

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

After using `setx`, restart IntelliJ IDEA.

## IntelliJ IDEA / Tomcat

The environment variable can also be added to the Tomcat Run/Debug Configuration:

```text
DB_PASSWORD=YOUR_DATABASE_PASSWORD
```

Do not place the real password in this README.

---

# 10. Final Secure Implementation

The final implementation applies three main security improvements.

## Login

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

## Search

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

# 11. Verification

After applying the fixes:

1. The project was rebuilt.
2. QApug / FindBugs analysis was run again.
3. The final analysis results were checked.

The final result was:

```text
Problems found: 0
FindBugs: 0
```

This confirms that the previously detected findings were no longer reported by the static analysis.

---

# 12. Initial vs Final Results

| Stage | Problems Found | Result |
|---|---:|---|
| Initial analysis | 9 | Vulnerabilities detected |
| After SQL Injection fixes | 6 | SQL-related findings resolved |
| After database password fixes | 5 | Hardcoded password findings reduced |
| After resource management fixes | **0** | **All reported findings resolved** |

---

# 13. Evidence / Screenshots

The following screenshots can be included in the lab report.

### Figure 1 – Initial QApug / FindBugs Analysis

Show the initial analysis containing the identified findings.

**Caption:**

> Figure 1: Initial QApug/FindBugs analysis identifying security and database resource management issues.

### Figure 2 – Original `Login.java`

Show the original SQL query containing direct user-input concatenation.

**Caption:**

> Figure 2: Original Login.java code vulnerable to SQL Injection.

### Figure 3 – Original Database Connection

Show the hardcoded database password.

**Caption:**

> Figure 3: Original database connection containing a hardcoded database password.

### Figure 4 – Fixed `Login.java`

Show `PreparedStatement` and `System.getenv("DB_PASSWORD")`.

**Caption:**

> Figure 4: Remediated Login.java using PreparedStatement and an environment variable for the database password.

### Figure 5 – Fixed `Search.java`

Show the parameterized SQL query and resource handling.

**Caption:**

> Figure 5: Remediated Search.java using a parameterized SQL query and automatic resource management.

### Figure 6 – Fixed `SearchPost.java`

Show the parameterized query and try-with-resources.

**Caption:**

> Figure 6: Remediated SearchPost.java with secure SQL and automatic resource management.

### Figure 7 – Final QApug / FindBugs Analysis

Show:

```text
Problems found: 0
FindBugs: 0
```

**Caption:**

> Figure 7: Final QApug/FindBugs analysis showing zero reported problems after remediation.

---

# 14. Testing Checklist

- [ ] Application starts successfully on Tomcat.
- [ ] Valid login credentials work.
- [ ] Invalid login credentials are rejected.
- [ ] Product search works.
- [ ] POST product search works.
- [ ] Database connection works using `DB_PASSWORD`.
- [ ] Database password is not stored in source code.
- [ ] SQL queries use `PreparedStatement`.
- [ ] Database resources use try-with-resources.
- [ ] QApug / FindBugs reports zero problems.

---

# 15. GitHub Security Checklist

Before pushing the project to GitHub:

- [ ] Do not commit the real database password.
- [ ] Do not include passwords in Java source files.
- [ ] Do not include passwords in screenshots.
- [ ] Do not commit sensitive IntelliJ/Tomcat configuration.
- [ ] Check the repository for accidentally exposed credentials.

Suggested `.gitignore`:

```gitignore
.idea/
target/
*.iml
```

---

# 16. Conclusion

This lab demonstrated the process of identifying, fixing, and verifying security vulnerabilities in a Java web application.

The initial QApug / FindBugs analysis reported **9 problems**. The major findings were:

1. SQL Injection vulnerabilities.
2. Hardcoded database passwords.
3. Improper database resource management.

The issues were remediated by:

- Using `PreparedStatement` for parameterized SQL queries.
- Moving the database password to the `DB_PASSWORD` environment variable.
- Using try-with-resources for `Connection`, `PreparedStatement`, and `ResultSet`.

After the fixes were applied, the application was analyzed again.

The final result was:

```text
Problems found: 0
FindBugs: 0
```

Therefore, all problems identified by the initial QApug / FindBugs analysis were successfully resolved.

---

## 17. Final Result

```text
Initial Analysis
       |
       v
9 Problems Found
       |
       +----> SQL Injection ----------------> Fixed
       |
       +----> Hardcoded DB Password --------> Fixed
       |
       +----> Database Resource Management -> Fixed
       |
       v
Final QApug / FindBugs Analysis
       |
       v
Problems Found: 0
FindBugs: 0
```

**Lab Status: COMPLETED**
