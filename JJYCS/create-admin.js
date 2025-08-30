const axios = require('axios');

const api = axios.create({
  baseURL: 'http://localhost:8080/api',
  timeout: 10000
});

async function createAdmin() {
  try {
    console.log('🔍 Creating fresh admin account...\n');

    const adminData = {
      name: 'YSC Administrator',
      email: 'yadmin@ycs.com', // Use different email to avoid conflicts
      password: 'YSCAdmin2024!',
      phone: '02-1234-5678',
      userType: 'ADMIN'
    };

    console.log('1. Creating admin account...');
    const signupResponse = await api.post('/auth/signup', adminData);
    
    if (signupResponse.data.success) {
      console.log(`✅ Admin account created successfully!`);
      console.log(`   Name: ${signupResponse.data.user.name}`);
      console.log(`   Email: ${signupResponse.data.user.email}`);
      console.log(`   Type: ${signupResponse.data.user.userType}`);
      console.log(`   Status: ${signupResponse.data.user.status}`);
      console.log(`   ID: ${signupResponse.data.user.id}`);
      
      // Now try to login with the new account
      console.log('\n2. Testing login with new admin account...');
      
      const loginResponse = await api.post('/auth/login', {
        email: adminData.email,
        password: adminData.password
      });
      
      if (loginResponse.data.success) {
        console.log(`✅ Admin login successful!`);
        console.log(`   Token: ${loginResponse.data.token ? '✓ Present' : '✗ Missing'}`);
        
        // Test admin API access
        if (loginResponse.data.token) {
          api.defaults.headers.common['Authorization'] = `Bearer ${loginResponse.data.token}`;
          
          console.log('\n3. Testing admin endpoints...');
          
          try {
            const pendingResponse = await api.get('/admin/users/pending');
            console.log(`✅ Pending approvals accessible: ${pendingResponse.data.users?.length || 0} users`);
            
            if (pendingResponse.data.users && pendingResponse.data.users.length > 0) {
              console.log('\n   Available users for approval:');
              pendingResponse.data.users.slice(0, 3).forEach(user => {
                console.log(`   - ${user.name} (${user.userType}) - ${user.email}`);
              });
            }
          } catch (error) {
            console.log(`❌ Admin endpoints failed: ${error.response?.data?.message || error.message}`);
          }
        }
        
        console.log('\n🎉 Admin account setup complete!');
        console.log(`   You can now login with:`);
        console.log(`   Email: ${adminData.email}`);
        console.log(`   Password: ${adminData.password}`);
        
      } else {
        console.log(`❌ Admin login failed: ${loginResponse.data.error || 'Unknown error'}`);
      }
      
    } else {
      console.log(`❌ Admin account creation failed: ${signupResponse.data.error || 'Unknown error'}`);
    }

  } catch (error) {
    console.error('❌ Admin creation failed:', error.response?.data?.message || error.message);
    if (error.response?.data?.message?.includes('already exists')) {
      console.log('\nℹ️  Admin account already exists. Try using:');
      console.log('   Email: yadmin@ycs.com');
      console.log('   Password: YSCAdmin2024!');
    }
  }
}

createAdmin();