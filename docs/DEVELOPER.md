# Developer Guide

[![Java](https://img.shields.io/badge/Java-24-007396?logo=java)](#)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.x-6DB33F?logo=spring-boot)](#)
[![Maven](https://img.shields.io/badge/Maven-3.9+-blue?logo=apache-maven)](#)

Comprehensive developer documentation for the FindYourDreamHouseAI project.

## 📋 Table of Contents

- [Development Setup](#-development-setup)
- [Project Structure](#-project-structure)
- [Code Style Guidelines](#-code-style-guidelines)
- [Testing](#-testing)
- [Database Development](#-database-development)
- [AWS Integration](#-aws-integration)
- [Security Implementation](#-security-implementation)
- [Common Patterns](#-common-patterns)
- [Debugging](#-debugging)
- [Performance Considerations](#-performance-considerations)

## 🛠️ Development Setup

### Prerequisites

- **Java 24** (or Java 21+)
- **Maven 3.9+**
- **PostgreSQL 12+**
- **AWS CLI** (for AWS integration)
- **IDE** (IntelliJ IDEA, Eclipse, or VS Code)
- **Git**

### IDE Configuration

#### IntelliJ IDEA

1. **Import Project:**
   ```bash
   # Clone and open in IntelliJ
   File → Open → Select project root directory
   ```

2. **Configure Java SDK:**
   - File → Project Structure → Project → Project SDK → Java 24
   - File → Project Structure → Modules → Language Level → 24

3. **Maven Configuration:**
   - File → Settings → Build → Build Tools → Maven
   - Set Maven home directory and user settings file

4. **Code Style:**
   - File → Settings → Editor → Code Style → Java
   - Import the project's code style settings

#### VS Code

1. **Install Extensions:**
   ```bash
   # Install Java Extension Pack
   code --install-extension vscjava.vscode-java-pack
   
   # Install Spring Boot Extension Pack
   code --install-extension vmware.vscode-spring-boot
   ```

2. **Configure Java:**
   - Open Command Palette (Ctrl+Shift+P)
   - Type "Java: Configure Java Runtime"
   - Set Java 24 as the default

### Local Development Environment

1. **Clone Repository:**
   ```bash
   git clone <repository-url>
   cd FindYourDreamHouseAI
   ```

2. **Database Setup:**
   ```bash
   # Using Docker
   docker run --name postgres-dev \
     -e POSTGRES_DB=findyourdreamhouse_dev \
     -e POSTGRES_USER=dev_user \
     -e POSTGRES_PASSWORD=dev_password \
     -p 5432:5432 \
     -d postgres:15
   
   # Or install PostgreSQL locally
   createdb findyourdreamhouse_dev
   ```

3. **AWS Configuration:**
   ```bash
   # Configure AWS credentials
   aws configure
   
   # Create development secrets
   aws secretsmanager create-secret \
     --name jwt-secret \
     --secret-string "$(openssl rand -base64 32)"
   
   aws secretsmanager create-secret \
     --name region \
     --secret-string "us-east-1"
   
   aws secretsmanager create-secret \
     --name bucket_name \
     --secret-string "your-dev-bucket"
   
   aws secretsmanager create-secret \
     --name basePath \
     --secret-string "dev/"
   ```

4. **Run Application:**
   ```bash
   # Development profile
   mvn spring-boot:run -Dspring-boot.run.profiles=dev
   
   # Or run from IDE
   # Run FindYourDreamHouseAiApplication.main()
   ```

## 🏗️ Project Structure

```
src/main/java/com/dreamhouse/ai/
├── FindYourDreamHouseAiApplication.java    # Main application class
├── authentication/                         # Authentication module
│   ├── configuration/                     # Security configuration
│   │   ├── AuthenticationFilter.java      # JWT authentication filter
│   │   ├── AuthorizationFilter.java       # JWT authorization filter
│   │   ├── SecurityConfiguration.java     # Spring Security config
│   │   └── WebSecurity.java              # Web security configuration
│   ├── controller/                        # REST controllers
│   │   ├── UserAuthController.java        # Authentication endpoints
│   │   └── UserProfileController.java     # User profile endpoints
│   ├── dto/                              # Data Transfer Objects
│   │   ├── AddressDTO.java               # Address data transfer
│   │   └── UserDTO.java                  # User data transfer
│   ├── exception/                        # Custom exceptions
│   │   ├── UserAlreadyExistsException.java
│   │   ├── UserIDNotFoundException.java
│   │   └── UserRestAdvisor.java          # Global exception handler
│   ├── model/                           # Domain models
│   │   ├── entity/                      # JPA entities
│   │   ├── request/                     # Request models
│   │   ├── response/                    # Response models
│   │   └── security/                    # Security models
│   ├── repository/                      # Data access layer
│   │   ├── UserRepository.java
│   │   ├── RoleRepository.java
│   │   └── AuthorityRepository.java
│   ├── service/                         # Business logic
│   │   ├── UserService.java             # User service interface
│   │   └── impl/                        # Service implementations
│   └── util/                           # Utility classes
│       └── SecurityUtil.java           # Security utilities
├── house/                              # House management module
│   ├── configuration/                  # House-specific configuration
│   ├── controller/                     # House REST controllers
│   ├── dto/                           # House DTOs
│   ├── exception/                     # House exceptions
│   ├── model/                         # House domain models
│   ├── repository/                    # House repositories
│   └── service/                       # House services
└── llm/                               # AI/LLM integration (future)
```

### Module Organization

The project follows **Spring Modulith** principles:

- **authentication**: User management, security, and authentication
- **house**: House advertisement management and related features
- **llm**: AI/LLM integration (planned for future)

Each module is self-contained with its own:
- Controllers
- Services
- Repositories
- DTOs
- Exceptions
- Configuration

## 📝 Code Style Guidelines

### Java Coding Standards

1. **Naming Conventions:**
   ```java
   // Classes: PascalCase
   public class UserService {
   
   // Methods and variables: camelCase
   public void createUser(String username) {
       String userUid = generateUid();
   }
   
   // Constants: UPPER_SNAKE_CASE
   private static final String DEFAULT_ROLE = "ROLE_USER";
   
   // Packages: lowercase with dots
   package com.dreamhouse.ai.authentication.service;
   ```

2. **Method Documentation:**
   ```java
   /**
    * Creates a new user account with the provided information.
    * 
    * @param userRequest the user registration request containing username, password, and personal details
    * @return UserRegisterResponse containing the created user's information
    * @throws UserAlreadyExistsException if username already exists
    * @throws UserAccountNotCreatedException if user creation fails
    */
   public UserRegisterResponse createUser(UserRegisterRequestModel userRequest) {
       // Implementation
   }
   ```

3. **Exception Handling:**
   ```java
   // Custom exceptions for business logic
   public class UserNotFoundException extends RuntimeException {
       public UserNotFoundException(String message) {
           super(message);
       }
   }
   
   // Service layer exception handling
   public UserDTO getUserById(String userId) {
       try {
           return userRepository.findById(userId)
               .map(this::convertToDTO)
               .orElseThrow(() -> new UserNotFoundException("User not found: " + userId));
       } catch (Exception e) {
           log.error("Error retrieving user: {}", userId, e);
           throw new UserServiceException("Failed to retrieve user", e);
       }
   }
   ```

### Spring Boot Best Practices

1. **Configuration Properties:**
   ```java
   @ConfigurationProperties(prefix = "app.aws")
   @Component
   public class AwsProperties {
       private String region;
       private String bucketName;
       private String basePath;
       
       // Getters and setters
   }
   ```

2. **Service Layer Pattern:**
   ```java
   @Service
   @Transactional
   public class UserServiceImpl implements UserService {
       
       private final UserRepository userRepository;
       private final ModelMapper modelMapper;
       
       public UserServiceImpl(UserRepository userRepository, ModelMapper modelMapper) {
           this.userRepository = userRepository;
           this.modelMapper = modelMapper;
       }
       
       @Override
       public UserDTO createUser(UserRegisterRequestModel request) {
           // Business logic implementation
       }
   }
   ```

3. **Repository Pattern:**
   ```java
   @Repository
   public interface UserRepository extends JpaRepository<UserEntity, String> {
       
       Optional<UserEntity> findByUsername(String username);
       
       @Query("SELECT u FROM UserEntity u WHERE u.email = :email")
       Optional<UserEntity> findByEmail(@Param("email") String email);
       
       @Modifying
       @Query("UPDATE UserEntity u SET u.lastLogin = :lastLogin WHERE u.userUid = :userId")
       void updateLastLogin(@Param("userId") String userId, @Param("lastLogin") LocalDateTime lastLogin);
   }
   ```

## 🧪 Testing

### Test Structure

```
src/test/java/com/dreamhouse/ai/
├── authentication/
│   ├── controller/
│   │   └── UserAuthControllerTest.java
│   ├── service/
│   │   └── UserServiceTest.java
│   └── integration/
│       └── AuthenticationIntegrationTest.java
├── house/
│   ├── controller/
│   │   └── HouseAdControllerTest.java
│   └── service/
│       └── HouseAdsServiceTest.java
└── integration/
    └── ApplicationIntegrationTest.java
```

### Unit Testing

```java
@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private ModelMapper modelMapper;
    
    @InjectMocks
    private UserServiceImpl userService;
    
    @Test
    void createUser_ShouldReturnUserDTO_WhenValidRequest() {
        // Given
        UserRegisterRequestModel request = new UserRegisterRequestModel();
        request.setUsername("testuser");
        request.setPassword("password123");
        
        UserEntity savedEntity = new UserEntity();
        savedEntity.setUserUid("test-uuid");
        savedEntity.setUsername("testuser");
        
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());
        when(userRepository.save(any(UserEntity.class))).thenReturn(savedEntity);
        when(modelMapper.map(any(), eq(UserRegisterResponse.class))).thenReturn(new UserRegisterResponse());
        
        // When
        UserRegisterResponse result = userService.createUser(request);
        
        // Then
        assertThat(result).isNotNull();
        verify(userRepository).save(any(UserEntity.class));
    }
    
    @Test
    void createUser_ShouldThrowException_WhenUsernameExists() {
        // Given
        UserRegisterRequestModel request = new UserRegisterRequestModel();
        request.setUsername("existinguser");
        
        when(userRepository.findByUsername("existinguser")).thenReturn(Optional.of(new UserEntity()));
        
        // When & Then
        assertThatThrownBy(() -> userService.createUser(request))
            .isInstanceOf(UserAlreadyExistsException.class)
            .hasMessage("Username already exists: existinguser");
    }
}
```

### Integration Testing

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
class AuthenticationIntegrationTest {
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    @Autowired
    private UserRepository userRepository;
    
    @Test
    void registerUser_ShouldReturnUserResponse_WhenValidRequest() {
        // Given
        UserRegisterRequestModel request = new UserRegisterRequestModel();
        request.setUsername("integrationtest");
        request.setPassword("password123");
        request.setName("Integration");
        request.setLastname("Test");
        
        // When
        ResponseEntity<UserRegisterResponse> response = restTemplate.postForEntity(
            "/api/v1/auth/register",
            request,
            UserRegisterResponse.class
        );
        
        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getUsername()).isEqualTo("integrationtest");
        
        // Verify database
        Optional<UserEntity> savedUser = userRepository.findByUsername("integrationtest");
        assertThat(savedUser).isPresent();
    }
}
```

### Running Tests

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=UserServiceTest

# Run tests with coverage
mvn test jacoco:report

# Run integration tests
mvn test -Dtest="*IntegrationTest"
```

## 🗄️ Database Development

### Entity Design

```java
@Entity
@Table(name = "users")
public class UserEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "user_uid")
    private String userUid;
    
    @Column(name = "username", unique = true, nullable = false)
    private String username;
    
    @Column(name = "password", nullable = false)
    private String password;
    
    @Column(name = "name")
    private String name;
    
    @Column(name = "lastname")
    private String lastname;
    
    @Column(name = "email")
    private String email;
    
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<AddressEntity> addresses = new ArrayList<>();
    
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "user_roles",
        joinColumns = @JoinColumn(name = "user_uid"),
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<RoleEntity> roles = new HashSet<>();
    
    @Column(name = "authorization_token")
    private String authorizationToken;
    
    @Column(name = "last_login")
    private LocalDateTime lastLogin;
    
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Constructors, getters, setters
}
```

### Migration Strategy

1. **Development:**
   ```yaml
   # application-dev.yaml
   spring:
     jpa:
       hibernate:
         ddl-auto: create-drop  # For development
   ```

2. **Production:**
   ```yaml
   # application-prod.yaml
   spring:
     jpa:
       hibernate:
         ddl-auto: validate  # For production
   ```

3. **Database Migrations:**
   ```sql
   -- V1__Create_users_table.sql
   CREATE TABLE users (
       user_uid UUID PRIMARY KEY,
       username VARCHAR(50) UNIQUE NOT NULL,
       password VARCHAR(255) NOT NULL,
       name VARCHAR(100),
       lastname VARCHAR(100),
       email VARCHAR(100),
       authorization_token TEXT,
       last_login TIMESTAMP,
       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
       updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
   );
   ```

### Query Optimization

```java
@Repository
public interface UserRepository extends JpaRepository<UserEntity, String> {
    
    // Use @Query for complex queries
    @Query("SELECT u FROM UserEntity u LEFT JOIN FETCH u.addresses WHERE u.userUid = :userId")
    Optional<UserEntity> findByIdWithAddresses(@Param("userId") String userId);
    
    // Use @EntityGraph for eager loading
    @EntityGraph(attributePaths = {"roles", "authorities"})
    Optional<UserEntity> findByUsername(String username);
    
    // Use native queries for performance-critical operations
    @Query(value = "SELECT COUNT(*) FROM users WHERE created_at >= :date", nativeQuery = true)
    long countUsersCreatedAfter(@Param("date") LocalDateTime date);
}
```

## ☁️ AWS Integration

### S3 Configuration

```java
@Configuration
@EnableConfigurationProperties(AwsProperties.class)
public class AwsConfiguration {
    
    @Bean
    public S3Client s3Client(AwsProperties awsProperties) {
        return S3Client.builder()
            .region(Region.of(awsProperties.getRegion()))
            .credentialsProvider(DefaultCredentialsProvider.create())
            .build();
    }
    
    @Bean
    public S3Presigner s3Presigner(AwsProperties awsProperties) {
        return S3Presigner.builder()
            .region(Region.of(awsProperties.getRegion()))
            .credentialsProvider(DefaultCredentialsProvider.create())
            .build();
    }
}
```

### Storage Service Implementation

```java
@Service
public class StorageServiceImpl implements StorageService {
    
    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final AwsProperties awsProperties;
    
    @Override
    public String uploadImage(String key, byte[] imageData, String contentType) {
        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(awsProperties.getBucketName())
                .key(key)
                .contentType(contentType)
                .serverSideEncryption(ServerSideEncryption.AES256)
                .build();
            
            s3Client.putObject(putObjectRequest, RequestBody.fromBytes(imageData));
            
            return generatePresignedUrl(key);
        } catch (Exception e) {
            log.error("Failed to upload image to S3: {}", key, e);
            throw new CloudException("Failed to upload image", e);
        }
    }
    
    private String generatePresignedUrl(String key) {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
            .bucket(awsProperties.getBucketName())
            .key(key)
            .build();
        
        GetObjectPresignedRequest presignedRequest = GetObjectPresignedRequest.builder()
            .signatureDuration(Duration.ofHours(1))
            .getObjectRequest(getObjectRequest)
            .build();
        
        return s3Presigner.presignGetObject(presignedRequest).url().toString();
    }
}
```

## 🔐 Security Implementation

### JWT Configuration

```java
@Configuration
public class JwtConfiguration {
    
    @Value("${jwt.secret}")
    private String jwtSecret;
    
    @Bean
    public JwtEncoder jwtEncoder() {
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
        return NimbusJwtEncoder.withSecretKey(key).build();
    }
    
    @Bean
    public JwtDecoder jwtDecoder() {
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
        return NimbusJwtDecoder.withSecretKey(key).build();
    }
}
```

### Security Filter Chain

```java
@Configuration
@EnableWebSecurity
public class SecurityConfiguration {
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/login", "/api/v1/auth/register").permitAll()
                .requestMatchers("/api/v1/houseAds").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/houseAds/**").permitAll()
                .anyRequest().authenticated()
            )
            .addFilterBefore(authenticationFilter(), UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(authorizationFilter(), UsernamePasswordAuthenticationFilter.class)
            .build();
    }
}
```

### Password Encoding

```java
@Configuration
public class PasswordConfiguration {
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
}
```

## 🔄 Common Patterns

### Service Layer Pattern

```java
@Service
@Transactional
public class UserServiceImpl implements UserService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;
    
    @Override
    @Transactional(readOnly = true)
    public UserDTO getUserById(String userId) {
        UserEntity user = userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException("User not found: " + userId));
        
        return modelMapper.map(user, UserDTO.class);
    }
    
    @Override
    public UserDTO createUser(UserRegisterRequestModel request) {
        validateUserRequest(request);
        
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new UserAlreadyExistsException("Username already exists: " + request.getUsername());
        }
        
        UserEntity user = new UserEntity();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setName(request.getName());
        user.setLastname(request.getLastname());
        user.setEmail(request.getEmail());
        
        UserEntity savedUser = userRepository.save(user);
        return modelMapper.map(savedUser, UserDTO.class);
    }
    
    private void validateUserRequest(UserRegisterRequestModel request) {
        if (StringUtils.isBlank(request.getUsername())) {
            throw new ValidationException("Username is required");
        }
        if (StringUtils.isBlank(request.getPassword())) {
            throw new ValidationException("Password is required");
        }
    }
}
```

### Controller Pattern

```java
@RestController
@RequestMapping("/api/v1/auth")
@Validated
public class UserAuthController {
    
    private final UserService userService;
    
    @PostMapping("/register")
    public ResponseEntity<UserRegisterResponse> registerUser(
            @Valid @RequestBody UserRegisterRequestModel request) {
        
        try {
            UserRegisterResponse response = userService.createUser(request);
            return ResponseEntity.ok(response);
        } catch (UserAlreadyExistsException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        } catch (ValidationException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }
    
    @GetMapping("/user/{userId}")
    public ResponseEntity<UserDTO> getUser(@PathVariable String userId) {
        UserDTO user = userService.getUserById(userId);
        return ResponseEntity.ok(user);
    }
}
```

### Exception Handling Pattern

```java
@RestControllerAdvice
public class UserRestAdvisor {
    
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFound(UserNotFoundException ex) {
        ErrorResponse error = ErrorResponse.builder()
            .error("User not found")
            .code("USER_NOT_FOUND")
            .message(ex.getMessage())
            .timestamp(LocalDateTime.now())
            .build();
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }
    
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidation(ValidationException ex) {
        ErrorResponse error = ErrorResponse.builder()
            .error("Validation failed")
            .code("VALIDATION_ERROR")
            .message(ex.getMessage())
            .timestamp(LocalDateTime.now())
            .build();
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
}
```

## 🐛 Debugging

### Logging Configuration

```yaml
# application-dev.yaml
logging:
  level:
    com.dreamhouse.ai: DEBUG
    org.springframework.security: DEBUG
    org.springframework.web: DEBUG
    org.hibernate.SQL: DEBUG
    org.hibernate.type.descriptor.sql.BasicBinder: TRACE
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} - %msg%n"
    file: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
```

### Debug Endpoints

```java
@RestController
@RequestMapping("/debug")
@Profile("dev")
public class DebugController {
    
    @GetMapping("/users")
    public ResponseEntity<List<UserEntity>> getAllUsers() {
        // Debug endpoint for development
        return ResponseEntity.ok(userRepository.findAll());
    }
    
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("timestamp", LocalDateTime.now());
        health.put("database", checkDatabaseConnection());
        return ResponseEntity.ok(health);
    }
}
```

### Common Debugging Techniques

1. **Enable SQL Logging:**
   ```yaml
   spring:
     jpa:
       show-sql: true
       properties:
         hibernate:
           format_sql: true
   ```

2. **Use Debugger:**
   - Set breakpoints in critical methods
   - Use conditional breakpoints for specific scenarios
   - Inspect variable values and object state

3. **Add Debug Logging:**
   ```java
   @Slf4j
   public class UserService {
       
       public UserDTO createUser(UserRegisterRequestModel request) {
           log.debug("Creating user with username: {}", request.getUsername());
           
           try {
               UserEntity user = new UserEntity();
               // ... user creation logic
               
               log.debug("User created successfully with ID: {}", user.getUserUid());
               return modelMapper.map(user, UserDTO.class);
           } catch (Exception e) {
               log.error("Failed to create user: {}", request.getUsername(), e);
               throw e;
           }
       }
   }
   ```

## ⚡ Performance Considerations

### Database Optimization

1. **Indexing:**
   ```java
   @Entity
   @Table(name = "users", indexes = {
       @Index(name = "idx_username", columnList = "username"),
       @Index(name = "idx_email", columnList = "email"),
       @Index(name = "idx_created_at", columnList = "created_at")
   })
   public class UserEntity {
       // Entity definition
   }
   ```

2. **Query Optimization:**
   ```java
   @Query("SELECT u FROM UserEntity u WHERE u.username = :username")
   @QueryHints(@QueryHint(name = "org.hibernate.cacheable", value = "true"))
   Optional<UserEntity> findByUsername(@Param("username") String username);
   ```

3. **Connection Pooling:**
   ```yaml
   spring:
     datasource:
       hikari:
         maximum-pool-size: 20
         minimum-idle: 5
         connection-timeout: 30000
         idle-timeout: 600000
         max-lifetime: 1800000
   ```

### Caching Strategy

```java
@Configuration
@EnableCaching
public class CacheConfiguration {
    
    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        cacheManager.setCaffeine(Caffeine.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(Duration.ofMinutes(10)));
        return cacheManager;
    }
}

@Service
public class UserService {
    
    @Cacheable(value = "users", key = "#userId")
    public UserDTO getUserById(String userId) {
        // Implementation
    }
    
    @CacheEvict(value = "users", key = "#user.userUid")
    public UserDTO updateUser(UserEntity user) {
        // Implementation
    }
}
```

### Memory Management

1. **JVM Tuning:**
   ```bash
   # Development
   java -Xms512m -Xmx2g -XX:+UseG1GC -jar app.jar
   
   # Production
   java -Xms2g -Xmx8g -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -jar app.jar
   ```

2. **Object Pooling:**
   ```java
   @Component
   public class ObjectPool {
       
       private final Queue<ModelMapper> mapperPool = new ConcurrentLinkedQueue<>();
       
       public ModelMapper borrowMapper() {
           ModelMapper mapper = mapperPool.poll();
           return mapper != null ? mapper : new ModelMapper();
       }
       
       public void returnMapper(ModelMapper mapper) {
           mapperPool.offer(mapper);
       }
   }
   ```

---

**Need Help?** Check out our [FAQ](./FAQ.md) or [open an issue](https://github.com/your-org/FindYourDreamHouseAI/issues) for development support.

