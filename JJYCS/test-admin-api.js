const axios = require('axios');

const api = axios.create({
  baseURL: 'http://localhost:8081/api',
  timeout: 10000
});

async function testAdminAPI() {
  try {
    console.log('🔍 Testing admin API functionality...\n');

    // First, create some test users (corporate and partner)
    console.log('1. Creating test users for approval testing...');
    
    const corporateUserData = {
      name: 'LG Electronics',
      email: 'lg@lgcorp.com',
      password: 'Password123!',
      phone: '02-3777-1114',
      userType: 'CORPORATE',
      companyName: 'LG Electronics Inc.',
      businessNumber: '107-86-15806',
      companyAddress: 'Seoul Digital City, South Korea'
    };

    const partnerUserData = {
      name: 'Incheon Logistics Partner',
      email: 'partner@incheon.co.kr', 
      password: 'Password123!',
      phone: '032-123-4567',
      userType: 'PARTNER',
      companyName: 'Incheon Logistics Co.',
      businessNumber: '789-88-54321',
      partnerRegion: 'Incheon'
    };

    let corporateUserId = null;
    let partnerUserId = null;

    // Create corporate user
    try {
      const corporateResponse = await api.post('/auth/signup', corporateUserData);
      if (corporateResponse.data.success) {
        corporateUserId = corporateResponse.data.user.id;
        console.log(`✅ Corporate user created: ${corporateResponse.data.user.name} (ID: ${corporateUserId})`);
        console.log(`   Status: ${corporateResponse.data.user.status}`);
        console.log(`   Pending Approval: ${corporateResponse.data.user.pendingApproval}`);
      }
    } catch (createError) {
      console.log('ℹ️  Corporate user might already exist, continuing...');
    }

    // Create partner user
    try {
      const partnerResponse = await api.post('/auth/signup', partnerUserData);
      if (partnerResponse.data.success) {
        partnerUserId = partnerResponse.data.user.id;
        console.log(`✅ Partner user created: ${partnerResponse.data.user.name} (ID: ${partnerUserId})`);
        console.log(`   Status: ${partnerResponse.data.user.status}`);
        console.log(`   Pending Approval: ${partnerResponse.data.user.pendingApproval}`);
      }
    } catch (createError) {
      console.log('ℹ️  Partner user might already exist, continuing...');
    }

    // Test getting pending approvals
    console.log('\n2. Getting pending approvals...');
    try {
      const pendingResponse = await api.get('/admin/users/pending');
      console.log(`✅ Pending approvals found: ${pendingResponse.data.users?.length || 0}`);
      
      if (pendingResponse.data.users && pendingResponse.data.users.length > 0) {
        pendingResponse.data.users.forEach(user => {
          console.log(`   - ${user.name} (${user.userType}) - ${user.email}`);
        });
      }
    } catch (error) {
      console.log(`❌ Failed to get pending approvals: ${error.response?.data?.message || error.message}`);
    }

    // Test getting all users
    console.log('\n3. Getting all users...');
    try {
      const allUsersResponse = await api.get('/admin/users');
      console.log(`✅ Total users found: ${allUsersResponse.data.users?.length || 0}`);
      
      if (allUsersResponse.data.users) {
        const userCounts = allUsersResponse.data.users.reduce((acc, user) => {
          acc[user.userType] = (acc[user.userType] || 0) + 1;
          return acc;
        }, {});
        console.log('   User counts by type:', userCounts);
      }
    } catch (error) {
      console.log(`❌ Failed to get all users: ${error.response?.data?.message || error.message}`);
    }

    // Test system stats
    console.log('\n4. Getting system stats...');
    try {
      const statsResponse = await api.get('/admin/stats');
      console.log('✅ System stats retrieved:');
      console.log('   Total Users:', statsResponse.data.totalUsers || 'N/A');
      console.log('   Pending Approvals:', statsResponse.data.pendingApprovals || 'N/A');
      console.log('   Active Orders:', statsResponse.data.activeOrders || 'N/A');
    } catch (error) {
      console.log(`❌ Failed to get system stats: ${error.response?.data?.message || error.message}`);
    }

    // Test approval/rejection if we have users to test with
    if (corporateUserId || partnerUserId) {
      console.log('\n5. Testing user approval/rejection...');
      
      const testUserId = corporateUserId || partnerUserId;
      const testUserName = corporateUserId ? 'LG Electronics' : 'Incheon Logistics Partner';
      
      // Test approval
      try {
        const approvalResponse = await api.post(`/admin/users/${testUserId}/approve`, {
          notes: 'Test approval via API testing'
        });
        
        if (approvalResponse.data.success) {
          console.log(`✅ User approved: ${testUserName}`);
        } else {
          console.log(`❌ Approval failed: ${approvalResponse.data.message}`);
        }
      } catch (error) {
        console.log(`❌ Approval error: ${error.response?.data?.message || error.message}`);
      }
    }

  } catch (error) {
    console.error('❌ Test failed:', error.message);
  }
}

testAdminAPI();