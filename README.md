# 🚀 Running the Spring Boot 3 Application

This project uses **Spring Boot 3** and the **Gradle Wrapper** for simple build and run operations.

---

## 🧰 Requirements

* **Java 21 (JDK 21)**
* **Gradle Wrapper** (`gradlew` and `gradlew.bat` included)

### Verify Java Installation

```bash
java -version
# Expected output:
# openjdk version "21" ...
```

If not Java 21, update your `JAVA_HOME` to point to a JDK 21 installation and ensure `$JAVA_HOME/bin` is first in your `PATH`.

---

## ⚙️ Build the Application

Before running, perform a clean build:

### macOS / Linux

```bash
chmod +x gradlew
./gradlew clean build
```

### Windows (PowerShell or CMD)

```bat
gradlew clean build
```

This command:

* Cleans previous builds
* Compiles and tests your code
* Packages the application under `build/libs/`

---

## ▶️ Run the Application

You can start the application in one of two ways:

### Option 1: Using `bootRun`

```bash
./gradlew clean bootRun
```

### Option 2: Running the built JAR

```bash
java -jar build/libs/<your-app-name>-<version>.jar
```

When running, the application is available at:
👉 **[http://localhost:8080](http://localhost:8080)**

Stop it anytime with **Ctrl + C**.

---

## 🧪 Test the Endpoint

### Test Variables

| Variable     | Value         |
| ------------ | ------------- |
| `userId`     | `test_user_1` |
| `cardNumber` | `123456789`   |
| `action`     | `block`       |

### Example cURL Command

```bash
curl -X GET "http://localhost:8080/cards/123456789?action=block&userId=test_user_1"
```

You should see a JSON or text response confirming the action or card details.

---

## ⚠️ Common Issues

### 🔹 Wrong Java Version

If you see:

```
Unsupported class file major version ...
```

Then Java 21 is not active.

**macOS / Linux:**

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
export PATH="$JAVA_HOME/bin:$PATH"
```

**Windows (PowerShell):**

```powershell
$env:JAVA_HOME="C:\Program Files\Java\jdk-21"
$env:Path="$env:JAVA_HOME\bin;$env:Path"
```

---

### 🔹 Port Already in Use

If port `8080` is busy, specify another port:

```bash
./gradlew bootRun --args='--server.port=8081'
```

---

## ✅ Summary

| Command                                                                               | Description                               |
| ------------------------------------------------------------------------------------- | ----------------------------------------- |
| `./gradlew clean build`                                                               | Clean, compile, test, and package the app |
| `./gradlew clean bootRun`                                                             | Run directly from source                  |
| `java -jar build/libs/<your-app>.jar`                                                 | Run the packaged JAR                      |
| `curl -X GET "http://localhost:8080/cards/123456789?action=block&userId=test_user_1"` | Test endpoint                             |
