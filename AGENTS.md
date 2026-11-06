# Agent Instructions & Pure Java Project Harness

## 🎯 Role & Objective
You are an expert Java developer. This project does NOT use Maven or Gradle. You will work directly with standard `javac` and `java` commands via custom automation scripts. Write clean, dependency-lean, and highly structured Java code.

## 🏗️ Architecture & Design Reference
Because we manage packages and classpaths manually, strict structure is critical:
- **System Architecture:** Refer to `ARCHITECTURE.md` for package trees and module separation.
- **Product Design:** Refer to `DESIGN.md` for feature definitions and business logic.
- *Rule:* Always check `ARCHITECTURE.md` before creating new sub-packages.

## 🛠️ Project Harness & Commands
Never run raw `javac` commands manually. Always use the project harness scripts located in the root directory. If they are missing, you are authorized to recreate them using the specifications below.

### Standard Execution Commands
- **Compile Project (Linux/macOS):** `./build.sh`
- **Run Application (Linux/macOS):** `./run.sh`
- **Compile Project (Windows):** `build.bat`
- **Run Application (Windows):** `run.bat`

---

## 📜 Harness Script Specifications

### 1. `build.sh` (Linux/macOS Compiler)
```bash
#!/bin/bash
echo "🚀 Kompiliere Java-Projekt..."
mkdir -p bin
find src -name "*.java" > sources.txt
javac -d bin @sources.txt
if [ \$? -eq 0 ]; then
    echo "✅ Build erfolgreich! Die class-Dateien liegen in /bin"
    rm sources.txt
else
    echo "❌ Build fehlgeschlagen!"
    rm sources.txt
    exit 1
fi
```

### 2. `run.sh` (Linux/macOS Runner)
```bash
#!/bin/bash
echo "🏃 Starte Anwendung..."
# Replace 'com.project.Main' with the actual fully qualified main class name
java -cp bin com.project.Main
```

### 3. `build.bat` (Windows Compiler)
```cmd
@echo off
echo 🚀 Kompiliere Java-Projekt...
if not exist bin mkdir bin
dir /s /b src\*.java > sources.txt
javac -d bin @sources.txt
if %errorlevel% equ 0 (
    echo ✅ Build erfolgreich!
    del sources.txt
) else (
    echo ❌ Build fehlgeschlagen!
    del sources.txt
    exit /b 1
)
```

### 4. `run.bat` (Windows Runner)
```cmd
@echo off
echo 🏃 Starte Anwendung...
:: Replace 'com.project.Main' with the actual fully qualified main class name
java -cp bin com.project.Main
```

---

## 🛡️ Guardrails, Git & Code Quality
1. **Compilable Code Only:** Every Java file you write must compile perfectly using the harness. Never output broken syntax or placeholder code blocks (`// TODO`).
2. **Directory & Package Alignment:** All files must start with the correct `package` declaration matching their exact folder structure inside `src/`.
3. **Git Cleanliness & Ignored Files:** 
   - The `bin/` directory contains compiled assets and must **never** be committed to Git.
   - The temporary `sources.txt` file must **never** be committed.
   - Only source code inside `src/` and documentation (`.md`) belong in GitHub Pull Requests.
4. **Zero External Dependencies:** Stick strictly to the standard Java API (java.util, java.io, etc.). Do not import external frameworks.

## 🔄 Workflow Loop
1. Read the task and write your Java classes inside the `src/` directory tree.
2. Run the harness compiler (`./build.sh` or `build.bat`).
3. Verify there are no compiler errors.
4. Execute the application (`./run.sh` or `run.bat`) to verify functionality.
5. Create a clean GitHub commit/PR containing *only* the new source files.
