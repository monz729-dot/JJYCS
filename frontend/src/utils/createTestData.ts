import { supabase } from '@/lib/supabase'

// Supabase에 테스트 데이터를 생성하는 함수
export async function createTestData() {
  console.log('🚀 테스트 데이터 생성 시작...')

  try {
    // 1. 테스트 사용자 프로필 생성
    const testUsers = [
      {
        id: '00000000-0000-0000-0000-000000000001',
        username: 'general_user',
        name: '김일반',
        email: 'general@test.com',
        phone: '010-1111-1111',
        user_type: 'general',
        approval_status: 'approved',
        email_verified: true,
        terms_agreed: true,
        privacy_agreed: true,
        address: '서울시 강남구 테헤란로 123',
        created_at: '2024-01-01T00:00:00Z',
        updated_at: '2024-01-01T00:00:00Z'
      },
      {
        id: '00000000-0000-0000-0000-000000000002',
        username: 'corporate_user',
        name: '이기업',
        email: 'corporate@test.com',
        phone: '010-2222-2222',
        user_type: 'corporate',
        company_name: '테스트기업(주)',
        business_number: '123-45-67890',
        manager_name: '김대리',
        manager_contact: '010-2222-3333',
        approval_status: 'approved',
        email_verified: true,
        terms_agreed: true,
        privacy_agreed: true,
        address: '서울시 서초구 강남대로 456',
        created_at: '2024-01-01T00:00:00Z',
        updated_at: '2024-01-01T00:00:00Z'
      },
      {
        id: '00000000-0000-0000-0000-000000000003',
        username: 'partner_user',
        name: '박파트너',
        email: 'partner@test.com',
        phone: '010-3333-3333',
        user_type: 'partner',
        company_name: '파트너물류(주)',
        business_number: '234-56-78901',
        manager_name: '박과장',
        manager_contact: '010-3333-4444',
        approval_status: 'approved',
        email_verified: true,
        terms_agreed: true,
        privacy_agreed: true,
        address: '서울시 마포구 월드컵로 789',
        created_at: '2024-01-01T00:00:00Z',
        updated_at: '2024-01-01T00:00:00Z'
      },
      {
        id: '00000000-0000-0000-0000-000000000004',
        username: 'warehouse_user',
        name: '최창고',
        email: 'warehouse@test.com',
        phone: '010-4444-4444',
        user_type: 'warehouse',
        approval_status: 'approved',
        email_verified: true,
        terms_agreed: true,
        privacy_agreed: true,
        address: '인천시 중구 공항로 101 물류센터',
        created_at: '2024-01-01T00:00:00Z',
        updated_at: '2024-01-01T00:00:00Z'
      },
      {
        id: '00000000-0000-0000-0000-000000000005',
        username: 'admin_user',
        name: '관리자',
        email: 'admin@test.com',
        phone: '010-5555-5555',
        user_type: 'admin',
        approval_status: 'approved',
        email_verified: true,
        terms_agreed: true,
        privacy_agreed: true,
        address: '서울시 중구 세종대로 110',
        created_at: '2024-01-01T00:00:00Z',
        updated_at: '2024-01-01T00:00:00Z'
      }
    ]

    // 기존 테스트 사용자 확인
    const { data: existingUsers } = await supabase
      .from('user_profiles')
      .select('id')
      .in('email', testUsers.map(u => u.email))

    if (!existingUsers || existingUsers.length === 0) {
      console.log('📝 테스트 사용자 생성 중...')
      const { error: userError } = await supabase
        .from('user_profiles')
        .insert(testUsers)

      if (userError) {
        console.error('❌ 사용자 생성 실패:', userError)
      } else {
        
      }
    } else {
      
    }

    // 2. 테스트 주문 데이터 생성
    const testOrders = [
      {
        id: '10000000-0000-0000-0000-000000000001',
        user_id: '00000000-0000-0000-0000-000000000001',
        order_number: 'TEST-GEN-001',
        status: 'pending',
        order_type: 'air',
        recipient_name: '김수취',
        recipient_phone: '010-1234-5678',
        recipient_address: '태국 방콕 수쿰빗 로드 123',
        total_amount: 150000,
        requires_repack: false,
        requires_extra_recipient: false,
        shipping_notes: '조심히 다뤄주세요',
        member_code: 'GEN001',
        tracking_number: 'TH240815001',
        created_at: '2024-01-15T10:00:00Z',
        updated_at: '2024-01-15T10:00:00Z'
      },
      {
        id: '10000000-0000-0000-0000-000000000002',
        user_id: '00000000-0000-0000-0000-000000000001',
        order_number: 'TEST-GEN-002',
        status: 'confirmed',
        order_type: 'sea',
        recipient_name: '이수취',
        recipient_phone: '010-2345-6789',
        recipient_address: '태국 치앙마이 님만해민 로드 456',
        total_amount: 280000,
        requires_repack: true,
        requires_extra_recipient: false,
        shipping_notes: '리패킹 요청',
        member_code: 'GEN001',
        tracking_number: 'TH240810002',
        created_at: '2024-01-10T14:30:00Z',
        updated_at: '2024-01-12T09:15:00Z'
      },
      {
        id: '10000000-0000-0000-0000-000000000003',
        user_id: '00000000-0000-0000-0000-000000000002',
        order_number: 'TEST-COR-001',
        status: 'shipped',
        order_type: 'air',
        recipient_name: '박수취',
        recipient_phone: '010-3456-7890',
        recipient_address: '태국 푸켓 파통 비치 로드 789',
        total_amount: 520000,
        requires_repack: false,
        requires_extra_recipient: true,
        shipping_notes: '긴급 배송',
        member_code: 'COR001',
        tracking_number: 'TH240812003',
        created_at: '2024-01-12T16:45:00Z',
        updated_at: '2024-01-13T11:20:00Z'
      },
      {
        id: '10000000-0000-0000-0000-000000000004',
        user_id: '00000000-0000-0000-0000-000000000002',
        order_number: 'TEST-COR-002',
        status: 'processing',
        order_type: 'sea',
        recipient_name: '최수취',
        recipient_phone: '010-4567-8901',
        recipient_address: '태국 파타야 비치 로드 101',
        total_amount: 750000,
        requires_repack: true,
        requires_extra_recipient: true,
        shipping_notes: '대량 주문',
        member_code: 'COR001',
        tracking_number: 'TH240813004',
        created_at: '2024-01-13T13:00:00Z',
        updated_at: '2024-01-14T08:30:00Z'
      },
      {
        id: '10000000-0000-0000-0000-000000000005',
        user_id: '00000000-0000-0000-0000-000000000001',
        order_number: 'TEST-REF-001',
        status: 'delivered',
        order_type: 'air',
        recipient_name: '정수취',
        recipient_phone: '010-5678-9012',
        recipient_address: '태국 코사무이 차웽 비치 202',
        total_amount: 320000,
        requires_repack: false,
        requires_extra_recipient: false,
        shipping_notes: '파트너 추천',
        member_code: 'REF001',
        tracking_number: 'TH240805005',
        created_at: '2024-01-05T11:15:00Z',
        updated_at: '2024-01-08T16:45:00Z'
      },
      {
        id: '10000000-0000-0000-0000-000000000006',
        user_id: '00000000-0000-0000-0000-000000000001',
        order_number: 'TEST-DEL-001',
        status: 'delayed',
        order_type: 'air',
        recipient_name: '한수취',
        recipient_phone: '010-6789-0123',
        recipient_address: '태국 후아힌 비치 로드 303',
        total_amount: 95000,
        requires_repack: false,
        requires_extra_recipient: false,
        shipping_notes: '회원코드 미기재로 지연',
        member_code: null,
        tracking_number: null,
        created_at: '2024-01-14T17:30:00Z',
        updated_at: '2024-01-15T09:00:00Z'
      }
    ]

    // 기존 테스트 주문 확인
    const { data: existingOrders } = await supabase
      .from('orders')
      .select('id')
      .in('order_number', testOrders.map(o => o.order_number))

    if (!existingOrders || existingOrders.length === 0) {
      console.log('📦 테스트 주문 생성 중...')
      const { error: orderError } = await supabase
        .from('orders')
        .insert(testOrders)

      if (orderError) {
        console.error('❌ 주문 생성 실패:', orderError)
      } else {
        
      }
    } else {
      
    }

    console.log('🎉 테스트 데이터 생성 완료!')
    return { success: true }

  } catch (error) {
    console.error('❌ 테스트 데이터 생성 실패:', error)
    return { success: false, error }
  }
}

// 개발자 콘솔에서 호출할 수 있도록 window 객체에 추가
if (typeof window !== 'undefined') {
  (window as any).createTestData = createTestData
}