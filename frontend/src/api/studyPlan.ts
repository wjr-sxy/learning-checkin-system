import request from '../utils/request'

export const getUserPlans = (userId: number) => {
  return request.get('/study-plan', { params: { userId } })
}

export const createPlan = (plan: any) => {
  return request.post('/study-plan', plan)
}

export const updatePlan = (plan: any) => {
  return request.put('/study-plan', plan)
}

export const deletePlan = (id: number) => {
  return request.delete(`/study-plan/${id}`)
}

export const completePlan = (id: number) => {
  return request.put(`/study-plan/${id}/complete`)
}

export const updateProgress = (id: number, data: { completedTasks: number, totalTasks: number, note: string }) => {
  return request.put(`/study-plan/${id}/progress`, data)
}

export const getProgressHistory = (id: number) => {
  return request.get(`/study-plan/${id}/history`)
}

export const getPlanTasks = (planId: number) => {
  return request.get(`/study-plan/${planId}/tasks`)
}

export const addTask = (planId: number, task: any) => {
  return request.post(`/study-plan/${planId}/tasks`, task)
}

export const deleteTask = (taskId: number) => {
  return request.delete(`/study-plan/tasks/${taskId}`)
}

export const updateTaskStatus = (taskId: number, status: number) => {
  return request.put(`/study-plan/tasks/${taskId}/status`, { status })
}

export const getDailyPoints = (userId: number) => {
  return request.get('/study-plan/daily-points', { params: { userId } })
}
