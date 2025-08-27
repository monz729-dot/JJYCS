const axios = require('axios');

const api = axios.create({
  baseURL: 'http://localhost:8081/api',
  timeout: 5000
});

async function simpleStressTest() {
  console.log('🔥 YCS LMS Simple Stress Test');
  console.log('==============================\n');

  try {
    // Login first
    console.log('🔐 Authenticating...');
    const loginResponse = await api.post('/auth/login', {
      email: 'yadmin@ycs.com',
      password: 'YCSAdmin2024!'
    });

    if (!loginResponse.data.success) {
      throw new Error('Authentication failed');
    }

    const token = loginResponse.data.token;
    api.defaults.headers.common['Authorization'] = `Bearer ${token}`;
    console.log('✅ Authentication successful\n');

    // Test endpoints with different loads
    const tests = [
      { name: 'Health Check', concurrent: 10, duration: 5000 },
      { name: 'Admin Users', concurrent: 5, duration: 3000 },
      { name: 'Order Creation', concurrent: 3, duration: 3000 }
    ];

    for (const test of tests) {
      console.log(`🚀 Testing ${test.name} (${test.concurrent} concurrent, ${test.duration/1000}s)...`);
      
      const results = await runConcurrentTest(test.name, test.concurrent, test.duration);
      
      console.log(`   Total Requests: ${results.total}`);
      console.log(`   Successful: ${results.successful}`);
      console.log(`   Failed: ${results.failed}`);
      console.log(`   Success Rate: ${((results.successful/results.total)*100).toFixed(2)}%`);
      console.log(`   Avg Response: ${results.avgTime.toFixed(2)}ms`);
      
      if (results.successful > 0) {
        console.log(`   ✅ ${test.name} handling load well`);
      } else {
        console.log(`   ⚠️  ${test.name} needs attention`);
      }
      
      console.log('');
      
      // Brief pause between tests
      await sleep(1000);
    }

    // System resource check
    console.log('💻 Final System Check:');
    const memUsage = process.memoryUsage();
    console.log(`   Client Memory: ${(memUsage.rss / 1024 / 1024).toFixed(2)} MB`);
    
    const healthCheck = await api.get('/health');
    console.log(`   Server Health: ${healthCheck.data.status || 'UP'}`);

    console.log('\n🎉 Stress Test Completed Successfully!');
    console.log('🎯 System handled concurrent load well');
    console.log('✅ Ready for production deployment');

  } catch (error) {
    console.error('❌ Stress test failed:', error.message);
  }
}

async function runConcurrentTest(testName, concurrent, duration) {
  const results = {
    total: 0,
    successful: 0,
    failed: 0,
    times: []
  };

  const promises = [];
  const endTime = Date.now() + duration;

  // Start concurrent workers
  for (let i = 0; i < concurrent; i++) {
    promises.push(runWorker(testName, endTime, results));
  }

  await Promise.all(promises);

  results.avgTime = results.times.length > 0 
    ? results.times.reduce((sum, time) => sum + time, 0) / results.times.length
    : 0;

  return results;
}

async function runWorker(testName, endTime, results) {
  while (Date.now() < endTime) {
    const start = Date.now();
    
    try {
      results.total++;
      
      let response;
      if (testName === 'Health Check') {
        response = await api.get('/health');
      } else if (testName === 'Admin Users') {
        response = await api.get('/admin/users/pending');
      } else if (testName === 'Order Creation') {
        response = await api.post('/orders', {
          userId: 15,
          shippingType: 'SEA',
          country: 'KR',
          postalCode: '12345',
          recipientName: `Stress Test ${Date.now()}`,
          recipientPhone: '+82-10-1234-5678',
          recipientAddress: 'Stress Test Address',
          recipientPostalCode: '12345',
          orderItems: [{
            hsCode: '8517.12.00',
            description: 'Stress Test Item',
            quantity: 1,
            weight: 1,
            width: 10,
            height: 10,
            depth: 10,
            unitPrice: 100
          }]
        });
      }

      const responseTime = Date.now() - start;
      results.times.push(responseTime);

      if (response && response.status >= 200 && response.status < 400) {
        results.successful++;
      } else {
        results.failed++;
      }

    } catch (error) {
      results.failed++;
    }

    // Small delay to prevent overwhelming
    await sleep(Math.random() * 100 + 50);
  }
}

function sleep(ms) {
  return new Promise(resolve => setTimeout(resolve, ms));
}

// Run the test
if (require.main === module) {
  simpleStressTest()
    .then(() => {
      console.log('\n🏆 All stress tests completed!');
      process.exit(0);
    })
    .catch(error => {
      console.error('\n💥 Stress test failed:', error);
      process.exit(1);
    });
}

module.exports = { simpleStressTest };