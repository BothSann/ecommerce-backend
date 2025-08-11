# E-commerce Backend API

A complete Spring Boot backend API for an e-commerce application with user management, product catalog, order processing, and custom ID generation system.

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
- **Maven**: 3.6+ 
- **Firebase**: Valid Firebase project with Realtime Database
- **Environment**: Windows/Linux/macOS

## 🛠️ Installation & Setup

### 1. Clone the Repository
```bash
git clone <repository-url>
cd ecommerce-app-backend
```

### 2. Firebase Configuration

#### Set up Firebase Environment Variables:
```bash
# Required Firebase Configuration
export FIREBASE_PRIVATE_KEY_ID="your-private-key-id"
export FIREBASE_PRIVATE_KEY="-----BEGIN PRIVATE KEY-----\nyour-private-key\n-----END PRIVATE KEY-----\n"
export FIREBASE_CLIENT_EMAIL="your-service-account@project.iam.gserviceaccount.com"
export FIREBASE_CLIENT_ID="your-client-id"
export FIREBASE_PROJECT_ID="your-project-id"
export FIREBASE_DATABASE_URL="https://your-project-default-rtdb.firebaseio.com/"
```

#### Or create `src/main/resources/application.properties`:
```properties
# Firebase Configuration
firebase.private-key-id=${FIREBASE_PRIVATE_KEY_ID}
firebase.private-key=${FIREBASE_PRIVATE_KEY}
firebase.client-email=${FIREBASE_CLIENT_EMAIL}
firebase.client-id=${FIREBASE_CLIENT_ID}
firebase.project-id=${FIREBASE_PROJECT_ID}
firebase.database-url=${FIREBASE_DATABASE_URL}

# Server Configuration
server.port=8080
spring.main.allow-circular-references=true
```

### 3. Build and Run
```bash
# Build the project
./mvnw clean compile

# Run tests
./mvnw test

# Start the application
./mvnw spring-boot:run
```

The API will be available at `http://localhost:8080/api`

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
- `PENDING` - Default status when order is created
- `CONFIRMED` - Admin has confirmed the order  
- `SHIPPED` - Order has been shipped
- `DELIVERED` - Order delivered (final status)

### Status Transitions
- PENDING → CONFIRMED ✅
- CONFIRMED → SHIPPED ✅  
- SHIPPED → DELIVERED ✅
- Invalid transitions return HTTP 400 ❌

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

### Dependencies
- **Spring Boot**: 3.5.4
- **Spring Security**: Session-based authentication
- **Firebase Admin SDK**: 9.5.0
- **Lombok**: Code generation
- **JUnit 5**: Testing framework
- **Mockito**: Mocking framework

## 🔐 Security Features

- **Session-based Authentication**: Stateful sessions with cookie management
- **Password Encryption**: BCrypt hashing for secure password storage
- **Role-based Access Control**: CUSTOMER and ADMIN roles with method-level security
- **CORS Support**: Configured for cross-origin requests
- **Request Validation**: Input validation and sanitization

## 📱 Android Integration

### Key Points for Mobile Development
- **Base URLs**: Use `10.0.2.2:8080` for Android Emulator
- **Cookie Management**: Implement OkHttp CookieJar for session persistence
- **String IDs**: All entity IDs are String type for easy JSON parsing
- **Role-based UI**: Show/hide features based on user role from session
- **Session Persistence**: Handle session expiration gracefully

### Required Android Dependencies
```kotlin
// Networking
implementation("com.squareup.retrofit2:retrofit:2.9.0")
implementation("com.squareup.retrofit2:converter-gson:2.9.0")
implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

// Image loading
implementation("io.coil-kt:coil-compose:2.5.0")
```

## 🚀 Deployment

### Environment Variables for Production
```bash
export FIREBASE_PRIVATE_KEY_ID="production-key-id"
export FIREBASE_PRIVATE_KEY="-----BEGIN PRIVATE KEY-----\n...\n-----END PRIVATE KEY-----\n"
export FIREBASE_CLIENT_EMAIL="prod-service@project.iam.gserviceaccount.com"
export FIREBASE_CLIENT_ID="production-client-id"
export FIREBASE_PROJECT_ID="production-project-id"
export FIREBASE_DATABASE_URL="https://production-project-rtdb.firebaseio.com/"
```

### Build for Production
```bash
# Create JAR file
./mvnw clean package

# Run JAR
java -jar target/ecommerce-app-0.0.1-SNAPSHOT.jar
```

## 🐛 Troubleshooting

### Common Issues

1. **Firebase Connection Error**
   - Verify all environment variables are set
   - Check Firebase service account permissions
   - Ensure Database URL ends with `/`

2. **Port Already in Use**
   ```bash
   # Change port
   ./mvnw spring-boot:run -Dspring-boot.run.arguments="--server.port=8081"
   ```

3. **Session Issues**
   - Check cookie settings in browser
   - Verify CORS configuration
   - Enable session creation in Security Config

4. **ID Generation Errors**
   - Check Firebase write permissions
   - Verify `/counters/` path exists
   - Review transaction logs

### Logs and Debugging
```bash
# Enable debug logging
export LOGGING_LEVEL_COM_ECOMMERCE_APP=DEBUG

# View specific logs
./mvnw spring-boot:run | grep "IdGeneratorService"
```

## 📝 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/new-feature`)
3. Commit changes (`git commit -am 'Add new feature'`)
4. Push to branch (`git push origin feature/new-feature`)
5. Create a Pull Request

## 📞 Support

For support and questions:
- Create an issue on GitHub
- Check the troubleshooting section
- Review Firebase documentation for database-related issues

---

**Built with ❤️ using Spring Boot and Firebase**