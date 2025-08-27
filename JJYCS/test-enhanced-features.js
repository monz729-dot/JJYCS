const axios = require('axios');

const api = axios.create({
  baseURL: 'http://localhost:8081/api',
  timeout: 10000
});

async function testEnhancedFeatures() {
  try {
    console.log('🔍 Testing enhanced error handling and toast notifications...\n');

    // Login as admin first to get a valid token
    console.log('1. Admin login for testing...');
    const loginResponse = await api.post('/auth/login', {
      email: 'yadmin@ycs.com',
      password: 'YCSAdmin2024!'
    });

    if (loginResponse.data.success) {
      console.log(`✅ Admin logged in: ${loginResponse.data.user.name}`);
      api.defaults.headers.common['Authorization'] = `Bearer ${loginResponse.data.token}`;
    }

    // Test 1: Order Creation with Business Rules
    console.log('\n2. Testing order creation with business rules...');
    
    // Create a large CBM order to trigger business rules
    const largeCBMOrder = {
      userId: loginResponse.data.user.id,
      shippingType: 'SEA', // Should be auto-converted to AIR
      country: 'KR',
      postalCode: '12345',
      recipientName: 'Samsung Display',
      recipientPhone: '+82-2-2255-0114',
      recipientAddress: 'Samsung Electronics Building, Seoul',
      recipientPostalCode: '12345',
      specialRequests: 'Test large CBM order for business rule validation',
      orderItems: [
        {
          hsCode: '8517.12.00',
          description: 'Large Display Panel',
          quantity: 5,
          weight: 15,
          width: 400, // Large dimensions to exceed CBM
          height: 200,
          depth: 50,
          unitPrice: 899.99 // High value to test THB threshold
        },
        {
          hsCode: '8517.12.00',
          description: 'Premium Electronic Component',
          quantity: 2,
          weight: 5,
          width: 200,
          height: 150,
          depth: 100,
          unitPrice: 1299.99 // Another high value item
        }
      ]
    };

    try {
      const orderResponse = await api.post('/orders', largeCBMOrder);
      
      if (orderResponse.data.success) {
        const order = orderResponse.data.order;
        console.log(`✅ Order created: ${order.orderNumber}`);
        console.log(`   CBM: ${order.totalCbm}m³`);
        console.log(`   Shipping Type: ${order.shippingType} (originally requested SEA)`);
        console.log(`   CBM Exceeded: ${orderResponse.data.cbmExceeded ? 'Yes' : 'No'}`);
        console.log(`   Requires Extra Recipient: ${orderResponse.data.requiresExtraRecipient ? 'Yes' : 'No'}`);
        console.log(`   No Member Code: ${orderResponse.data.noMemberCode ? 'Yes' : 'No'}`);
        
        if (orderResponse.data.warnings) {
          console.log(`   Warnings: ${orderResponse.data.warnings}`);
        }
      }
    } catch (orderError) {
      console.log(`❌ Order creation failed: ${orderError.response?.data?.message || orderError.message}`);
    }

    // Test 2: Warehouse Error Scenarios
    console.log('\n3. Testing warehouse error scenarios...');
    
    // Test invalid order code
    try {
      await api.post('/warehouse/scan', {
        labelCode: 'INVALID-ORDER-123',
        scanType: 'INBOUND',
        location: 'WAREHOUSE_A',
        notes: 'Testing invalid order scan'
      });
    } catch (warehouseError) {
      console.log(`✅ Expected error for invalid order: ${warehouseError.response?.data?.message || warehouseError.message}`);
    }

    // Test 3: Admin API Error Scenarios
    console.log('\n4. Testing admin API error scenarios...');
    
    // Test invalid user approval
    try {
      await api.post('/admin/users/99999/approve', {
        notes: 'Testing non-existent user approval'
      });
    } catch (adminError) {
      console.log(`✅ Expected error for non-existent user: ${adminError.response?.data?.message || adminError.message}`);
    }

    // Test 4: Network/Timeout Scenarios
    console.log('\n5. Testing network error handling...');
    
    // Create a temporary API client with very short timeout
    const timeoutApi = axios.create({
      baseURL: 'http://localhost:8081/api',
      timeout: 1, // 1ms timeout to force timeout error
      headers: {
        'Authorization': `Bearer ${loginResponse.data.token}`
      }
    });

    try {
      await timeoutApi.get('/admin/users/pending');
    } catch (timeoutError) {
      console.log(`✅ Expected timeout error: ${timeoutError.code === 'ECONNABORTED' ? 'TIMEOUT' : timeoutError.message}`);
    }

    // Test 5: Validation Errors
    console.log('\n6. Testing validation errors...');
    
    // Try to create order with missing required fields
    try {
      await api.post('/orders', {
        userId: loginResponse.data.user.id,
        shippingType: 'SEA',
        // Missing required fields
        orderItems: []
      });
    } catch (validationError) {
      console.log(`✅ Expected validation error: ${validationError.response?.status} - ${validationError.response?.data?.message || validationError.message}`);
    }

    console.log('\n🎉 Enhanced error handling tests completed!');
    console.log('   Frontend toast notifications and error handlers should now provide better user experience.');
    console.log('   Visit http://localhost:3003 to test the UI improvements.');

  } catch (error) {
    console.error('❌ Enhanced features test failed:', error.message);
  }
}

testEnhancedFeatures();