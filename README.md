# Smart Inventory For Cafe (SIC)

A complete, offline-first, desktop-based Point-of-Sale (POS) and inventory management system built in Java. SIC is designed specifically to address the operational pain points of small to medium-sized cafes by providing zero-configuration setup, secure role-based access, and robust financial tracking.

## 🚀 Features

* **Zero-Configuration SQLite Database:** The system uses an embedded SQLite database (`cafe_inventory.db`) that autonomously builds its own relational schema (`CREATE TABLE IF NOT EXISTS`) upon the first launch. No complex MySQL or server setup is required.
* **Role-Based Access Control (RBAC):** Strict isolation between user types:
    * **Admin Dashboard:** Manage the menu (Standard and Trial dishes), configure stock levels, view comprehensive billing history, manage staff accounts, create discount coupons, and view real-time cafe summary analytics.
    * **Staff POS:** A streamlined checkout interface with dynamic cart updates, real-time stock depletion checks, mutual-exclusion discount logic (Staff vs. Coupon vs. Member), and automated bill generation.
    * **Member Portal:** Verified customers get access to exclusive trial dishes, automatic 10% discounts, and their personal order history.
    * **Guest Feedback:** A secure, no-login-required portal for customers to submit reviews tied to specific Bill IDs.
* **Advanced Algorithms & Data Integrity:**
    * **UUID Generation (RFC 4122):** Auto-generates globally unique IDs for bills and users to prevent primary key collisions.
    * **Timezone Enforcement:** Hardcoded `Asia/Kolkata` TimeZone mapping ensures all database timestamps accurately reflect Indian Standard Time (IST).
    * **Mutual Exclusion Discount Logic:** Prevents stacking abuse by ensuring Coupons, Staff Allowances, and Member Benefits cannot be applied simultaneously.
    * **UI Protections:** Uses a custom `NonEditableTableModel` to permanently disable accidental cell edits in all data tables.
* **Custom UI Rendering:** Bypasses standard OS-level UI limitations with a custom `StyledButton` class to ensure visual consistency and professional hover states across platforms.

## 🛠️ Prerequisites

To run this application, you will need:

1. **Java Development Kit (JDK):** Version 8 or higher installed on your machine.
2. **SQLite JDBC Driver:** The `sqlite-jdbc` JAR file is required for database connectivity.

## 📥 Installation & Setup

1. **Clone the Repository:**
   git clone [https://github.com/utkarsh-10-10/Smart-Inventory-For-Cafe.git](https://github.com/YourUsername/Smart-Inventory-For-Cafe.git)
   cd Smart-Inventory-For-Cafe
3. **Download the SQLite JDBC Driver:**

Download the latest sqlite-jdbc JAR file (e.g., sqlite-jdbc-3.45.1.0.jar) from the official GitHub releases or Maven Central.

Place the downloaded .jar file in a folder named libs inside your project directory (or directly in the root folder).

## 💻 How to Run (Terminal/Command Prompt)
Step 1: Compile the Code
Open your terminal in the project directory and compile the Java file:

Bash
javac SIC_System.java
Step 2: Execute the Application
Run the compiled code, making sure to include the SQLite JDBC driver in the classpath (-cp).

On Windows:
(Note the semicolon ; separating the current directory . and the driver path)

java -cp ".;libs\sqlite-jdbc-X.X.X.jar" SIC_System
On Mac/Linux:
(Note the colon : separating the current directory . and the driver path)

java -cp ".:libs/sqlite-jdbc-X.X.X.jar" SIC_System
(Replace X.X.X with the actual version number of the driver you downloaded).

## 🔑 Default Login Credentials
Upon the very first launch, the database will auto-generate and seed the following default accounts.

###Admin:
Username: admin
Password: admin123

###Staff:
Username: staff
Password: staff123

###Member:
Username: member
Password: member123

(It is highly recommended to log in as Admin, create new secure accounts, and delete the default staff/member accounts for security).

##📂 Project Structure
SIC_System.java: The core executable file containing all UI, business logic, and database connection utilities.

Smart_Inventory_For_Cafe_Project_Report_v2.docx: The comprehensive academic project report detailing system design, architecture, and evaluations.

Smart-Inventory-For-Cafe-SIC_6.pptx: The presentation deck designed for project evaluations and viva.

##🤝 Team
Code Wizards
