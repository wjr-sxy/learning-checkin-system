import axios from 'axios'

const request = axios.create({
  baseURL: '/api',
  timeout: 5000
})

request.interceptors.request.use(config => {
  // Use sessionStorage for session isolation
  const token = sessionStorage.getItem('active_token')
  if (token) {
    config.headers['Authorization'] = `Bearer ${token}`
  }
  return config
}, error => {
  return Promise.reject(error)
})

request.interceptors.response.use(response => {
  // If it's a blob, return the response directly
  if (response.config.responseType === 'blob') {
    return response
  }

  const res = response.data
  // Backend result structure: { code: 200, message: "Success", data: ... }
  if (res.code && res.code !== 200) {
    // Handle 401 Unauthorized specifically
    if (res.code === 401) {
      sessionStorage.removeItem('active_token')
      localStorage.removeItem('token') // Cleanup legacy
      // Do NOT clear 'user' from localStorage if we are not using it there, 
      // but original code did. Let's stick to session clear.
      window.location.href = '/login'
    }
    return Promise.reject(new Error(res.message || 'Error'))
  }
  return res
}, error => {
  if (error.response && error.response.status === 401) {
    sessionStorage.removeItem('active_token')
    localStorage.removeItem('token')
    window.location.href = '/login'
  }
  return Promise.reject(error)
})

export default request
