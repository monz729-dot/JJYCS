import axios from 'axios'

const API_BASE_URL = import.meta.env.VITE_API_BASE || '/api'

export interface AdminUser {
  id: string
  name: string
  email: string
  phone?: string
  role: 'enterprise' | 'partner' | 'warehouse' | 'individual'
  approvalStatus: 'pending' | 'approved' | 'rejected'
  registeredAt: string
  approvedAt?: string
  approvedBy?: string
  approvalNotes?: string
  enterpriseProfile?: {
    companyName: string
    businessNumber: string
    businessAddress: string
  }
  partnerProfile?: {
    partnerType: 'affiliate' | 'referral' | 'corporate'
    businessLicense: string
  }
  warehouseProfile?: {
    warehouseName: string
    location: string
    capacity: number
  }
}

export interface ApprovalRequest {
  reason?: string
}

export const adminApi = {
  // Get pending users
  async getPendingUsers(): Promise<{ success: boolean; data: AdminUser[]; message: string }> {
    try {
      const response = await axios.get(`${API_BASE_URL}/admin/users/pending`)
      return response.data
    } catch (error: any) {
      return {
        success: false,
        data: [],
        message: error.response?.data?.message || 'Failed to fetch pending users'
      }
    }
  },

  // Approve user
  async approveUser(userId: string): Promise<{ success: boolean; message: string; userId: string }> {
    try {
      const response = await axios.post(`${API_BASE_URL}/admin/users/${userId}/approve`)
      return response.data
    } catch (error: any) {
      return {
        success: false,
        message: error.response?.data?.message || 'Failed to approve user',
        userId
      }
    }
  },

  // Reject user
  async rejectUser(userId: string, request: ApprovalRequest): Promise<{ success: boolean; message: string; userId: string }> {
    try {
      const response = await axios.post(`${API_BASE_URL}/admin/users/${userId}/reject`, request)
      return response.data
    } catch (error: any) {
      return {
        success: false,
        message: error.response?.data?.message || 'Failed to reject user',
        userId
      }
    }
  },

  // Get registration stats
  async getRegistrationStats(): Promise<{ 
    success: boolean; 
    stats: any; 
    roleBreakdown: any; 
    averageApprovalTime: string 
  }> {
    try {
      const response = await axios.get(`${API_BASE_URL}/admin/stats/registrations`)
      return response.data
    } catch (error: any) {
      return {
        success: false,
        stats: {},
        roleBreakdown: {},
        averageApprovalTime: '0일'
      }
    }
  }
}

export default adminApi