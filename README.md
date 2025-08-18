# ⚡ Crypto Analytics Backend (Spring Boot)

This is the **backend service** for the Crypto Analytics Dashboard.  
It provides APIs for market data, snapshots, trending coins, predictions, user authentication, and notifications.  

Built with **Spring Boot**, **MongoDB**, **JWT Security**, and **Python Prophet** for coin predictions.  

---

## ✨ Features

- 🔐 JWT-based authentication & user management  
- 📊 Coin snapshots, trending coins & gainers API  
- 📈 Coin prediction service (powered by **Prophet**)  
- 🔔 Notifications & market metrics  
- 🖼️ Cloudinary integration for image uploads  
- ⏱️ Cron jobs for scheduled updates  

---

## ⚙️ Setup

### 1. Clone the repository
```bash
git clone https://github.com/your-username/crypto-analytics-backend.git
cd crypto-analytics-backend
```
### 2. Configure environment & properties

Go to src/main/resources/ and update configuration files:

application.properties → database, security, and app configs

Make sure you set your MongoDB connection and other secrets before running.

### 3. Build and run with Maven
```bash
./mvnw clean install
./mvnw spring-boot:run
```

Windows users:
```bash
mvnw.cmd spring-boot:run
```

The backend will start on:
```bash
http://localhost:8080
```

📦 Deployment

To build a production JAR:
```bash
./mvnw clean package
```

Run it:
```bash
java -jar target/crypto-analytics-0.0.1-SNAPSHOT.jar
```

When hosting (Heroku, Railway, VPS, etc.), update your .env and application.properties accordingly.
Use the hosted backend URL in your frontend .env as:
```bash
VITE_API_BASE_URL=https://your-hosted-backend-link
```

