import request from '../utils/request'

export const getDashboardStats = () => {
  return request.get('/admin/stats/dashboard')
}

// User Management
export const getUsers = (params: any) => {
  return request.get('/admin/users', { params })
}

export const exportUsers = (username?: string) => {
  return request.get('/admin/users/export', {
    params: { username },
    responseType: 'blob'
  })
}

export const updateUserStatus = (id: number, status: number) => {
  return request.post(`/admin/users/${id}/status`, { status })
}

export const resetUserPassword = (id: number, password: string) => {
  return request.post(`/admin/users/${id}/password`, { password })
}

export const adjustUserPoints = (id: number, data: any) => {
  return request.post(`/admin/users/${id}/points`, data)
}

// Points Bank
export const getPointsStats = () => {
  return request.get('/admin/points/stats')
}

export const getPointsRules = () => {
  return request.get('/admin/points/rules')
}

export const savePointsRule = (rule: any) => {
  return request.post('/admin/points/rules', rule)
}

// System Tools
export const getAnnouncements = (params: any) => {
  return request.get('/admin/system/announcements', { params })
}

export const saveAnnouncement = (data: any) => {
  return request.post('/admin/system/announcements', data)
}

export const deleteAnnouncement = (id: number) => {
  return request.delete(`/admin/system/announcements/${id}`)
}

export const getBlacklist = (params: any) => {
  return request.get('/admin/system/blacklist', { params })
}

export const addBlacklist = (data: any) => {
  return request.post('/admin/system/blacklist', data)
}

export const deleteBlacklist = (id: number) => {
  return request.delete(`/admin/system/blacklist/${id}`)
}

// Logs
export const getOperationLogs = (params: any) => {
  return request.get('/admin/system/logs/operation', { params })
}

export const exportOperationLogs = () => {
  return request.get('/admin/system/logs/operation/export', {
    responseType: 'blob'
  })
}

export const getLoginLogs = (params: any) => {
  return request.get('/admin/system/logs/login', { params })
}

export const exportLoginLogs = () => {
  return request.get('/admin/system/logs/login/export', {
    responseType: 'blob'
  })
}

// Sensitive Words
export const importSensitiveWords = (file: File) => {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/admin/content/sensitive/import', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

export const exportSensitiveWords = () => {
  return request.get('/admin/content/sensitive/export', {
    responseType: 'blob'
  })
}

export const getSensitiveLogs = (params: any) => {
  return request.get('/admin/content/sensitive/logs', { params })
}

export const getSystemHealth = () => {
  return request.get('/admin/system/health')
}

export const saveProduct = (product: any) => {
  return request.post('/shop/product', product)
}

export const deleteProduct = (id: number) => {
  return request.delete(`/shop/product/${id}`)
}

export const shipOrder = (id: number, trackingNumber: string) => {
  return request.post(`/admin/shop/orders/${id}/ship`, { trackingNumber })
}

// Order Audit
export const getOrders = (params: any) => {
  return request.get('/admin/shop/orders', { params })
}

export const refundOrder = (id: number) => {
  return request.post(`/admin/shop/orders/${id}/refund`)
}

export const markOrderAbnormal = (id: number) => {
  return request.post(`/admin/shop/orders/${id}/abnormal`)
}

export const cancelOrderAbnormal = (id: number) => {
  return request.post(`/admin/shop/orders/${id}/cancel-abnormal`)
}

export const getPointsMultiplier = () => {
  return request.get('/admin/points/multiplier')
}

export const setPointsMultiplier = (multiplier: number) => {
  return request.post('/admin/points/multiplier', { multiplier })
}

