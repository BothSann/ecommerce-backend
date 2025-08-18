# 🛒 E-commerce Backend API

A complete **Spring Boot 3.5.4** REST API for an e-commerce application featuring user authentication, product management, order processing, admin controls, and a robust custom ID generation system with Firebase Realtime Database integration.

## 🚀 Features

### Core Functionality
- **User Authentication**: Session-based authentication with role management (CUSTOMER/ADMIN)
- **Product Management**: Full CRUD operations for products with admin controls
- **Order Processing**: Order creation, tracking, and status management
- **Admin Dashboard**: Comprehensive admin controls for users, products, and orders
- **Custom ID Generation**: Sequential, formatted IDs (USR-0001, PRD-0001, ORD-0001)

### Security
- Session-based authentication with Spring Security
- Role-based access control (RBAC)
- CORS configuration for cross-origin requests
- Password encryption with BCrypt

### Database
- Firebase Realtime Database integration
- Real-time data synchronization
- Atomic transactions for ID generation
- Scalable NoSQL document storage

## 📋 Prerequisites

- **Java**: JDK 17 or higher
- **Maven**: 3.6+ (or use included `./mvnw`)
- **Firebase**: Valid Firebase project with Realtime Database enabled
- **Environment**: Windows/Linux/macOS
- **IDE**: IntelliJ IDEA, VS Code, or Eclipse (optional but recommended)

## 🛠️ Installation & Setup

### 1. Clone the Repository
```bash
git clone <repository-url>
cd ecommerce-app-backend
```

### 2. Firebase Realtime Database Setup

#### Step 1: Create Firebase Project
1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Click "Add project" and follow the setup wizard
3. Enable **Realtime Database** in the Firebase console:
   - Go to "Build" → "Realtime Database"
   - Click "Create Database"
   - Choose "Start in test mode" for development
   - Select your preferred region (e.g., `asia-southeast1`)

#### Step 2: Generate Service Account Key
1. In Firebase Console, go to **Project Settings** → **Service Accounts**
2. Click "Generate new private key"
3. Download the JSON file
4. **Rename it to `firebase-service-account.json`**
5. **Place it in `src/main/resources/` directory**

#### Step 3: Configure Database URL
Update `src/main/resources/application.properties` with your database URL:
```properties
# Firebase Configuration - Replace with your database URL
firebase.database.url=https://YOUR-PROJECT-ID-default-rtdb.REGION.firebasedatabase.app/

# Example:
# firebase.database.url=https://ecommerce-app-12345-default-rtdb.asia-southeast1.firebasedatabase.app/
```

#### Step 4: Database Security Rules (Development)
In Firebase Console → Realtime Database → Rules, set:
```json
{
  "rules": {
    ".read": "auth != null",
    ".write": "auth != null"
  }
}
```

**⚠️ Important:** This configuration uses service account authentication, so the above rules are for additional security. For production, implement more restrictive rules.

#### Step 5: Initialize Database Structure (Optional)
The application will automatically create the required structure, but you can pre-populate:
```json
{
  "counters": {
    "users": 0,
    "products": 0,
    "orders": 0
  },
  "users": {},
  "products": {},
  "orders": {}
}
```

### 3. Build and Run

#### Using Maven Wrapper (Recommended)
```bash
# Clean and compile
./mvnw clean compile

# Run tests to verify setup
./mvnw test

# Start the application
./mvnw spring-boot:run
```

#### Using System Maven
```bash
mvn clean compile
mvn test
mvn spring-boot:run
```

#### Verify Installation
```bash
# Check if server is running
curl http://localhost:8080/api/products

# Expected response: [] (empty array)
```

**🎉 The API will be available at `http://localhost:8080/api`**

## 📚 API Documentation

### Base URLs
- **Local Development**: `http://localhost:8080/api`
- **Android Emulator**: `http://10.0.2.2:8080/api`
- **Physical Device**: `http://192.168.x.x:8080/api` (replace with your local IP)

### Authentication Endpoints

#### Register Customer
```http
POST /api/auth/register
Content-Type: application/json

{
  "username": "john_doe",
  "email": "john@example.com",
  "password": "password123"
}
```

**Response:**
```json
{
  "success": true,
  "user": {
    "id": "USR-0001",
    "username": "john_doe",
    "email": "john@example.com",
    "role": "CUSTOMER"
  },
  "message": "User registered and logged in successfully",
  "sessionId": "session-id"
}
```

#### Register Admin
```http
POST /api/auth/register-admin
Content-Type: application/json

{
  "username": "admin",
  "email": "admin@example.com",
  "password": "admin123"
}
```

#### Login
```http
POST /api/auth/login
Content-Type: application/json

{
  "email": "john@example.com",
  "password": "password123"
}
```

#### Logout
```http
POST /api/auth/logout
```

#### Session Status
```http
GET /api/auth/session-status
```

### Product Endpoints (Public)

#### Get All Products
```http
GET /api/products
```

#### Get Product by ID
```http
GET /api/products/{id}
```

### Order Endpoints (Customer - Authentication Required)

#### Create Order
```http
POST /api/orders
Content-Type: application/json

{
  "userId": "USR-0001",
  "items": [
    {
      "productId": "PRD-0001",
      "productName": "iPhone 15",
      "quantity": 1,
      "price": 999.99
    }
  ],
  "totalAmount": 999.99
}
```

#### Get My Orders
```http
GET /api/orders/my-orders
```

### Admin Endpoints (Admin Role Required)

#### Product Management
```http
GET /api/admin/products          # Get all products
POST /api/admin/products         # Create product
GET /api/admin/products/{id}     # Get product by ID
PUT /api/admin/products/{id}     # Update product
DELETE /api/admin/products/{id}  # Delete product
```

#### User Management
```http
GET /api/admin/users                              # Get all users
PUT /api/admin/users/{userId}/role?role=ADMIN     # Update user role
DELETE /api/admin/users/{userId}                  # Delete user
```

#### Order Management
```http
GET /api/admin/orders                    # Get all orders
GET /api/admin/orders/{orderId}          # Get order details
PUT /api/admin/orders/{orderId}/status   # Update order status
```

## 🆔 Custom ID Generation System

### ID Formats
- **Users**: `USR-0001`, `USR-0002`, `USR-0003`, ...
- **Products**: `PRD-0001`, `PRD-0002`, `PRD-0003`, ...
- **Orders**: `ORD-0001`, `ORD-0002`, `ORD-0003`, ...

### Features
- **Sequential Numbering**: Automatic increment with zero-padding
- **Thread-Safe**: Firebase transactions ensure atomic operations
- **Persistent Counters**: Counter values stored in `/counters/` path
- **Format Consistency**: 4-digit zero-padded numbers (expandable to 5+ digits)

### Database Structure
```
Firebase Realtime Database:
├── users/
│   ├── USR-0001: { user data }
│   └── USR-0002: { user data }
├── products/
│   ├── PRD-0001: { product data }
│   └── PRD-0002: { product data }
├── orders/
│   ├── ORD-0001: { order data }
│   └── ORD-0002: { order data }
└── counters/
    ├── users: 2
    ├── products: 2
    └── orders: 2
```

## 📦 Order Status Management

### Available Statuses
| Status | Description | Actions Available |
|--------|-------------|------------------|
| `PENDING` | Default when order is created | Admin can confirm |
| `CONFIRMED` | Admin has confirmed the order | Admin can mark as shipped |
| `SHIPPED` | Order has been shipped | Admin can mark as delivered |
| `DELIVERED` | Order delivered (final status) | No further actions |

### Status Transition Rules
```
PENDING → CONFIRMED ✅
CONFIRMED → SHIPPED ✅  
SHIPPED → DELIVERED ✅

❌ Invalid Transitions:
- PENDING → SHIPPED (must go through CONFIRMED)
- CONFIRMED → DELIVERED (must go through SHIPPED)
- DELIVERED → any status (final state)
- Any backward transitions
```

### Order Status API Response
```json
{
  "id": "ORD-0001",
  "userId": "USR-0001",
  "status": "PENDING",
  "items": [...],
  "totalAmount": 999.99,
  "createdAt": "2024-01-15T10:30:00",
  "updatedAt": "2024-01-15T10:30:00"
}
```

## 🧪 Testing

### Run Tests
```bash
./mvnw test
```

### Manual Testing with cURL

#### 1. Register and Test Users
```bash
# Register customer
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"customer","email":"customer@example.com","password":"password123"}'

# Register admin  
curl -X POST http://localhost:8080/api/auth/register-admin \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","email":"admin@example.com","password":"admin123"}'
```

#### 2. Admin Product Management
```bash
# Login as admin (save cookies)
curl -c cookies.txt -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@example.com","password":"admin123"}'

# Create product
curl -b cookies.txt -X POST http://localhost:8080/api/admin/products \
  -H "Content-Type: application/json" \
  -d '{"name":"iPhone 15","description":"Latest iPhone","price":999.99,"quantity":50,"imageUrl":"https://example.com/iphone.jpg"}'
```

#### 3. Customer Order Creation
```bash
# Login as customer
curl -c customer_cookies.txt -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"customer@example.com","password":"password123"}'

# Create order
curl -b customer_cookies.txt -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{"userId":"USR-0001","items":[{"productId":"PRD-0001","productName":"iPhone 15","quantity":1,"price":999.99}],"totalAmount":999.99}'
```

## 🏗️ Project Structure

```
src/
├── main/java/com/ecommerce/app/
│   ├── config/
│   │   ├── FirebaseConfig.java       # Firebase configuration
│   │   └── SecurityConfig.java       # Spring Security configuration
│   ├── controller/
│   │   ├── AdminController.java      # Admin endpoints
│   │   ├── AuthController.java       # Authentication endpoints
│   │   ├── OrderController.java      # Order management
│   │   └── ProductController.java    # Product endpoints
│   ├── dto/
│   │   ├── LoginRequest.java         # Request DTOs
│   │   ├── RegisterRequest.java
│   │   ├── ProductRequest.java
│   │   └── OrderStatusUpdateRequest.java
│   ├── model/
│   │   ├── User.java                 # Domain models
│   │   ├── Product.java
│   │   ├── Order.java
│   │   ├── OrderItem.java
│   │   ├── OrderStatus.java
│   │   └── IdCounter.java
│   ├── security/
│   │   └── AuthService.java          # Authentication service
│   ├── service/
│   │   ├── FirebaseService.java      # Firebase operations
│   │   └── IdGeneratorService.java   # Custom ID generation
│   └── EcommerceAppApplication.java  # Main application
└── test/
    └── java/com/ecommerce/app/
        └── service/
            ├── IdGeneratorServiceTest.java
            └── IdGeneratorServiceIntegrationTest.java
```

## 🔧 Configuration

### Application Properties
```properties
# Server
server.port=8080
spring.main.allow-circular-references=true

# Firebase (via environment variables)
firebase.private-key-id=${FIREBASE_PRIVATE_KEY_ID}
firebase.private-key=${FIREBASE_PRIVATE_KEY}
firebase.client-email=${FIREBASE_CLIENT_EMAIL}
firebase.client-id=${FIREBASE_CLIENT_ID}
firebase.project-id=${FIREBASE_PROJECT_ID}
firebase.database-url=${FIREBASE_DATABASE_URL}

# Logging (optional)
logging.level.com.ecommerce.app=DEBUG
logging.level.org.springframework.security=DEBUG
```

### Key Dependencies
- **Spring Boot**: 3.5.4
- **Spring Security**: 6.x (Session-based authentication)
- **Spring Validation**: Request validation
- **Firebase Admin SDK**: 9.5.0 (Realtime Database)
- **Jackson**: JSON processing
- **Lombok**: Code generation and boilerplate reduction
- **JUnit 5**: Testing framework
- **Mockito**: Mocking framework for unit tests

## 🔐 Security Features

### Authentication & Authorization
- **Session-based Authentication**: Stateful sessions with JSESSIONID cookies
- **Password Encryption**: BCrypt hashing (strength 12) for secure password storage
- **Role-based Access Control**: 
  - `CUSTOMER`: Can place orders, view own orders, browse products
  - `ADMIN`: Full access to all resources + user management
- **Method-level Security**: `@PreAuthorize` annotations on sensitive endpoints

### Cross-Origin & Session Configuration
- **CORS Support**: Configured for `*` origins (customize for production)
- **Session Management**: 
  - 30-minute timeout
  - HTTP-only cookies disabled for mobile compatibility
  - Secure cookies disabled for development
  - SameSite=None for cross-origin requests

### Input Validation
- **Request Validation**: Spring Validation annotations
- **SQL Injection Protection**: Firebase NoSQL (no SQL injection risk)
- **XSS Protection**: JSON serialization handles special characters
- **Input Sanitization**: Automatic via Spring Boot

### Production Security Checklist
```properties
# Production settings (update application.properties)
server.servlet.session.cookie.secure=true
server.servlet.session.cookie.same-site=strict
spring.web.cors.allowed-origins=https://yourdomain.com
logging.level.org.springframework.security=WARN
```

## 📱 Android Integration Guide

### Network Configuration
| Environment | Base URL | Notes |
|-------------|----------|-------|
| **Android Emulator** | `http://10.0.2.2:8080/api` | Emulator's host mapping |
| **Physical Device** | `http://192.168.x.x:8080/api` | Replace with your local IP |
| **Production** | `https://yourdomain.com/api` | Use HTTPS in production |

### Session Management for Android
```java
// Required: Implement OkHttp CookieJar for session persistence
public class SessionCookieJar implements CookieJar {
    private final Map<String, List<Cookie>> cookieStore = new HashMap<>();
    
    @Override
    public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
        cookieStore.put(url.host(), cookies);
    }
    
    @Override
    public List<Cookie> loadForRequest(HttpUrl url) {
        List<Cookie> cookies = cookieStore.get(url.host());
        return cookies != null ? cookies : new ArrayList<>();
    }
}
```

### Required Android Dependencies (Java)
```gradle
// Networking
implementation 'com.squareup.retrofit2:retrofit:2.9.0'
implementation 'com.squareup.retrofit2:converter-gson:2.9.0'
implementation 'com.squareup.okhttp3:logging-interceptor:4.12.0'
implementation 'com.squareup.okhttp3:okhttp:4.12.0'

// Image loading
implementation 'com.github.bumptech.glide:glide:4.16.0'

// DataStore for local session
implementation 'androidx.datastore:datastore-preferences:1.0.0'

// Material Design
implementation 'com.google.android.material:material:1.11.0'
```

### Android Retrofit Setup
```java
// Network client with cookie management
OkHttpClient client = new OkHttpClient.Builder()
    .cookieJar(new SessionCookieJar())
    .addInterceptor(new HttpLoggingInterceptor()
        .setLevel(HttpLoggingInterceptor.Level.BODY))
    .connectTimeout(30, TimeUnit.SECONDS)
    .readTimeout(30, TimeUnit.SECONDS)
    .build();

// Retrofit instance
Retrofit retrofit = new Retrofit.Builder()
    .baseUrl("http://10.0.2.2:8080/api/")
    .client(client)
    .addConverterFactory(GsonConverterFactory.create())
    .build();
```

### Key Android Development Notes
- **String IDs**: All entity IDs are String type (USR-0001, PRD-0001) for easy JSON parsing
- **Role-based UI**: Check user role from session to show/hide admin features
- **Session Expiration**: Handle 401 responses by redirecting to login
- **Network Security**: Add `android:usesCleartextTraffic="true"` for development
- **Error Handling**: Implement proper error handling for network timeouts and server errors

## 🚀 Deployment

### Production Firebase Setup
1. **Create Production Firebase Project**
   - Separate project from development
   - Enable Realtime Database with production security rules
   - Generate new service account for production

2. **Production Security Rules**
```json
{
  "rules": {
    "users": {
      "$uid": {
        ".read": "auth != null && (auth.uid == $uid || auth.token.admin == true)",
        ".write": "auth != null && (auth.uid == $uid || auth.token.admin == true)"
      }
    },
    "products": {
      ".read": "auth != null",
      ".write": "auth != null && auth.token.admin == true"
    },
    "orders": {
      "$orderid": {
        ".read": "auth != null && (data.child('userId').val() == auth.uid || auth.token.admin == true)",
        ".write": "auth != null && (data.child('userId').val() == auth.uid || auth.token.admin == true)"
      }
    },
    "counters": {
      ".read": "auth != null && auth.token.admin == true",
      ".write": "auth != null && auth.token.admin == true"
    }
  }
}
```

### Build for Production
```bash
# Build optimized JAR
./mvnw clean package -DskipTests

# Create Docker image (optional)
docker build -t ecommerce-backend .

# Run with production profile
java -jar -Dspring.profiles.active=prod target/ecommerce-app-0.0.1-SNAPSHOT.jar
```

### Production Configuration
```properties
# application-prod.properties
server.port=8080
server.servlet.session.cookie.secure=true
server.servlet.session.cookie.same-site=strict
spring.web.cors.allowed-origins=https://yourdomain.com
logging.level.org.springframework.security=WARN
logging.level.com.ecommerce.app=INFO

# Production Firebase
firebase.database.url=https://prod-project-rtdb.firebaseio.com/
```

### Health Check Endpoint
```bash
# Add to your load balancer/monitoring
GET /actuator/health

# Response
{"status":"UP"}
```

## 🐛 Troubleshooting

### Common Issues

#### 1. Firebase Connection Errors
```
Error: "Firebase service account file not found"
Solution:
- Ensure firebase-service-account.json is in src/main/resources/
- Check file name spelling (case-sensitive)
- Verify the JSON file is valid
```

```
Error: "Permission denied" or "401 Unauthorized"
Solution:
- Verify service account has proper permissions
- Check if Realtime Database is enabled
- Ensure database URL is correct and ends with '/'
```

#### 2. Port Already in Use
```bash
# Change port temporarily
./mvnw spring-boot:run -Dspring-boot.run.arguments="--server.port=8081"

# Or set in application.properties
server.port=8081
```

#### 3. Session/Cookie Issues
```
Problem: Login works but session not maintained
Solution:
- Check CORS configuration
- Verify cookie settings in browser
- For Android: Use 10.0.2.2:8080 instead of localhost
- Ensure cookies are enabled in client
```

#### 4. ID Generation Errors
```
Error: "Failed to generate ID" or duplicate IDs
Solution:
- Check Firebase write permissions
- Verify /counters/ path exists in database
- Check transaction logs in Firebase console
- Restart application to reset connection
```

#### 5. Build/Compilation Issues
```bash
# Clean Maven cache
./mvnw clean

# Delete target folder
rm -rf target/

# Rebuild
./mvnw clean compile
```

### Debug Logging
```bash
# Enable debug mode
export LOGGING_LEVEL_COM_ECOMMERCE_APP=DEBUG

# View Firebase operations
./mvnw spring-boot:run | grep "Firebase"

# View ID generation logs
./mvnw spring-boot:run | grep "IdGenerator"

# View security logs
./mvnw spring-boot:run | grep "Security"
```

### Testing Firebase Connection
```bash
# Test database connectivity
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"test","email":"test@example.com","password":"test123"}'

# Check if user was created in Firebase Console
```

## 📝 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/new-feature`)
3. Commit changes (`git commit -am 'Add new feature'`)
4. Push to branch (`git push origin feature/new-feature`)
5. Create a Pull Request

## 📊 Database Schema

### Firebase Realtime Database Structure
```json
{
  "users": {
    "USR-0001": {
      "id": "USR-0001",
      "username": "john_doe",
      "email": "john@example.com",
      "password": "$2a$12$hashedPassword",
      "role": "CUSTOMER"
    }
  },
  "products": {
    "PRD-0001": {
      "id": "PRD-0001",
      "name": "iPhone 15",
      "description": "Latest iPhone model",
      "price": 999.99,
      "quantity": 50,
      "imageUrl": "https://example.com/iphone15.jpg"
    }
  },
  "orders": {
    "ORD-0001": {
      "id": "ORD-0001",
      "userId": "USR-0001",
      "items": [
        {
          "productId": "PRD-0001",
          "productName": "iPhone 15",
          "quantity": 1,
          "price": 999.99
        }
      ],
      "totalAmount": 999.99,
      "status": "PENDING",
      "createdAt": "2024-01-15T10:30:00",
      "updatedAt": "2024-01-15T10:30:00"
    }
  },
  "counters": {
    "users": 1,
    "products": 1,
    "orders": 1
  }
}
```

## 🧪 Testing Examples

### Complete Test Flow
```bash
# 1. Register admin
curl -c admin.txt -X POST http://localhost:8080/api/auth/register-admin \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","email":"admin@test.com","password":"admin123"}'

# 2. Create product
curl -b admin.txt -X POST http://localhost:8080/api/admin/products \
  -H "Content-Type: application/json" \
  -d '{"name":"Test Product","description":"A test product","price":99.99,"quantity":10,"imageUrl":"http://example.com/image.jpg"}'

# 3. Register customer
curl -c customer.txt -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"customer","email":"customer@test.com","password":"test123"}'

# 4. Browse products
curl http://localhost:8080/api/products

# 5. Create order
curl -b customer.txt -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{"userId":"USR-0001","items":[{"productId":"PRD-0001","productName":"Test Product","quantity":1,"price":99.99}],"totalAmount":99.99}'

# 6. Admin updates order status
curl -b admin.txt -X PUT http://localhost:8080/api/admin/orders/ORD-0001/status \
  -H "Content-Type: application/json" \
  -d '{"status":"CONFIRMED"}'
```

## 📞 Support & Contributing

### Getting Help
- **Issues**: Create an issue on GitHub with detailed description
- **Documentation**: Check this README and inline code comments
- **Firebase**: Review [Firebase Realtime Database docs](https://firebase.google.com/docs/database)
- **Spring Boot**: Check [Spring Boot documentation](https://spring.io/projects/spring-boot)

### Contributing
1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Make your changes
4. Add tests for new functionality
5. Commit your changes (`git commit -m 'Add amazing feature'`)
6. Push to the branch (`git push origin feature/amazing-feature`)
7. Open a Pull Request

### Code Style
- Follow Java conventions
- Use Lombok annotations where appropriate
- Add JavaDoc for public methods
- Write unit tests for new features
- Keep controllers thin, move logic to services

---

**🚀 Built with ❤️ using Spring Boot 3.5.4 and Firebase Realtime Database**

**📱 Ready for Android integration • 🔐 Production-ready security • 🆔 Custom ID generation • 📊 Real-time data sync**