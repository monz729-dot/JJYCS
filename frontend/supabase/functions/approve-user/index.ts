// Edge Function: 승인/거절 처리
import { serve } from "https://deno.land/std/http/server.ts";
import { createClient } from "https://esm.sh/@supabase/supabase-js@2";

serve(async (req) => {
  try {
    if (req.method !== "POST") return new Response("Method Not Allowed", { status: 405 });

    const { user_id, approved } = await req.json();
    if (!user_id || typeof approved !== "boolean") {
      return new Response("user_id(boolean approved) required", { status: 400 });
    }

    const supabase = createClient(
      Deno.env.get("SUPABASE_URL")!,
      Deno.env.get("SUPABASE_SERVICE_ROLE_KEY")! // 서버 전용 키
    );

    const { error } = await supabase
      .from("user_profiles")
      .update({
        approval_status: approved ? "approved" : "rejected",
      })
      .eq("id", user_id);

    if (error) throw error;

    return new Response(JSON.stringify({ ok: true }), { headers: { "Content-Type": "application/json" } });
  } catch (e) {
    return new Response(String(e), { status: 500 });
  }
});