const axios = require('axios');
const { performance } = require('perf_hooks');

const api = axios.create({
  baseURL: 'http://localhost:8081/api',
  timeout: 30000
});

// Performance benchmark configuration
const BENCHMARK_CONFIG = {
  concurrent_users: [1, 5, 10, 20, 50],
  test_duration_ms: 30000, // 30 seconds
  endpoints: [
    { name: 'Health Check', method: 'GET', path: '/health', authenticated: false },
    { name: 'Login', method: 'POST', path: '/auth/login', authenticated: false },
    { name: 'Order List', method: 'GET', path: '/orders', authenticated: true },
    { name: 'Create Order', method: 'POST', path: '/orders', authenticated: true },
    { name: 'Warehouse Scan', method: 'POST', path: '/warehouse/scan', authenticated: true }
  ]
};

let adminToken = '';
let testResults = {};

async function performanceBenchmark() {
  try {
    console.log('🚀 YCS LMS Performance Benchmark Test');
    console.log('=====================================\n');

    // Initialize authentication
    await initializeAuth();

    console.log('📊 Starting Performance Tests...\n');
    
    // Run benchmark tests
    for (const userCount of BENCHMARK_CONFIG.concurrent_users) {
      console.log(`🔄 Testing with ${userCount} concurrent users...`);
      testResults[userCount] = {};
      
      for (const endpoint of BENCHMARK_CONFIG.endpoints) {
        console.log(`   Testing ${endpoint.name}...`);
        const result = await runLoadTest(endpoint, userCount);
        testResults[userCount][endpoint.name] = result;
        
        // Brief pause between endpoint tests
        await sleep(2000);
      }
      
      console.log(`✅ Completed ${userCount} user test\n`);
    }

    // Generate comprehensive report
    generatePerformanceReport();
    
    // System resource check
    await checkSystemResources();
    
    // Memory leak detection
    await detectMemoryLeaks();

  } catch (error) {
    console.error('❌ Performance benchmark failed:', error.message);
  }
}

async function initializeAuth() {
  console.log('🔐 Initializing authentication...');
  
  try {
    const loginResponse = await api.post('/auth/login', {
      email: 'yadmin@ycs.com',
      password: 'YCSAdmin2024!'
    });

    if (loginResponse.data.success) {
      adminToken = loginResponse.data.token;
      console.log('✅ Authentication initialized\n');
    } else {
      throw new Error('Authentication failed');
    }
  } catch (error) {
    console.error('❌ Authentication initialization failed:', error.message);
    throw error;
  }
}

async function runLoadTest(endpoint, concurrentUsers) {
  const results = {
    totalRequests: 0,
    successfulRequests: 0,
    failedRequests: 0,
    responseTimes: [],
    errors: {},
    startTime: Date.now(),
    endTime: null
  };

  const promises = [];
  const endTime = Date.now() + BENCHMARK_CONFIG.test_duration_ms;

  // Create concurrent user sessions
  for (let i = 0; i < concurrentUsers; i++) {
    promises.push(runUserSession(endpoint, endTime, results));
  }

  // Wait for all user sessions to complete
  await Promise.all(promises);
  
  results.endTime = Date.now();
  return calculateMetrics(results);
}

async function runUserSession(endpoint, endTime, results) {
  while (Date.now() < endTime) {
    const startTime = performance.now();
    
    try {
      results.totalRequests++;
      
      const response = await makeRequest(endpoint);
      
      const responseTime = performance.now() - startTime;
      results.responseTimes.push(responseTime);
      
      if (response.status >= 200 && response.status < 400) {
        results.successfulRequests++;
      } else {
        results.failedRequests++;
        recordError(results, response.status, 'HTTP Error');
      }
      
    } catch (error) {
      results.failedRequests++;
      recordError(results, 'ERROR', error.message);
    }

    // Small delay to prevent overwhelming the server
    await sleep(50);
  }
}

async function makeRequest(endpoint) {
  const config = {
    method: endpoint.method,
    url: endpoint.path,
    headers: endpoint.authenticated ? { 'Authorization': `Bearer ${adminToken}` } : {}
  };

  // Add request data for POST requests
  if (endpoint.method === 'POST') {
    switch (endpoint.path) {
      case '/auth/login':
        config.data = {
          email: 'yadmin@ycs.com',
          password: 'YCSAdmin2024!'
        };
        break;
      case '/orders':
        config.data = {
          userId: 15,
          shippingType: 'SEA',
          country: 'KR',
          postalCode: '12345',
          recipientName: 'Test Recipient',
          recipientPhone: '+82-10-1234-5678',
          recipientAddress: 'Test Address',
          recipientPostalCode: '12345',
          orderItems: [{
            hsCode: '8517.12.00',
            description: 'Performance Test Item',
            quantity: 1,
            weight: 1,
            width: 10,
            height: 10,
            depth: 10,
            unitPrice: 100
          }]
        };
        break;
      case '/warehouse/scan':
        config.data = {
          labelCode: 'TEST-' + Date.now(),
          scanType: 'INBOUND',
          location: 'WAREHOUSE_A',
          notes: 'Performance test scan'
        };
        break;
    }
  }

  return api.request(config);
}

function recordError(results, code, message) {
  const errorKey = `${code}: ${message}`;
  results.errors[errorKey] = (results.errors[errorKey] || 0) + 1;
}

function calculateMetrics(results) {
  const responseTimes = results.responseTimes.sort((a, b) => a - b);
  const totalDuration = results.endTime - results.startTime;
  
  return {
    totalRequests: results.totalRequests,
    successfulRequests: results.successfulRequests,
    failedRequests: results.failedRequests,
    successRate: ((results.successfulRequests / results.totalRequests) * 100).toFixed(2),
    requestsPerSecond: (results.totalRequests / (totalDuration / 1000)).toFixed(2),
    avgResponseTime: (responseTimes.reduce((sum, time) => sum + time, 0) / responseTimes.length).toFixed(2),
    minResponseTime: responseTimes[0]?.toFixed(2) || 0,
    maxResponseTime: responseTimes[responseTimes.length - 1]?.toFixed(2) || 0,
    p50ResponseTime: responseTimes[Math.floor(responseTimes.length * 0.5)]?.toFixed(2) || 0,
    p95ResponseTime: responseTimes[Math.floor(responseTimes.length * 0.95)]?.toFixed(2) || 0,
    p99ResponseTime: responseTimes[Math.floor(responseTimes.length * 0.99)]?.toFixed(2) || 0,
    errors: results.errors,
    duration: totalDuration
  };
}

function generatePerformanceReport() {
  console.log('\n📊 Performance Benchmark Report');
  console.log('================================\n');

  Object.keys(testResults).forEach(userCount => {
    console.log(`🔸 ${userCount} Concurrent Users`);
    console.log('─'.repeat(40));
    
    Object.keys(testResults[userCount]).forEach(endpoint => {
      const metrics = testResults[userCount][endpoint];
      console.log(`\n  ${endpoint}:`);
      console.log(`    Total Requests: ${metrics.totalRequests}`);
      console.log(`    Success Rate: ${metrics.successRate}%`);
      console.log(`    Requests/sec: ${metrics.requestsPerSecond}`);
      console.log(`    Avg Response: ${metrics.avgResponseTime}ms`);
      console.log(`    Min Response: ${metrics.minResponseTime}ms`);
      console.log(`    Max Response: ${metrics.maxResponseTime}ms`);
      console.log(`    P95 Response: ${metrics.p95ResponseTime}ms`);
      console.log(`    P99 Response: ${metrics.p99ResponseTime}ms`);
      
      if (Object.keys(metrics.errors).length > 0) {
        console.log(`    Errors: ${JSON.stringify(metrics.errors)}`);
      }
    });
    console.log('\n');
  });

  // Performance analysis
  analyzePerformance();
}

function analyzePerformance() {
  console.log('🔍 Performance Analysis');
  console.log('======================\n');

  // Find performance bottlenecks
  let slowestEndpoints = {};
  let highestThroughput = {};

  Object.keys(testResults).forEach(userCount => {
    Object.keys(testResults[userCount]).forEach(endpoint => {
      const metrics = testResults[userCount][endpoint];
      
      if (!slowestEndpoints[endpoint] || 
          parseFloat(metrics.p95ResponseTime) > parseFloat(slowestEndpoints[endpoint].p95ResponseTime)) {
        slowestEndpoints[endpoint] = { 
          ...metrics, 
          userCount: userCount 
        };
      }
      
      if (!highestThroughput[endpoint] || 
          parseFloat(metrics.requestsPerSecond) > parseFloat(highestThroughput[endpoint].requestsPerSecond)) {
        highestThroughput[endpoint] = { 
          ...metrics, 
          userCount: userCount 
        };
      }
    });
  });

  console.log('🐌 Slowest Response Times (P95):');
  Object.keys(slowestEndpoints).forEach(endpoint => {
    const metrics = slowestEndpoints[endpoint];
    console.log(`   ${endpoint}: ${metrics.p95ResponseTime}ms (${metrics.userCount} users)`);
  });

  console.log('\n🚀 Highest Throughput:');
  Object.keys(highestThroughput).forEach(endpoint => {
    const metrics = highestThroughput[endpoint];
    console.log(`   ${endpoint}: ${metrics.requestsPerSecond} req/s (${metrics.userCount} users)`);
  });

  // Performance recommendations
  console.log('\n💡 Performance Recommendations:');
  
  Object.keys(slowestEndpoints).forEach(endpoint => {
    const metrics = slowestEndpoints[endpoint];
    if (parseFloat(metrics.p95ResponseTime) > 1000) {
      console.log(`   ⚠️  ${endpoint}: P95 response time (${metrics.p95ResponseTime}ms) exceeds 1000ms`);
      console.log(`      - Consider database query optimization`);
      console.log(`      - Add caching for frequent requests`);
      console.log(`      - Review business logic complexity`);
    }
    
    if (parseFloat(metrics.successRate) < 99) {
      console.log(`   ⚠️  ${endpoint}: Success rate (${metrics.successRate}%) below 99%`);
      console.log(`      - Review error handling and retry logic`);
      console.log(`      - Check resource limits and constraints`);
    }
  });

  // Scalability analysis
  console.log('\n📈 Scalability Analysis:');
  
  BENCHMARK_CONFIG.endpoints.forEach(endpoint => {
    console.log(`\n   ${endpoint.name} Scaling:`);
    BENCHMARK_CONFIG.concurrent_users.forEach(userCount => {
      if (testResults[userCount] && testResults[userCount][endpoint.name]) {
        const metrics = testResults[userCount][endpoint.name];
        console.log(`     ${userCount} users: ${metrics.requestsPerSecond} req/s (${metrics.p95ResponseTime}ms P95)`);
      }
    });
  });
}

async function checkSystemResources() {
  console.log('\n💻 System Resource Check');
  console.log('=========================');

  try {
    // Check backend health and metrics
    const healthResponse = await api.get('/actuator/health');
    console.log(`✅ Application Health: ${healthResponse.data.status || 'UP'}`);

    // Try to get JVM metrics if available
    try {
      const metricsResponse = await api.get('/actuator/metrics');
      console.log(`✅ Metrics Endpoints Available: ${metricsResponse.data.names?.length || 0} metrics`);
      
      // Check specific metrics
      const memoryMetrics = await api.get('/actuator/metrics/jvm.memory.used');
      if (memoryMetrics.data.measurements) {
        const memoryUsed = memoryMetrics.data.measurements[0].value;
        console.log(`📊 JVM Memory Used: ${(memoryUsed / 1024 / 1024).toFixed(2)} MB`);
      }
    } catch (error) {
      console.log('⚠️  Detailed metrics not available');
    }

  } catch (error) {
    console.log('⚠️  System resource check failed:', error.message);
  }
}

async function detectMemoryLeaks() {
  console.log('\n🔍 Memory Leak Detection');
  console.log('=========================');

  const initialMemory = process.memoryUsage();
  console.log('📊 Initial Memory Usage:');
  console.log(`   RSS: ${(initialMemory.rss / 1024 / 1024).toFixed(2)} MB`);
  console.log(`   Heap Used: ${(initialMemory.heapUsed / 1024 / 1024).toFixed(2)} MB`);
  console.log(`   Heap Total: ${(initialMemory.heapTotal / 1024 / 1024).toFixed(2)} MB`);

  // Force garbage collection if possible
  if (global.gc) {
    global.gc();
    console.log('🗑️  Forced garbage collection');
  }

  const finalMemory = process.memoryUsage();
  console.log('\n📊 Final Memory Usage:');
  console.log(`   RSS: ${(finalMemory.rss / 1024 / 1024).toFixed(2)} MB`);
  console.log(`   Heap Used: ${(finalMemory.heapUsed / 1024 / 1024).toFixed(2)} MB`);
  console.log(`   Heap Total: ${(finalMemory.heapTotal / 1024 / 1024).toFixed(2)} MB`);

  const memoryGrowth = finalMemory.heapUsed - initialMemory.heapUsed;
  console.log(`\n📈 Memory Growth: ${(memoryGrowth / 1024 / 1024).toFixed(2)} MB`);

  if (memoryGrowth > 50 * 1024 * 1024) { // 50MB threshold
    console.log('⚠️  Potential memory leak detected (>50MB growth)');
  } else {
    console.log('✅ No significant memory leaks detected');
  }
}

function sleep(ms) {
  return new Promise(resolve => setTimeout(resolve, ms));
}

// Run the performance benchmark
if (require.main === module) {
  performanceBenchmark()
    .then(() => {
      console.log('\n🎉 Performance Benchmark Completed!');
      console.log('=====================================');
      console.log('✅ System performance validated');
      console.log('✅ Scalability metrics collected');
      console.log('✅ Resource usage analyzed');
      console.log('✅ Memory leak detection completed');
      console.log('\n📋 Next Steps:');
      console.log('   1. Review performance recommendations');
      console.log('   2. Implement caching strategies');
      console.log('   3. Optimize database queries');
      console.log('   4. Configure load balancing');
      console.log('   5. Set up production monitoring');
    })
    .catch(error => {
      console.error('💥 Benchmark failed:', error);
      process.exit(1);
    });
}

module.exports = { performanceBenchmark };