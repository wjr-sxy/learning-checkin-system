import request from '../utils/request'

export function sendHeartbeat(data: { userId: number, duration: number }) {
  return request({
    url: '/stats/online/heartbeat',
    method: 'post',
    data
  })
}

export function getUserOnlineStats(userId: number) {
  return request({
    url: '/stats/online/user',
    method: 'get',
    params: { userId }
  })
}

export function getOnlineLeaderboard(type: string = 'month') {
  return request({
    url: '/stats/online/leaderboard',
    method: 'get',
    params: { type }
  })
}

export function getPlatformOnlineStats() {
  return request({
    url: '/stats/online/platform',
    method: 'get'
  })
}

export function getOnlineUsersList() {
  return request({
    url: '/stats/online/list',
    method: 'get'
  })
}
