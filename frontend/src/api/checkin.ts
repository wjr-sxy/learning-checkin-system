import request from '../utils/request'

export const getCheckinStatus = (userId: number) => {
  return request.get('/checkin/status', { params: { userId } })
}

export const dailyCheckin = (userId: number) => {
  return request.post('/checkin/daily', { userId })
}

export const reCheckin = (userId: number, date: string) => {
  return request.post('/checkin/recheckin', { userId, date })
}

export const getCheckinHistory = (userId: number) => {
  return request.get('/checkin/history', { params: { userId } })
}
