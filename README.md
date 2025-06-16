# Data Crow Source Code (NetBeans Build Guide)

This repository contains a clone of that Data Crow application source code from the official Bitbucket repository, with a primary focus on providing detailed instructions for building and running in Apache NetBeans on Windows.

## Original Project Information

* **Project Name:**                             Data Crow
* **Official Website:**                         [https://datacrow.org/](https://datacrow.org/)
* **Original Source Repository (Bitbucket):**    [https://bitbucket.org/vanderwaalsbv/datacrow.git](https://bitbucket.org/vanderwaalsbv/datacrow.git)
* **Author:**                                   Robert Jan van der Waals

-------------------------------------------------------------------------------
## License

This project adheres to the licensing terms of the original Data Crow project. Please refer to the original project licensing [GPL v3 or later](https://www.gnu.org/licenses/gpl-3.0.html)

-------------------------------------------------------------------------------

## Building and Running Data Crow client in Apache NetBeans (Windows Guide)

### Prerequisites

1. **Git for Windows:**
   * Download and install from [https://git-scm.com/download/win](https://git-scm.com/download/win)
   or
   * Run using winget: ```winget install --id Git.Git -e --source winget```
2. **Apache Maven 3.x:**
   * You can install it via Chocolatey ```choco install maven``` or manually download and setup env variables (ensure `M2_HOME` and `%M2_HOME%\bin` in `Path`).
   * Verify by opening a **new** CMD and typing `mvn -v`.
3. **Java Development Kit(JDK) 11 LTS:**
   * **Data Crow requires JDK 11.** Although newer JDKs might be on your system, they can cause runtime issues.
   * Download an OpenJDK 11 LTS distribution for Windows (e.g., Adoptium Temurin):  [https://adoptium.net/temurin/releases/?version=11](https://adoptium.net/temurin/releases/?version=11) (download the `.msi` installer).
   * During installation, ensure the option to **"Set or override *JAVA_HOME* variable"** is selected. Also, ensure "Modify PATH variable" is enabled.
   * Verify by opening a **new** CMD and typing ```java -version```. It **must** show `openjdk version "11.0.x"`
4. **Apache NetBeans IDE:**
   * Download and install a recent version (e.g., NetBeans 19+)

-------------------------------------------------------------------------------

### Step-by-Step Guide

#### 1. Clone the Repository

```cmd
cd C:\dev\ # Or your preferred development directory
git clone [https://github.com/S-Garner/datacrow.git](https://github.com/S-Garner/datacrow
```

#### 2. Perform an Initial Maven Build (Command Line)

```cmd
cd C:\dev\datacrow\modules
mvn clean install
```

You should see `BUILD SUCCESS`

#### 3. Import the Project in NetBeans

1. Open Apache NetBeans IDE.
2. Got to `File`>`Open Project...`
3. Navigate to your cloned repository's `modules` directory (e.g., C:\dev\datacrow\modules)
4. Select the `modules` folder and click Open Project
   * NetBeans will import the multi-module Maven project, showing main-project (or modules) as the parent project.

#### 4. Configure JDK 11 in NetBeans
First, ensure NetBeans knows about your JDK 11 installation:

1. In NetBeans, go to `Tools`>`Java Platforms`.
2. Click `Add Platform...`
3. Select `Java Standard Edition` and click `Next >`.
4. Browse to your JDK 11 installation directory (e.g., `C:\Program Files\Eclipse Adoptium\jdk-11.0.x.y-hotspot`). Click `Next >` and `Finish`.
5. Close the "Java Platform Manager".

#### 5. Configure the `datacrow-client` Module's Run Action (Crucial for execution)

This is the most important step to ensure the application runs correctly, as it explicitly sets the Java executable and working directory.

1. In the NetBeans **Projects** window, expand the `main-project` node.
2. Right-click specifically on the `datacrow-client` sub module.
3. Select `Properties`.
4. Go to the `Actions` category.
5. In the "Actions" dropdown, ensure `Run project` is selected.
6. Modify the "Set Properties" section to these exact values:
   * `exec.vmArgs=` (Leave blank)
   * `exec.args=-cp C:\\dev\\datacrow\\modules\\datacrow-client\\target\\datacrow-client-5.0.1.jar;C:\\dev\\datacrow\\modules\\datacrow-build\\staging\\dc-client\\lib\\* org.datacrow.client.DataCrow`
        * **IMPORTANT:** Adjust `C:\\dev\\datacrow\\` to your actual project's root path. Note the double backslashes for Java string escaping in properties. This entire string is passed as arguments to `java.exe`.
   * `exec.appArgs=` (Leave blank)
   * DELETE `exec.classpathScope` property if present (We are manually providing classpath).
   * DELETE `exec.mainClass` property (It conflicts with main class in `exec.args`).
   * `exec.executable=C:\\dev\\datacrow\\modules\\datacrow-build\\staging\\dc-client\\java\\bin\\java.exe`
        * **IMPORTANT:** Similarly, adjust the `C:\\dev\\datacrow\\` you your actual project's root path. This points to the staged Java 11 JRE.
   * `exec.workingdir=C:\\dev\\datacrow\\modules\\datacrow-build\\staging\\dc-client`
        * This ensures the application runs from the correct directory where its resources (like icons) are located.
7. Verify "Executable Goals" is set to: `process-classes org.codehaus.mojo:exec-maven-plugin:3.1.0:exec`
8. Click `OK` to save the changes.

#### 6. Run Data Crow from NetBeans

1. In the NetBeans Projects window, right-click on the `datacrow-client` module (the node itself)
2. Select Run (or Run Project)

### Common Issues & Troubleshooting

* java.lang.IllegalArgumentException: Width (-1) and height (-1) cannot be <= 0:
   * This is typically caused by running Data Crow with an incompatible Java version (e.g., Java 17 or 21) or by the application not finding its image resources at runtime.
   * **Solution:** Ensure JDK 11 is used for *both* compilation and execution (especially the `Run` properties for `datacrow-client`), and confirm `exec.workingdir` is set as described above.

* `mvn` or `java` not recognized in CMD:
   * Environment variables (`PATH`, `JAVA_HOME`, `M2_HOME`) are not correctly set or not refreshed
   * **Solution:** Close all CMD/PowerShell windows, double-check your system environment variables, and open a new terminal.

* NetBeans isn't picking up changes:
   * **Solution:** Close NetBeans completely. Navigate to `C:\Users\<YourUsername>\AppData\Local\NetBeans\Cache\<netbeans_version>` and delete all contents of that folder. Restart NetBeans and reopen the project.

-------------------------------------------------------------------------------

### NOTICE

I will update this guide for `datacrow-server` and other modules as needed. This currently just a development/build repository, but I will be adding features and working on things once I have the process down. Let me know if you encounter any issues.

-------------------------------------------------------------------------------

*Catalogue the World!*
