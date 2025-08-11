package com.ecommerce.app.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(properties = {
    "FIREBASE_PRIVATE_KEY_ID=test",
    "FIREBASE_PRIVATE_KEY=-----BEGIN PRIVATE KEY-----\\nMIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQC8Q7HgL9z+/fake\\n-----END PRIVATE KEY-----\\n",
    "FIREBASE_CLIENT_EMAIL=test@test.iam.gserviceaccount.com",
    "FIREBASE_CLIENT_ID=123456789",
    "FIREBASE_PROJECT_ID=test-project",
    "FIREBASE_DATABASE_URL=https://test-project-default-rtdb.firebaseio.com/"
})
public class IdGeneratorServiceIntegrationTest {

    @Autowired
    private IdGeneratorService idGeneratorService;

    @Test
    void testIdTypeEnumValues() {
        // Test enum values and their properties - no Firebase interaction needed
        IdGeneratorService.IdType userType = IdGeneratorService.IdType.USER;
        assertEquals("USR", userType.getPrefix());
        assertEquals("users", userType.getCounterKey());

        IdGeneratorService.IdType productType = IdGeneratorService.IdType.PRODUCT;
        assertEquals("PRD", productType.getPrefix());
        assertEquals("products", productType.getCounterKey());

        IdGeneratorService.IdType orderType = IdGeneratorService.IdType.ORDER;
        assertEquals("ORD", orderType.getPrefix());
        assertEquals("orders", orderType.getCounterKey());
    }

    @Test
    void testFormatId_ZeroPadding() throws Exception {
        // Use reflection to test the private formatId method
        java.lang.reflect.Method formatIdMethod = IdGeneratorService.class.getDeclaredMethod("formatId", String.class, Long.class);
        formatIdMethod.setAccessible(true);

        // Test various number formats
        String result1 = (String) formatIdMethod.invoke(idGeneratorService, "USR", 1L);
        assertEquals("USR-0001", result1);

        String result2 = (String) formatIdMethod.invoke(idGeneratorService, "PRD", 42L);
        assertEquals("PRD-0042", result2);

        String result3 = (String) formatIdMethod.invoke(idGeneratorService, "ORD", 9999L);
        assertEquals("ORD-9999", result3);

        String result4 = (String) formatIdMethod.invoke(idGeneratorService, "USR", 10000L);
        assertEquals("USR-10000", result4);
    }
}