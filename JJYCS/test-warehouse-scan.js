const axios = require('axios');

const api = axios.create({
  baseURL: 'http://localhost:8081/api',
  timeout: 10000
});

async function testWarehouseScan() {
  try {
    console.log('🔍 Testing warehouse scan functionality...\n');

    // First, create a fresh order to test full workflow
    console.log('1. Creating a fresh order for testing...');
    
    const newOrderData = {
      userId: 1,
      shippingType: 'SEA',
      country: 'KR', 
      postalCode: '12345',
      recipientName: 'Test Recipient',
      recipientPhone: '+82-10-1111-2222',
      recipientAddress: 'Test Address for Warehouse Scan',
      recipientPostalCode: '12345',
      specialRequests: 'Fresh order for warehouse scan testing',
      orderItems: [{
        hsCode: '6109.10.00',
        description: 'Test T-Shirt',
        quantity: 1,
        weight: 0.3,
        width: 25,
        height: 15, 
        depth: 8,
        unitPrice: 29.99
      }]
    };

    let testOrderNumber = '';
    
    try {
      const createResponse = await api.post('/orders', newOrderData);
      testOrderNumber = createResponse.data.order.orderNumber;
      console.log(`✅ Created fresh order: ${testOrderNumber}`);
      console.log(`   Status: ${createResponse.data.order.status}`);
    } catch (createError) {
      console.log('❌ Failed to create order, using existing order instead');
      testOrderNumber = 'YCS-250824-009'; // fallback to existing order
    }

    // Test with the order (fresh or existing)
    console.log(`\n2. Getting order details: ${testOrderNumber}...`);
    const orderNumber = testOrderNumber;
    
    try {
      const orderResponse = await api.get(`/orders/number/${orderNumber}`);
      console.log('Raw response:', JSON.stringify(orderResponse.data, null, 2));
      
      const order = orderResponse.data.order;
      if (order) {
        console.log(`✅ Order found: ${order.orderNumber}`);
        console.log(`   Status: ${order.status}`);
        console.log(`   Recipient: ${order.recipientName}`);
        console.log(`   Items: ${order.items?.length || 0}`);
        console.log(`   Total CBM: ${order.totalCbm} m³`);
        console.log(`   Shipping Type: ${order.shippingType}`);
      } else {
        console.log('❌ No order data in response');
      }
      
      // Test warehouse scan for this order
      console.log('\n3. Testing warehouse scan operations...');
      
      const scanOperations = ['INBOUND', 'OUTBOUND', 'HOLD', 'MIXBOX'];
      
      for (const scanType of scanOperations) {
        try {
          const scanData = {
            labelCode: orderNumber,
            scanType: scanType,
            location: 'WAREHOUSE_A',
            notes: `Test ${scanType.toLowerCase()} scan via API test`
          };
          
          const scanResponse = await api.post('/warehouse/scan', scanData);
          
          if (scanResponse.data.success) {
            console.log(`✅ ${scanType} scan successful for ${orderNumber}`);
          } else {
            console.log(`❌ ${scanType} scan failed: ${scanResponse.data.message}`);
          }
        } catch (scanError) {
          console.log(`❌ ${scanType} scan error: ${scanError.response?.data?.message || scanError.message}`);
          if (scanError.response?.data) {
            console.log('   Error details:', JSON.stringify(scanError.response.data, null, 2));
          }
        }
      }
      
    } catch (orderError) {
      console.log(`❌ Order lookup failed: ${orderError.response?.data?.message || orderError.message}`);
      console.log('This might be because the getOrderByNumber endpoint is not implemented yet');
    }

  } catch (error) {
    console.error('❌ Test failed:', error.message);
  }
}

testWarehouseScan();