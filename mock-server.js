const express = require('express');
const cors = require('cors');
const sqlite3 = require('sqlite3').verbose();
const path = require('path');

const app = express();
const port = 8081;

app.use(cors());
app.use(express.json());

// SQLite 데이터베이스 연결
const dbPath = path.join(__dirname, 'database', 'ycs_lms.db');
const db = new sqlite3.Database(dbPath, (err) => {
    if (err) {
        console.error('❌ 데이터베이스 연결 실패:', err.message);
    } else {
        console.log('✅ SQLite 데이터베이스 연결 성공');
    }
});

// Dashboard stats endpoint - real database data
app.get('/api/admin/dashboard/stats', (req, res) => {
  const queries = {
    totalUsers: "SELECT COUNT(*) as count FROM users",
    totalOrders: "SELECT COUNT(*) as count FROM orders",
    totalRevenue: "SELECT COALESCE(SUM(amount), 0) as total FROM payments WHERE payment_status = 'completed'",
    completedDeliveries: "SELECT COUNT(*) as count FROM orders WHERE status = 'delivered'"
  };
  
  const stats = {};
  let completed = 0;
  const total = Object.keys(queries).length;
  
  Object.entries(queries).forEach(([key, query]) => {
    db.get(query, [], (err, row) => {
      if (err) {
        console.error(`Database error for ${key}:`, err.message);
        stats[key] = 0;
      } else {
        stats[key] = row.count || row.total || 0;
      }
      
      completed++;
      if (completed === total) {
        // 성장률은 고정값으로 설정 (실제로는 이전 기간 데이터와 비교)
        stats.userGrowthRate = 100.0;
        stats.orderGrowthRate = 50.0;
        stats.revenueGrowthRate = 25.0;
        stats.deliveryGrowthRate = 15.0;
        
        res.json({
          success: true,
          data: stats
        });
      }
    });
  });
});

// Order status distribution endpoint - real database data
app.get('/api/admin/dashboard/order-status', (req, res) => {
  const query = `
    SELECT 
      status,
      COUNT(*) as count,
      ROUND(COUNT(*) * 100.0 / (SELECT COUNT(*) FROM orders), 1) as percentage
    FROM orders 
    GROUP BY status
    ORDER BY count DESC
  `;
  
  db.all(query, [], (err, rows) => {
    if (err) {
      console.error('Database error for order status:', err.message);
      res.status(500).json({
        success: false,
        message: 'Database error'
      });
    } else {
      res.json({
        success: true,
        data: rows
      });
    }
  });
});

// Recent orders endpoint - real database data
app.get('/api/admin/dashboard/recent-orders', (req, res) => {
  const query = `
    SELECT 
      o.id,
      o.order_number as orderNumber,
      u.name as customerName,
      o.status,
      o.actual_amount as totalAmount,
      o.created_at as createdAt
    FROM orders o
    JOIN users u ON o.user_id = u.id
    ORDER BY o.created_at DESC
    LIMIT 5
  `;
  
  db.all(query, [], (err, rows) => {
    if (err) {
      console.error('Database error for recent orders:', err.message);
      res.status(500).json({
        success: false,
        message: 'Database error'
      });
    } else {
      res.json({
        success: true,
        data: rows
      });
    }
  });
});

// Recent users endpoint - real database data
app.get('/api/admin/dashboard/recent-users', (req, res) => {
  const query = `
    SELECT 
      id,
      name,
      email,
      user_type as role,
      status
    FROM users
    ORDER BY created_at DESC
    LIMIT 5
  `;
  
  db.all(query, [], (err, rows) => {
    if (err) {
      console.error('Database error for recent users:', err.message);
      res.status(500).json({
        success: false,
        message: 'Database error'
      });
    } else {
      res.json({
        success: true,
        data: rows
      });
    }
  });
});

// System status endpoint
app.get('/api/admin/dashboard/system-status', (req, res) => {
  const systemStatus = [
    { name: "데이터베이스", status: "online" },
    { name: "Redis", status: "online" },
    { name: "API 서버", status: "online" },
    { name: "창고 시스템", status: "online" }
  ];
  
  res.json({
    success: true,
    data: systemStatus
  });
});

// User dashboard endpoints
app.get('/api/dashboard/stats', (req, res) => {
  // Get user email from header (in real app, from JWT token)
  const userEmail = req.headers['x-user-email'] || req.query.userEmail || 'general@test.com';
  
  // First get user ID from email
  db.get("SELECT id FROM users WHERE email = ?", [userEmail], (err, user) => {
    if (err || !user) {
      console.error(`User not found for email ${userEmail}:`, err?.message);
      res.status(404).json({
        success: false,
        message: 'User not found'
      });
      return;
    }
    
    const userId = user.id;
  
  const queries = {
    userOrders: `SELECT COUNT(*) as count FROM orders WHERE user_id = ?`,
    pendingOrders: `SELECT COUNT(*) as count FROM orders WHERE user_id = ? AND status = 'pending'`,
    shippedOrders: `SELECT COUNT(*) as count FROM orders WHERE user_id = ? AND status = 'shipped'`,
    deliveredOrders: `SELECT COUNT(*) as count FROM orders WHERE user_id = ? AND status = 'delivered'`
  };
  
  const stats = {};
  let completed = 0;
  const total = Object.keys(queries).length;
  
  Object.entries(queries).forEach(([key, query]) => {
    db.get(query, [userId], (err, row) => {
      if (err) {
        console.error(`Database error for ${key}:`, err.message);
        stats[key] = 0;
      } else {
        stats[key] = row.count || 0;
      }
      
      completed++;
      if (completed === total) {
        res.json({
          success: true,
          data: {
            newOrders: stats.pendingOrders,
            pending: stats.pendingOrders,
            shipped: stats.shippedOrders,
            delivered: stats.deliveredOrders
          }
        });
      }
    });
  });
  });
});

app.get('/api/orders', (req, res) => {
  // Get user email from header (in real app, from JWT token)
  const userEmail = req.headers['x-user-email'] || req.query.userEmail || 'general@test.com';
  
  // First get user ID from email
  db.get("SELECT id FROM users WHERE email = ?", [userEmail], (err, user) => {
    if (err || !user) {
      console.error(`User not found for email ${userEmail}:`, err?.message);
      res.status(404).json({
        success: false,
        message: 'User not found'
      });
      return;
    }
    
    const userId = user.id;
    
    // Get recent orders for this specific user
    const query = `
      SELECT 
        o.id,
        o.order_number as orderNo,
        u.name as customer,
        o.status,
        o.actual_amount as amount,
        DATE(o.created_at) as date
      FROM orders o
      JOIN users u ON o.user_id = u.id
      WHERE o.user_id = ?
      ORDER BY o.created_at DESC
      LIMIT 5
    `;
    
    db.all(query, [userId], (err, rows) => {
      if (err) {
        console.error('Database error for user orders:', err.message);
        res.status(500).json({
          success: false,
          message: 'Database error'
        });
      } else {
        res.json({
          success: true,
          data: rows
        });
      }
    });
  });
});

app.get('/api/activities', (req, res) => {
  // Get recent activities from audit logs
  const query = `
    SELECT 
      id,
      action as title,
      'Recent activity' as description,
      created_at as time
    FROM audit_logs
    ORDER BY created_at DESC
    LIMIT 4
  `;
  
  db.all(query, [], (err, rows) => {
    if (err) {
      console.error('Database error for activities:', err.message);
      res.json({
        success: true,
        data: []
      });
    } else {
      // Format activities with icons and colors
      const activities = rows.map((row, index) => ({
        id: row.id,
        title: getActivityTitle(row.title),
        description: getActivityDescription(row.title),
        time: formatTimeAgo(row.time),
        icon: getActivityIcon(row.title),
        iconBg: getActivityIconBg(index),
        iconColor: getActivityIconColor(index)
      }));
      
      res.json({
        success: true,
        data: activities
      });
    }
  });
});

// Helper functions for activities
function getActivityTitle(action) {
  const titles = {
    'USER_LOGIN': '사용자 로그인',
    'SCAN_EVENT': '상품 스캔',
    'ORDER_CREATE': '새 주문 접수',
    'PAYMENT_COMPLETE': '결제 완료',
    'USER_APPROVE': '사용자 승인'
  };
  return titles[action] || action;
}

function getActivityDescription(action) {
  const descriptions = {
    'USER_LOGIN': '시스템에 로그인했습니다',
    'SCAN_EVENT': '창고에서 상품을 스캔했습니다',
    'ORDER_CREATE': '새로운 주문이 접수되었습니다',
    'PAYMENT_COMPLETE': '결제가 완료되었습니다',
    'USER_APPROVE': '관리자가 승인했습니다'
  };
  return descriptions[action] || 'Recent activity';
}

function getActivityIcon(action) {
  const icons = {
    'USER_LOGIN': 'mdi-login',
    'SCAN_EVENT': 'mdi-qrcode-scan',
    'ORDER_CREATE': 'mdi-package-variant',
    'PAYMENT_COMPLETE': 'mdi-credit-card',
    'USER_APPROVE': 'mdi-check-circle'
  };
  return icons[action] || 'mdi-information';
}

function getActivityIconBg(index) {
  const colors = ['bg-blue-50', 'bg-green-50', 'bg-yellow-50', 'bg-purple-50'];
  return colors[index % colors.length];
}

function getActivityIconColor(index) {
  const colors = ['text-blue-600', 'text-green-600', 'text-yellow-600', 'text-purple-600'];
  return colors[index % colors.length];
}

function formatTimeAgo(timestamp) {
  const now = new Date();
  const time = new Date(timestamp);
  const diffInMinutes = Math.floor((now - time) / (1000 * 60));
  
  if (diffInMinutes < 60) {
    return `${diffInMinutes}분 전`;
  } else if (diffInMinutes < 1440) {
    return `${Math.floor(diffInMinutes / 60)}시간 전`;
  } else {
    return `${Math.floor(diffInMinutes / 1440)}일 전`;
  }
}

// Admin orders endpoint
app.get('/api/admin/orders', (req, res) => {
  const query = `
    SELECT 
      o.id,
      o.order_number as orderNumber,
      u.name as customerName,
      u.email as customerEmail,
      o.order_type as orderType,
      o.status,
      o.actual_amount as totalAmount,
      o.estimated_amount as estimatedAmount,
      o.total_items as itemCount,
      o.total_cbm_m3 as totalCbm,
      o.requires_extra_recipient as requiresExtraRecipient,
      u.member_code as memberCode,
      o.created_at as createdAt
    FROM orders o
    JOIN users u ON o.user_id = u.id
    ORDER BY o.created_at DESC
  `;
  
  db.all(query, [], (err, rows) => {
    if (err) {
      console.error('Database error for admin orders:', err.message);
      res.status(500).json({
        success: false,
        message: 'Database error'
      });
    } else {
      // Format the data for frontend
      const formattedOrders = rows.map(row => ({
        ...row,
        totalAmount: `₩${Number(row.totalAmount || 0).toLocaleString()}`,
        estimatedCost: `₩${Number(row.estimatedAmount || 0).toLocaleString()}`,
        totalCbm: Number(row.totalCbm || 0).toFixed(1),
        requiresExtraRecipient: Boolean(row.requiresExtraRecipient),
        createdAt: row.createdAt.split(' ')[0] // Extract date part
      }));
      
      res.json({
        success: true,
        data: formattedOrders
      });
    }
  });
});

// Admin order stats endpoint
app.get('/api/admin/orders/stats', (req, res) => {
  const queries = {
    total: "SELECT COUNT(*) as count FROM orders",
    pending: "SELECT COUNT(*) as count FROM orders WHERE status = 'pending'",
    shipped: "SELECT COUNT(*) as count FROM orders WHERE status = 'shipped'",
    delivered: "SELECT COUNT(*) as count FROM orders WHERE status = 'delivered'"
  };
  
  const stats = {};
  let completed = 0;
  const total = Object.keys(queries).length;
  
  Object.entries(queries).forEach(([key, query]) => {
    db.get(query, [], (err, row) => {
      if (err) {
        console.error(`Database error for admin order stats ${key}:`, err.message);
        stats[key] = 0;
      } else {
        stats[key] = row.count || 0;
      }
      
      completed++;
      if (completed === total) {
        res.json({
          success: true,
          data: [
            {
              title: '총 주문',
              value: stats.total.toLocaleString(),
              description: '전체 기간',
              icon: 'ShoppingBagIcon',
              iconColor: 'text-blue-600'
            },
            {
              title: '대기 주문',
              value: stats.pending.toLocaleString(),
              description: '처리 필요',
              icon: 'ClockIcon',
              iconColor: 'text-yellow-600'
            },
            {
              title: '배송 중',
              value: stats.shipped.toLocaleString(),
              description: '진행 중',
              icon: 'TruckIcon',
              iconColor: 'text-purple-600'
            },
            {
              title: '완료',
              value: stats.delivered.toLocaleString(),
              description: '배송 완료',
              icon: 'CheckCircleIcon',
              iconColor: 'text-green-600'
            }
          ]
        });
      }
    });
  });
});

// Warehouse dashboard endpoint
app.get('/api/warehouse/stats', (req, res) => {
  const queries = {
    totalInventory: "SELECT COUNT(*) as count FROM inventory",
    inboundCompleted: "SELECT COUNT(*) as count FROM inventory WHERE status = 'inbound_completed'",
    readyForOutbound: "SELECT COUNT(*) as count FROM inventory WHERE status = 'ready_for_outbound'",
    outboundCompleted: "SELECT COUNT(*) as count FROM inventory WHERE status = 'outbound_completed'",
    scanEvents: "SELECT COUNT(*) as count FROM scan_events WHERE DATE(created_at) = DATE('now')"
  };
  
  const stats = {};
  let completed = 0;
  const total = Object.keys(queries).length;
  
  Object.entries(queries).forEach(([key, query]) => {
    db.get(query, [], (err, row) => {
      if (err) {
        console.error(`Database error for warehouse stats ${key}:`, err.message);
        stats[key] = 0;
      } else {
        stats[key] = row.count || 0;
      }
      
      completed++;
      if (completed === total) {
        res.json({
          success: true,
          data: {
            totalInventory: stats.totalInventory,
            inboundCompleted: stats.inboundCompleted,
            readyForOutbound: stats.readyForOutbound,
            outboundCompleted: stats.outboundCompleted,
            todayScans: stats.scanEvents
          }
        });
      }
    });
  });
});

// Warehouse inventory endpoint
app.get('/api/warehouse/inventory', (req, res) => {
  const query = `
    SELECT 
      i.id,
      i.label_code as labelCode,
      o.order_number as orderNumber,
      u.name as customerName,
      i.status,
      i.location_code as locationCode,
      i.scan_count as scanCount,
      i.created_at as createdAt,
      w.name as warehouseName
    FROM inventory i
    JOIN orders o ON i.order_id = o.id
    JOIN users u ON o.user_id = u.id
    JOIN warehouses w ON i.warehouse_id = w.id
    ORDER BY i.created_at DESC
    LIMIT 20
  `;
  
  db.all(query, [], (err, rows) => {
    if (err) {
      console.error('Database error for warehouse inventory:', err.message);
      res.status(500).json({
        success: false,
        message: 'Database error'
      });
    } else {
      res.json({
        success: true,
        data: rows
      });
    }
  });
});

// Partner dashboard endpoint
app.get('/api/partner/stats', (req, res) => {
  // Get user email from header (in real app, from JWT token)
  const userEmail = req.headers['x-user-email'] || req.query.userEmail || 'partner@test.com';
  
  // First get user ID from email
  db.get("SELECT id FROM users WHERE email = ? AND user_type = 'partner'", [userEmail], (err, user) => {
    if (err || !user) {
      console.error(`Partner not found for email ${userEmail}:`, err?.message);
      res.status(404).json({
        success: false,
        message: 'Partner not found'
      });
      return;
    }
    
    const partnerId = user.id;
  
  const queries = {
    totalReferrals: `SELECT COUNT(*) as count FROM partner_referrals WHERE partner_id = ?`,
    paidCommissions: `SELECT COALESCE(SUM(commission_amount), 0) as total FROM partner_referrals WHERE partner_id = ? AND status = 'paid'`,
    pendingCommissions: `SELECT COALESCE(SUM(commission_amount), 0) as total FROM partner_referrals WHERE partner_id = ? AND status = 'pending'`
  };
  
  const stats = {};
  let completed = 0;
  const total = Object.keys(queries).length;
  
  Object.entries(queries).forEach(([key, query]) => {
    db.get(query, [partnerId], (err, row) => {
      if (err) {
        console.error(`Database error for partner stats ${key}:`, err.message);
        stats[key] = 0;
      } else {
        stats[key] = row.count || row.total || 0;
      }
      
      completed++;
      if (completed === total) {
        res.json({
          success: true,
          data: {
            totalReferrals: stats.totalReferrals,
            paidCommissions: stats.paidCommissions,
            pendingCommissions: stats.pendingCommissions,
            totalCommissions: stats.paidCommissions + stats.pendingCommissions
          }
        });
      }
    });
  });
  });
});

// 추가 API 엔드포인트들
app.get('/api/estimates', (req, res) => {
  res.json({
    success: true,
    data: []
  });
});

app.get('/api/payments', (req, res) => {
  res.json({
    success: true,
    data: []
  });
});

app.get('/api/tracking', (req, res) => {
  res.json({
    success: true,
    data: []
  });
});

app.get('/api/notifications', (req, res) => {
  res.json({
    success: true,
    data: []
  });
});

app.get('/api/profile', (req, res) => {
  const userEmail = req.headers['x-user-email'] || 'general@test.com';
  
  db.get("SELECT * FROM users WHERE email = ?", [userEmail], (err, user) => {
    if (err || !user) {
      res.status(404).json({
        success: false,
        message: 'User not found'
      });
      return;
    }
    
    res.json({
      success: true,
      data: user
    });
  });
});

// Admin users management endpoint
app.get('/api/admin/users', (req, res) => {
  const query = `
    SELECT 
      id,
      username,
      name,
      email,
      user_type,
      status,
      created_at,
      updated_at,
      phone,
      member_code
    FROM users
    ORDER BY created_at DESC
  `;
  
  db.all(query, [], (err, rows) => {
    if (err) {
      console.error('Database error for admin users:', err.message);
      res.status(500).json({
        success: false,
        message: 'Database error'
      });
    } else {
      // Format the data for frontend
      const formattedUsers = rows.map(user => ({
        ...user,
        lastLogin: null, // 실제 데이터베이스에 last_login_at 컬럼이 없음
        joinDate: user.created_at.split(' ')[0],
        approval_status: user.status === 'active' ? 'approved' : 'pending',
        statusBadge: getStatusBadge(user.status, user.status === 'active' ? 'approved' : 'pending', true)
      }));
      
      res.json({
        success: true,
        data: formattedUsers
      });
    }
  });
});

// Helper function for status badge
function getStatusBadge(status, approvalStatus, emailVerified) {
  if (!emailVerified) {
    return { type: 'warning', text: '이메일 미인증' };
  }
  if (approvalStatus === 'pending') {
    return { type: 'info', text: '승인 대기' };
  }
  if (approvalStatus === 'rejected') {
    return { type: 'danger', text: '승인 거절' };
  }
  if (status === 'active' && approvalStatus === 'approved') {
    return { type: 'success', text: '활성' };
  }
  if (status === 'inactive') {
    return { type: 'secondary', text: '비활성' };
  }
  return { type: 'secondary', text: status };
}

// Admin user stats endpoint
app.get('/api/admin/users/stats', (req, res) => {
  const queries = {
    total: "SELECT COUNT(*) as count FROM users",
    active: "SELECT COUNT(*) as count FROM users WHERE status = 'active'",
    pending: "SELECT COUNT(*) as count FROM users WHERE status != 'active'",
    newToday: "SELECT COUNT(*) as count FROM users WHERE DATE(created_at) = DATE('now')"
  };
  
  const stats = {};
  let completed = 0;
  const total = Object.keys(queries).length;
  
  Object.entries(queries).forEach(([key, query]) => {
    db.get(query, [], (err, row) => {
      if (err) {
        console.error(`Database error for user stats ${key}:`, err.message);
        stats[key] = 0;
      } else {
        stats[key] = row.count || 0;
      }
      
      completed++;
      if (completed === total) {
        res.json({
          success: true,
          data: [
            {
              title: '총 사용자',
              value: stats.total.toLocaleString(),
              description: '전체 등록자',
              icon: 'UsersIcon',
              iconColor: 'text-blue-600'
            },
            {
              title: '활성 사용자',
              value: stats.active.toLocaleString(),
              description: '활성 상태',
              icon: 'CheckCircleIcon',
              iconColor: 'text-green-600'
            },
            {
              title: '승인 대기',
              value: stats.pending.toLocaleString(),
              description: '처리 필요',
              icon: 'ClockIcon',
              iconColor: 'text-yellow-600'
            },
            {
              title: '오늘 가입',
              value: stats.newToday.toLocaleString(),
              description: '신규 등록자',
              icon: 'UserPlusIcon',
              iconColor: 'text-purple-600'
            }
          ]
        });
      }
    });
  });
});

// Admin user detail endpoint
app.get('/api/admin/users/:id', (req, res) => {
  const userId = req.params.id;
  
  const query = `
    SELECT 
      u.*,
      ep.company_name,
      ep.business_number,
      ep.representative,
      ep.address as company_address,
      pp.commission_rate,
      pp.referral_code,
      pp.bank_account
    FROM users u
    LEFT JOIN enterprise_profiles ep ON u.id = ep.user_id
    LEFT JOIN partner_profiles pp ON u.id = pp.user_id
    WHERE u.id = ?
  `;
  
  db.get(query, [userId], (err, user) => {
    if (err) {
      console.error('Database error for user detail:', err.message);
      res.status(500).json({
        success: false,
        message: 'Database error'
      });
    } else if (!user) {
      res.status(404).json({
        success: false,
        message: 'User not found'
      });
    } else {
      res.json({
        success: true,
        data: user
      });
    }
  });
});

// 기본 404 핸들러 (모든 누락된 API에 대한 폴백)
app.use((req, res, next) => {
  if (req.path.startsWith('/api/')) {
    console.log(`❌ 404 - Missing API endpoint: ${req.method} ${req.path}`);
    res.status(404).json({
      success: false,
      message: `API endpoint not found: ${req.method} ${req.path}`,
      timestamp: new Date().toISOString()
    });
  } else {
    next();
  }
});

app.listen(port, () => {
  console.log(`✅ Mock API server running at http://localhost:${port}`);
  console.log(`📊 Available APIs:`);
  console.log(`   Admin Dashboard:`);
  console.log(`   - GET /api/admin/dashboard/stats`);
  console.log(`   - GET /api/admin/dashboard/order-status`);
  console.log(`   - GET /api/admin/dashboard/recent-orders`);
  console.log(`   - GET /api/admin/dashboard/recent-users`);
  console.log(`   - GET /api/admin/dashboard/system-status`);
  console.log(`   Admin Orders:`);
  console.log(`   - GET /api/admin/orders`);
  console.log(`   - GET /api/admin/orders/stats`);
  console.log(`   User Dashboard:`);
  console.log(`   - GET /api/dashboard/stats`);
  console.log(`   - GET /api/orders`);
  console.log(`   - GET /api/activities`);
  console.log(`   Warehouse:`);
  console.log(`   - GET /api/warehouse/stats`);
  console.log(`   - GET /api/warehouse/inventory`);
  console.log(`   Partner:`);
  console.log(`   - GET /api/partner/stats`);
  console.log(`   🛡️  404 Handler: All other /api/* requests`);
});