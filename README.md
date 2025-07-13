# 📅 Appointment Booking Service

A Kotlin + Spring Boot microservice to schedule appointments between clients and professionals.  
This project showcases clean RESTful design, concurrency safety, Firestore integration, and availability checking.

---

## 🚀 Features

- ✅ Create appointments with overlap prevention
- 🔍 Filter bookings by client, professional, and date
- 📄 Paginate booking results
- 🗑️ Delete bookings
- 📆 Show professional availability
- 🔥 Firebase Firestore as backend database

---

## 🛠️ Tech Stack

- **Language**: Kotlin  
- **Framework**: Spring Boot  
- **Database**: Firebase Firestore (NoSQL)  
- **Build Tool**: Gradle  
- **Cloud**: Google Cloud Service Account for local Firebase access  

---

## 📦 Setup Instructions

### 1. Clone the Repository

```bash
git clone https://github.com/BHANU9090/BookingService.git
cd BookingService
```
1. Add your Firestore credentials

❗ Do not push your service account JSON file to GitHub.

### 2. Place your ServiceAccountKey.json inside:

```css

app/src/main/resources/
Update application.properties:
```
```properties

firebase.service.account.key.path=classpath:ServiceAccoun
```
### 3. Run the project

```bash

./gradlew bootRun
```
🧪 API Endpoints
1️⃣ POST /bookings
Create a new appointment.
Returns 201 Created or 409 Conflict (if overlapping booking exists).

```json

{
  "clientId": 1,
  "professionalId": 2,
  "startTime": "2025-07-14T10:00:00",
  "endTime": "2025-07-14T11:00:00"
}
```
2️⃣ GET /bookings
List all bookings (with optional filters):

🔍 Query Parameters:
clientId (Long)
professionalId (Long)
date (yyyy-MM-dd)
page (Int)
size (Int)

✅ Example:

```bash

GET /bookings?professionalId=2&date=2025-07-14&page=0&size=5
```
3️⃣ DELETE /bookings/{id}
Delete a booking by ID.
Returns 204 No Content or 404 Not Found.

✅ Example:

```bash

DELETE /bookings/VXzOk9HXqTpcUdPmA2fU
```
4️⃣ GET /bookings/availability
Returns available time slots for a professional on a specific date.

🔍 Query Parameters:
professionalId (Long)

date (yyyy-MM-dd)

✅ Example:

```bash

GET /bookings/availability?professionalId=2&date=2025-07-14
```

