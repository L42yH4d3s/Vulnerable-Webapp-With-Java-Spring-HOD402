# Vulnerable-Webapp-With-Java-Spring-HOD402

## Description
This is a sample web application built with Java Spring Boot, designed for educational and security testing purposes. The project intentionally contains multiple common web vulnerabilities (SQL Injection, Insecure Deserialization, Path Traversal, XXE, SSTI, etc.) for hands-on learning and demonstration.

## Main Features
- User authentication and authorization (USER, SELLER, ADMIN roles)
- Product management (add, edit, delete, view)
- User management (ADMIN only)
- Integrated Spring Boot Admin for monitoring
- Several endpoints intentionally left vulnerable for security practice (e.g., Blind SQL Injection, XXE, Deserialization, SSTI)

## Directory Structure
```
Vulnerable-Webapp-With-Java-Spring-HOD402/
├── admin-server/                # Spring Boot Admin Server (monitoring)
├── web-app-client-demo/         # Main vulnerable web application
├── Picture/                     # Images for documentation/demo
├── blind_sql_injection_exploit.py # SQLi exploit script
├── decrypt_password.py          # Password hash cracking script
├── wordlist.txt                 # Wordlist for brute-force
├── users_data.txt               # Extracted user data
├── decrypted_results.txt        # Cracked password results
├── .gitignore                   # Git ignore rules
├── README.md                    # This documentation file
└── ...
```

## Vulnerable Endpoints

### 1. **Blind SQL Injection**
- `/products/search?keyword=...`  
  *Vulnerability: Boolean-based Blind SQLi*

### 2. **XXE (XML External Entity Injection)**
- `POST /seller/revenue/upload`  
  *Vulnerability: XXE via XML file upload (see CVE-2017-12629, CVE-2017-12628 for reference)*

### 3. **Java Deserialization**
- `POST /users/{id}/preferences/import`
  *Vulnerability: Insecure Java Deserialization via base64-encoded data parameter (see CVE-2015-4852, CVE-2017-9805 for reference)*

### 4. **SSTI (Server-Side Template Injection)**
- `/admin-server/notifications/preview?template=...`  
  *Vulnerability: SSTI in Spring Boot Admin context (see CVE-2019-3799 for Spring-related SSTI)*

*Note: Endpoint paths may vary. Please check the actual controller code for the exact mapping in your project.*

## Installation & Usage
### 1. Requirements
- Java 8u76
- Maven 3.6+
- MariaDB/MySQL (or H2 for testing)

### 2. Database Setup
- Edit DB connection info in `web-app-client-demo/src/main/resources/application.properties` or related config files.
- Import sample data if needed (`data-dev.sql`, `data-prod.sql`).

### 3. Build & Run
```bash
# Start admin-server (optional, for monitoring)
cd admin-server
mvn spring-boot:run

# Start web-app-client-demo (main vulnerable app)
cd ../web-app-client-demo
mvn spring-boot:run
```
- Access the app at: [http://localhost:8083](http://localhost:8083)
- Access admin server at: [http://localhost:8081](http://localhost:8081)

### 4. Exploiting Vulnerabilities
- Use `blind_sql_injection_exploit.py` to exploit the SQLi demo.
- See `users_data.txt` and `decrypted_results.txt` for sample extracted and cracked data.
- For XXE, Deserialization, and SSTI, craft appropriate payloads and send to the endpoints listed above.

## Security Notice
**This project is intentionally vulnerable and should only be used for educational or testing purposes in a safe environment. Do NOT deploy to production!**

## Contribution
Contributions, bug reports, and suggestions are welcome! Please open an issue or pull request on this GitHub repository.

---
**Author:** HOD402 Security Course 