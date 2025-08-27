const axios = require('axios');

const api = axios.create({
  baseURL: 'http://localhost:8081/api',
  timeout: 10000
});

async function testAdminLogin() {
  try {
    console.log('🔍 Testing admin login functionality...\n');

    // First, try to create admin account if it doesn't exist
    console.log('1. Creating/checking admin account...');
    
    const adminData = {
      name: 'System Administrator',
      email: 'admin@ycs.com',
      password: 'admin123',
      phone: '02-1234-5678',
      userType: 'ADMIN'
    };

    try {
      const signupResponse = await api.post('/auth/signup', adminData);
      if (signupResponse.data.success) {
        console.log(`✅ Admin account created: ${signupResponse.data.user.name}`);
      }
    } catch (signupError) {
      console.log('ℹ️  Admin account might already exist, trying login...');
    }

    // Try logging in as admin with different possible credentials
    console.log('\n2. Attempting admin login with different credentials...');
    
    const credentialOptions = [
      { email: 'admin@ycs.com', password: 'admin123' },
      { email: 'admin@ycs.com', password: 'Password123!' },
      { email: 'admin@ycs.com', password: 'password' },
    ];

    let loginSuccess = false;
    let loginResponse = null;

    for (const loginData of credentialOptions) {
      try {
        console.log(`   Trying: ${loginData.email} / ${loginData.password}`);
        loginResponse = await api.post('/auth/login', loginData);
        if (loginResponse.data.success) {
          console.log(`✅ Login successful with: ${loginData.email} / ${loginData.password}`);
          loginSuccess = true;
          break;
        }
      } catch (error) {
        console.log(`   ❌ Failed: ${error.response?.data?.message || error.message}`);
      }
    }

    if (!loginSuccess) {
      console.log('\n❌ All login attempts failed. Admin account might not exist or password is different.');
      return;
    }

    // Continue with successful login
    if (loginResponse && loginResponse.data.success) {
      console.log(`✅ Admin login successful!`);
      console.log(`   User: ${loginResponse.data.user.name}`);
      console.log(`   Type: ${loginResponse.data.user.userType}`);
      console.log(`   Status: ${loginResponse.data.user.status}`);
      console.log(`   Token: ${loginResponse.data.token ? '✓ Present' : '✗ Missing'}`);
      
      // Set token for subsequent requests
      if (loginResponse.data.token) {
        api.defaults.headers.common['Authorization'] = `Bearer ${loginResponse.data.token}`;
        
        // Test accessing admin endpoints with token
        console.log('\n3. Testing admin-only endpoints with token...');
        
        try {
          const pendingResponse = await api.get('/admin/users/pending');
          console.log(`✅ Pending users endpoint accessible: ${pendingResponse.data.users?.length || 0} users`);
        } catch (error) {
          console.log(`❌ Pending users endpoint failed: ${error.response?.data?.message || error.message}`);
        }
        
        try {
          const approveResponse = await api.post('/admin/users/14/approve', {
            notes: 'Test approval with admin token'
          });
          console.log(`✅ User approval endpoint accessible`);
        } catch (error) {
          console.log(`ℹ️  User approval: ${error.response?.data?.message || 'User might already be approved'}`);
        }
      }
    } else {
      console.log(`❌ Admin login failed: ${loginResponse.data.error || 'Unknown error'}`);
    }

  } catch (error) {
    console.error('❌ Login test failed:', error.response?.data?.message || error.message);
  }
}

testAdminLogin();