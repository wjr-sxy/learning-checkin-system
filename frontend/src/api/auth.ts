import request from '../utils/request'

export const getUserInfo = () => {
  return request.get('/auth/me')
}

export const login = (data: any) => {
  return request.post('/auth/login', data)
}

export const register = (data: any) => {
  return request.post('/auth/register', data)
}
