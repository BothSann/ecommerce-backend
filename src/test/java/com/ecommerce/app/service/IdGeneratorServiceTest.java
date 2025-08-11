package com.ecommerce.app.service;

import com.google.firebase.database.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class IdGeneratorServiceTest {

    @Mock
    private DatabaseReference databaseReference;

    @Mock
    private DatabaseReference counterReference;

    private IdGeneratorService idGeneratorService;

    @BeforeEach
    void setUp() {
        idGeneratorService = new IdGeneratorService();
        // Set the databaseReference using reflection
        ReflectionTestUtils.setField(idGeneratorService, "databaseReference", databaseReference);
    }

    @Test
    void testGenerateId_UserType_Success() throws ExecutionException, InterruptedException, TimeoutException {
        // Setup
        when(databaseReference.child("counters/users")).thenReturn(counterReference);
        
        // Mock the transaction behavior
        doAnswer(invocation -> {
            Transaction.Handler handler = invocation.getArgument(0);
            MutableData mockData = mock(MutableData.class);
            
            // Simulate first call (null value, should initialize to 1)
            when(mockData.getValue(Long.class)).thenReturn(null).thenReturn(1L);
            
            Transaction.Result result = handler.doTransaction(mockData);
            assertNotNull(result);
            
            // Simulate successful completion
            DataSnapshot mockSnapshot = mock(DataSnapshot.class);
            when(mockSnapshot.getValue(Long.class)).thenReturn(1L);
            handler.onComplete(null, true, mockSnapshot);
            
            return null;
        }).when(counterReference).runTransaction(any(Transaction.Handler.class));

        // Execute
        CompletableFuture<String> future = idGeneratorService.generateId(IdGeneratorService.IdType.USER);
        String result = future.get(5, TimeUnit.SECONDS);

        // Verify
        assertEquals("USR-0001", result);
        verify(counterReference).runTransaction(any(Transaction.Handler.class));
    }

    @Test
    void testGenerateId_ProductType_Success() throws ExecutionException, InterruptedException, TimeoutException {
        // Setup
        when(databaseReference.child("counters/products")).thenReturn(counterReference);
        
        // Mock the transaction behavior for existing counter
        doAnswer(invocation -> {
            Transaction.Handler handler = invocation.getArgument(0);
            MutableData mockData = mock(MutableData.class);
            
            // Simulate existing value of 5, should increment to 6
            when(mockData.getValue(Long.class)).thenReturn(5L).thenReturn(6L);
            
            Transaction.Result result = handler.doTransaction(mockData);
            assertNotNull(result);
            
            // Simulate successful completion
            DataSnapshot mockSnapshot = mock(DataSnapshot.class);
            when(mockSnapshot.getValue(Long.class)).thenReturn(6L);
            handler.onComplete(null, true, mockSnapshot);
            
            return null;
        }).when(counterReference).runTransaction(any(Transaction.Handler.class));

        // Execute
        CompletableFuture<String> future = idGeneratorService.generateId(IdGeneratorService.IdType.PRODUCT);
        String result = future.get(5, TimeUnit.SECONDS);

        // Verify
        assertEquals("PRD-0006", result);
        verify(counterReference).runTransaction(any(Transaction.Handler.class));
    }

    @Test
    void testGenerateId_OrderType_Success() throws ExecutionException, InterruptedException, TimeoutException {
        // Setup
        when(databaseReference.child("counters/orders")).thenReturn(counterReference);
        
        // Mock the transaction behavior
        doAnswer(invocation -> {
            Transaction.Handler handler = invocation.getArgument(0);
            MutableData mockData = mock(MutableData.class);
            
            // Simulate existing value of 99, should increment to 100
            when(mockData.getValue(Long.class)).thenReturn(99L).thenReturn(100L);
            
            Transaction.Result result = handler.doTransaction(mockData);
            assertNotNull(result);
            
            // Simulate successful completion
            DataSnapshot mockSnapshot = mock(DataSnapshot.class);
            when(mockSnapshot.getValue(Long.class)).thenReturn(100L);
            handler.onComplete(null, true, mockSnapshot);
            
            return null;
        }).when(counterReference).runTransaction(any(Transaction.Handler.class));

        // Execute
        CompletableFuture<String> future = idGeneratorService.generateId(IdGeneratorService.IdType.ORDER);
        String result = future.get(5, TimeUnit.SECONDS);

        // Verify
        assertEquals("ORD-0100", result);
        verify(counterReference).runTransaction(any(Transaction.Handler.class));
    }

    @Test
    void testGenerateId_DatabaseError_ThrowsException() {
        // Setup
        when(databaseReference.child("counters/users")).thenReturn(counterReference);
        DatabaseError mockError = mock(DatabaseError.class);
        com.google.firebase.database.DatabaseException expectedException = new com.google.firebase.database.DatabaseException("Database error");
        when(mockError.toException()).thenReturn(expectedException);
        
        // Mock the transaction behavior with error
        doAnswer(invocation -> {
            Transaction.Handler handler = invocation.getArgument(0);
            handler.onComplete(mockError, false, null);
            return null;
        }).when(counterReference).runTransaction(any(Transaction.Handler.class));

        // Execute & Verify
        CompletableFuture<String> future = idGeneratorService.generateId(IdGeneratorService.IdType.USER);
        
        ExecutionException exception = assertThrows(ExecutionException.class, () -> 
            future.get(5, TimeUnit.SECONDS)
        );
        
        assertEquals(expectedException, exception.getCause());
    }

    @Test
    void testGetCurrentCounter_Success() throws ExecutionException, InterruptedException, TimeoutException {
        // Setup
        when(databaseReference.child("counters/users")).thenReturn(counterReference);
        
        doAnswer(invocation -> {
            ValueEventListener listener = invocation.getArgument(0);
            DataSnapshot mockSnapshot = mock(DataSnapshot.class);
            when(mockSnapshot.getValue(Long.class)).thenReturn(42L);
            listener.onDataChange(mockSnapshot);
            return null;
        }).when(counterReference).addListenerForSingleValueEvent(any(ValueEventListener.class));

        // Execute
        CompletableFuture<Long> future = idGeneratorService.getCurrentCounter(IdGeneratorService.IdType.USER);
        Long result = future.get(5, TimeUnit.SECONDS);

        // Verify
        assertEquals(42L, result);
        verify(counterReference).addListenerForSingleValueEvent(any(ValueEventListener.class));
    }

    @Test
    void testGetCurrentCounter_NullValue_ReturnsZero() throws ExecutionException, InterruptedException, TimeoutException {
        // Setup
        when(databaseReference.child("counters/products")).thenReturn(counterReference);
        
        doAnswer(invocation -> {
            ValueEventListener listener = invocation.getArgument(0);
            DataSnapshot mockSnapshot = mock(DataSnapshot.class);
            when(mockSnapshot.getValue(Long.class)).thenReturn(null);
            listener.onDataChange(mockSnapshot);
            return null;
        }).when(counterReference).addListenerForSingleValueEvent(any(ValueEventListener.class));

        // Execute
        CompletableFuture<Long> future = idGeneratorService.getCurrentCounter(IdGeneratorService.IdType.PRODUCT);
        Long result = future.get(5, TimeUnit.SECONDS);

        // Verify
        assertEquals(0L, result);
    }

    @Test
    void testResetCounter_Success() throws ExecutionException, InterruptedException, TimeoutException {
        // Setup
        when(databaseReference.child("counters/orders")).thenReturn(counterReference);
        
        doAnswer(invocation -> {
            DatabaseReference.CompletionListener listener = invocation.getArgument(1);
            listener.onComplete(null, counterReference);
            return null;
        }).when(counterReference).setValue(eq(0L), any(DatabaseReference.CompletionListener.class));

        // Execute
        CompletableFuture<Void> future = idGeneratorService.resetCounter(IdGeneratorService.IdType.ORDER);
        future.get(5, TimeUnit.SECONDS);

        // Verify
        verify(counterReference).setValue(eq(0L), any(DatabaseReference.CompletionListener.class));
    }

    @Test
    void testIdTypeEnum_Values() {
        // Test enum values and their properties
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