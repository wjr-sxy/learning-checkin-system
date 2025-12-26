import request from '../utils/request'

// Product Management
export const getAdminProducts = (params?: any) => {
  return request.get('/admin/shop/list', { params })
}

export const saveProduct = (data: any) => {
  if (data.id) {
    return request.put('/admin/shop/update', data)
  } else {
    return request.post('/admin/shop/add', data)
  }
}

export const deleteProduct = (id: number) => {
  return request.delete(`/admin/shop/delete/${id}`)
}

export const uploadProductImage = (file: File) => {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/admin/shop/upload', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

// Order Management
export const getOrders = (params: any) => {
  return request.get('/admin/shop/orders', { params })
}

export const shipOrder = (orderId: number, trackingNumber: string) => {
  return request.post(`/admin/shop/orders/${orderId}/ship`, { trackingNumber })
}

export const refundOrder = (orderId: number) => {
  return request.post(`/admin/shop/orders/${orderId}/refund`)
}

export const markOrderAbnormal = (orderId: number) => {
  return request.post(`/admin/shop/orders/${orderId}/abnormal`)
}

export const cancelOrderAbnormal = (orderId: number) => {
  return request.post(`/admin/shop/orders/${orderId}/cancel-abnormal`)
}
