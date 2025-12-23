import request from '../utils/request'

export const getPointsLog = (userId: number) => {
  return request({
    url: '/points/log',
    method: 'get',
    params: { userId }
  })
}

export const getPointsTrend = (userId: number, days: number = 7) => {
  return request({
    url: '/points/trend',
    method: 'get',
    params: { userId, days }
  })
}
