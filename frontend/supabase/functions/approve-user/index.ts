import { serve } from "https://deno.land/std@0.168.0/http/server.ts"
import { createClient } from 'https://esm.sh/@supabase/supabase-js@2'

const corsHeaders = {
  'Access-Control-Allow-Origin': '*',
  'Access-Control-Allow-Headers': 'authorization, x-client-info, apikey, content-type',
}

serve(async (req) => {
  // CORS 처리
  if (req.method === 'OPTIONS') {
    return new Response('ok', { headers: corsHeaders })
  }

  try {
    // Supabase 클라이언트 생성
    const supabaseUrl = Deno.env.get('SUPABASE_URL')
    const supabaseServiceKey = Deno.env.get('SUPABASE_SERVICE_ROLE_KEY')
    
    if (!supabaseUrl || !supabaseServiceKey) {
      throw new Error('Missing environment variables')
    }

    const supabase = createClient(supabaseUrl, supabaseServiceKey)

    // 요청 데이터 파싱
    const { userId, action, reason } = await req.json()
    
    if (!userId || !action) {
      return new Response(
        JSON.stringify({ error: 'Missing required fields' }),
        { 
          status: 400, 
          headers: { ...corsHeaders, 'Content-Type': 'application/json' } 
        }
      )
    }

    // 승인 상태 업데이트
    const newStatus = action === 'approve' ? 'approved' : 'rejected'
    
    const { data, error } = await supabase
      .from('user_profiles')
      .update({ 
        approval_status: newStatus,
        updated_at: new Date().toISOString()
      })
      .eq('id', userId)
      .select()
      .single()

    if (error) {
      throw new Error(error.message)
    }

    // 승인 완료 시 이메일 발송 (실제 구현에서는 이메일 서비스 연동)
    if (action === 'approve') {
      // 이메일 발송 로직
      console.log(`User ${userId} approved. Sending approval email.`)
    } else {
      // 거절 시 이메일 발송
      console.log(`User ${userId} rejected. Sending rejection email.`)
    }

    return new Response(
      JSON.stringify({ 
        success: true, 
        message: `User ${action === 'approve' ? 'approved' : 'rejected'} successfully`,
        data 
      }),
      { 
        status: 200, 
        headers: { ...corsHeaders, 'Content-Type': 'application/json' } 
      }
    )

  } catch (error) {
    return new Response(
      JSON.stringify({ error: error.message }),
      { 
        status: 500, 
        headers: { ...corsHeaders, 'Content-Type': 'application/json' } 
      }
    )
  }
})
