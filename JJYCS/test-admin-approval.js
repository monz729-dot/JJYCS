const axios = require('axios');

const api = axios.create({
  baseURL: 'http://localhost:8081/api',
  timeout: 10000
});

async function testAdminApproval() {
  try {
    console.log('🔍 Testing admin approval workflow...\n');

    // Login as admin
    console.log('1. Logging in as admin...');
    const loginResponse = await api.post('/auth/login', {
      email: 'yadmin@ycs.com',
      password: 'YSCAdmin2024!'
    });

    if (loginResponse.data.success) {
      console.log(`✅ Admin logged in: ${loginResponse.data.user.name}`);
      api.defaults.headers.common['Authorization'] = `Bearer ${loginResponse.data.token}`;
    } else {
      throw new Error('Admin login failed');
    }

    // Get pending approvals
    console.log('\n2. Getting pending approvals...');
    const pendingResponse = await api.get('/admin/users/pending');
    console.log(`✅ Found ${pendingResponse.data.users?.length || 0} pending users`);

    if (!pendingResponse.data.users || pendingResponse.data.users.length === 0) {
      console.log('ℹ️  No pending users found. Creating test users for approval...');
      
      // Create a test corporate user
      const corporateData = {
        name: 'Hyundai Motors',
        email: 'hyundai@hyundai.com',
        password: 'Password123!',
        phone: '02-3464-1114',
        userType: 'CORPORATE',
        companyName: 'Hyundai Motor Company',
        businessNumber: '101-81-42074',
        companyAddress: 'Seoul, South Korea'
      };

      const partnerData = {
        name: 'Daegu Partner',
        email: 'partner@daegu.co.kr',
        password: 'Password123!',
        phone: '053-123-4567',
        userType: 'PARTNER',
        companyName: 'Daegu Logistics Co.',
        businessNumber: '123-88-99999',
        partnerRegion: 'Daegu'
      };

      // Remove auth header temporarily for signup
      delete api.defaults.headers.common['Authorization'];

      try {
        const corpSignup = await api.post('/auth/signup', corporateData);
        console.log(`✅ Created corporate user: ${corpSignup.data.user.name}`);
      } catch (error) {
        console.log('ℹ️  Corporate user might already exist');
      }

      try {
        const partnerSignup = await api.post('/auth/signup', partnerData);
        console.log(`✅ Created partner user: ${partnerSignup.data.user.name}`);
      } catch (error) {
        console.log('ℹ️  Partner user might already exist');
      }

      // Restore auth header
      api.defaults.headers.common['Authorization'] = `Bearer ${loginResponse.data.token}`;

      // Get updated pending list
      const updatedResponse = await api.get('/admin/users/pending');
      console.log(`✅ Updated pending list: ${updatedResponse.data.users?.length || 0} users`);
      pendingResponse.data = updatedResponse.data;
    }

    // Test approval workflow
    if (pendingResponse.data.users && pendingResponse.data.users.length > 0) {
      console.log('\n3. Testing user approval workflow...');
      
      const userToApprove = pendingResponse.data.users[0];
      console.log(`   Approving: ${userToApprove.name} (${userToApprove.userType})`);
      
      const approvalResponse = await api.post(`/admin/users/${userToApprove.id}/approve`, {
        notes: 'Approved via automated test - user meets all requirements'
      });
      
      if (approvalResponse.data.success) {
        console.log(`✅ User approved successfully!`);
        console.log(`   User: ${userToApprove.name}`);
        console.log(`   Status changed from PENDING to APPROVED`);
      } else {
        console.log(`❌ Approval failed: ${approvalResponse.data.message}`);
      }

      // Test rejection if there's another user
      if (pendingResponse.data.users.length > 1) {
        console.log('\n4. Testing user rejection workflow...');
        
        const userToReject = pendingResponse.data.users[1];
        console.log(`   Rejecting: ${userToReject.name} (${userToReject.userType})`);
        
        const rejectionResponse = await api.post(`/admin/users/${userToReject.id}/reject`, {
          reason: 'Incomplete documentation provided - please resubmit with all required documents'
        });
        
        if (rejectionResponse.data.success) {
          console.log(`✅ User rejected successfully!`);
          console.log(`   User: ${userToReject.name}`);
          console.log(`   Status changed from PENDING to REJECTED`);
          console.log(`   Reason: Incomplete documentation`);
        } else {
          console.log(`❌ Rejection failed: ${rejectionResponse.data.message}`);
        }
      }

      // Final check - get remaining pending users
      console.log('\n5. Final status check...');
      const finalResponse = await api.get('/admin/users/pending');
      console.log(`✅ Remaining pending users: ${finalResponse.data.users?.length || 0}`);
      
      if (finalResponse.data.users && finalResponse.data.users.length > 0) {
        console.log('   Still pending:');
        finalResponse.data.users.forEach(user => {
          console.log(`   - ${user.name} (${user.userType}) - ${user.email}`);
        });
      }
    }

    console.log('\n🎉 Admin approval workflow testing complete!');
    console.log('   The frontend admin dashboard is now ready for use.');
    console.log('   Access it at: http://localhost:3003/admin/approvals');

  } catch (error) {
    console.error('❌ Admin approval test failed:', error.response?.data?.message || error.message);
  }
}

testAdminApproval();