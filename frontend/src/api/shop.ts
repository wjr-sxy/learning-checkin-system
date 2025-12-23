import request from '../utils/request'

export const getProducts = () => {
  return request.get('/shop/products')
}

export const getOwnedProducts = (userId: number) => {
  return request.get('/shop/owned', { params: { userId } })
}

export const exchangeProduct = (userId: number, productId: number, shippingInfo?: any) => {
  return request.post('/shop/exchange', { userId, productId, ...shippingInfo })
}

export const getExchangeHistory = (userId: number) => {
  return request.get('/shop/history', { params: { userId } })
}

export const refundProduct = (orderId: number) => {
  return request.post(`/shop/refund/${orderId}`)
}
