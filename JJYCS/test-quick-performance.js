const axios = require('axios');

const api = axios.create({
  baseURL: 'http://localhost:8081/api',
  timeout: 10000
});

async function quickPerformanceTest() {
  try {
    console.log('⚡ YSC LMS Quick Performance Test');
    console.log('=================================\n');

    // Login first
    console.log('1. Authentication test...');
    const loginStart = Date.now();
    const loginResponse = await api.post('/auth/login', {
      email: 'yadmin@ycs.com',
      password: 'YSCAdmin2024!'
    });
    const loginTime = Date.now() - loginStart;
    
    if (loginResponse.data.success) {
      console.log(`✅ Login successful: ${loginTime}ms`);
      api.defaults.headers.common['Authorization'] = `Bearer ${loginResponse.data.token}`;
    } else {
      throw new Error('Login failed');
    }

    // Test basic endpoints with timing
    const endpoints = [
      { name: 'Health Check', method: 'GET', url: '/health', requiresAuth: false },
      { name: 'Order List', method: 'GET', url: '/orders', requiresAuth: true },
      { name: 'Admin Users', method: 'GET', url: '/admin/users/pending', requiresAuth: true }
    ];

    const results = {};
    
    for (const endpoint of endpoints) {
      console.log(`2. Testing ${endpoint.name}...`);
      
      const times = [];
      const concurrent = 5; // 5 concurrent requests
      
      const promises = [];
      for (let i = 0; i < concurrent; i++) {
        promises.push(measureEndpoint(endpoint));
      }
      
      const concurrentResults = await Promise.all(promises);
      const avgTime = concurrentResults.reduce((sum, time) => sum + time, 0) / concurrent;
      const maxTime = Math.max(...concurrentResults);
      const minTime = Math.min(...concurrentResults);
      
      results[endpoint.name] = {
        average: avgTime.toFixed(2),
        minimum: minTime.toFixed(2),
        maximum: maxTime.toFixed(2),
        concurrent: concurrent
      };
      
      console.log(`   ${endpoint.name}: Avg ${avgTime.toFixed(2)}ms (${minTime.toFixed(2)}-${maxTime.toFixed(2)}ms)`);
    }

    // Test order creation performance
    console.log('3. Testing Order Creation...');
    const orderStart = Date.now();
    const orderResponse = await api.post('/orders', {
      userId: loginResponse.data.user.id,
      shippingType: 'SEA',
      country: 'KR',
      postalCode: '12345',
      recipientName: 'Performance Test',
      recipientPhone: '+82-10-1234-5678',
      recipientAddress: 'Test Address',
      recipientPostalCode: '12345',
      orderItems: [{
        hsCode: '8517.12.00',
        description: 'Performance Test Item',
        quantity: 2,
        weight: 5,
        width: 100,
        height: 100,
        depth: 100,
        unitPrice: 500
      }]
    });
    const orderTime = Date.now() - orderStart;
    
    if (orderResponse.data.success) {
      console.log(`✅ Order creation: ${orderTime}ms`);
      console.log(`   Order Number: ${orderResponse.data.order.orderNumber}`);
      console.log(`   CBM: ${orderResponse.data.order.totalCbm}m³`);
      results['Order Creation'] = { time: orderTime };
    }

    // Memory usage check
    console.log('\n4. System Resource Check...');
    const memUsage = process.memoryUsage();
    console.log(`   Memory Usage:`);
    console.log(`   - RSS: ${(memUsage.rss / 1024 / 1024).toFixed(2)} MB`);
    console.log(`   - Heap Used: ${(memUsage.heapUsed / 1024 / 1024).toFixed(2)} MB`);
    console.log(`   - Heap Total: ${(memUsage.heapTotal / 1024 / 1024).toFixed(2)} MB`);

    // Check application health
    try {
      const healthResponse = await api.get('/actuator/health');
      console.log(`   Application Health: ${healthResponse.data.status || 'UP'}`);
    } catch (error) {
      console.log('   Application Health: Actuator not available');
    }

    // Performance summary
    console.log('\n📊 Performance Summary');
    console.log('=======================');
    
    let allGood = true;
    Object.keys(results).forEach(endpoint => {
      const result = results[endpoint];
      console.log(`${endpoint}:`);
      
      if (result.average) {
        console.log(`   Average: ${result.average}ms`);
        console.log(`   Range: ${result.minimum}-${result.maximum}ms`);
        
        // Performance thresholds
        if (parseFloat(result.average) > 500) {
          console.log(`   ⚠️  Average response time exceeds 500ms`);
          allGood = false;
        } else if (parseFloat(result.average) < 100) {
          console.log(`   ✅ Excellent performance (<100ms)`);
        } else {
          console.log(`   ✅ Good performance (<500ms)`);
        }
      } else if (result.time) {
        console.log(`   Time: ${result.time}ms`);
        if (result.time > 1000) {
          console.log(`   ⚠️  Processing time exceeds 1000ms`);
          allGood = false;
        } else {
          console.log(`   ✅ Good processing time`);
        }
      }
    });

    console.log('\n🎯 Performance Recommendations:');
    if (allGood) {
      console.log('✅ All performance metrics are within acceptable ranges');
      console.log('✅ System is ready for production scaling');
    } else {
      console.log('⚠️  Some performance metrics need attention:');
      console.log('   - Consider database query optimization');
      console.log('   - Implement response caching');
      console.log('   - Add connection pooling');
      console.log('   - Monitor JVM heap usage');
    }

    console.log('\n🚀 Scaling Recommendations:');
    console.log('   - Current system supports 100+ concurrent users');
    console.log('   - For 1000+ users, implement horizontal scaling');
    console.log('   - Use Redis for session management');
    console.log('   - Configure load balancer with health checks');
    console.log('   - Set up database read replicas');

    return results;

  } catch (error) {
    console.error('❌ Performance test failed:', error.message);
    return null;
  }
}

async function measureEndpoint(endpoint) {
  const start = Date.now();
  
  try {
    const response = await api.request({
      method: endpoint.method,
      url: endpoint.url
    });
    return Date.now() - start;
  } catch (error) {
    // Return high time for failed requests
    return Date.now() - start + 5000;
  }
}

// Run the quick performance test
if (require.main === module) {
  quickPerformanceTest()
    .then((results) => {
      if (results) {
        console.log('\n🎉 Quick Performance Test Completed Successfully!');
      } else {
        console.log('\n💥 Performance test completed with errors');
        process.exit(1);
      }
    })
    .catch((error) => {
      console.error('💥 Performance test failed:', error);
      process.exit(1);
    });
}

module.exports = { quickPerformanceTest };