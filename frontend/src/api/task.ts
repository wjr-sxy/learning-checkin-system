import request from '../utils/request'

export interface Task {
  id?: number
  teacherId?: number
  courseId?: number
  title: string
  content: string
  attachmentUrl?: string
  deadline: string
  rewardPoints: number
  submitType: string // TEXT, FILE, IMAGE
  status: number
  isRecurring: boolean
  startDate?: string
  endDate?: string
  makeupCount?: number
  makeupCostPercent?: number
  createTime?: string
}

export interface TaskSubmission {
  id: number
  taskId: number
  studentId: number
  content: string
  fileUrls: string
  status: number // 0: Pending, 1: Graded, 2: Returned
  score: number
  rating: number
  comment: string
  submitTime: string
  gradeTime?: string
}

export const deleteTask = (id: number) => {
  return request({
    url: `/tasks/${id}`,
    method: 'delete'
  })
}

export const getTask = (id: number) => {
  return request({
    url: `/tasks/${id}`,
    method: 'get'
  })
}

export const createTask = (data: Task) => {
  return request({
    url: '/tasks/create',
    method: 'post',
    data
  })
}

export const getTaskLeaderboard = (id: number) => {
  return request({
    url: `/tasks/${id}/leaderboard`,
    method: 'get'
  })
}

export const getTaskStats = (id: number) => {
  return request({
    url: `/tasks/${id}/stats`,
    method: 'get'
  })
}

export const getStudentTasks = () => {
  return request({
    url: '/tasks/student',
    method: 'get'
  })
}

export const getTeacherTasks = () => {
  return request({
    url: '/tasks/teacher',
    method: 'get'
  })
}

export const submitTask = (id: number, data: { content: string, fileUrls: string }) => {
  return request({
    url: `/tasks/${id}/submit`,
    method: 'post',
    data
  })
}

export const getSubmissions = (id: number, params?: any) => {
  return request({
    url: `/tasks/${id}/submissions`,
    method: 'get',
    params
  })
}

export const gradeSubmission = (id: number, data: { score: number, rating: number, comment: string }) => {
  return request({
    url: `/tasks/submissions/${id}/grade`,
    method: 'post',
    data
  })
}

export const returnSubmission = (id: number, comment: string) => {
  return request({
    url: `/tasks/submissions/${id}/return`,
    method: 'post',
    data: { comment }
  })
}

export const batchPass = (ids: number[]) => {
  return request({
    url: '/tasks/submissions/batch-pass',
    method: 'post',
    data: ids
  })
}

export const batchReturn = (ids: number[], comment: string) => {
  return request({
    url: '/tasks/submissions/batch-return',
    method: 'post',
    data: { ids, comment }
  })
}

// Templates
export const getTemplates = (teacherId: number) => {
  return request({
    url: '/grading/templates',
    method: 'get',
    params: { teacherId }
  })
}

export const createTemplate = (data: any) => {
  return request({
    url: '/grading/templates',
    method: 'post',
    data
  })
}

export const deleteTemplate = (id: number) => {
  return request({
    url: `/grading/templates/${id}`,
    method: 'delete'
  })
}

export const useTemplate = (id: number) => {
  return request({
    url: `/grading/templates/${id}/use`,
    method: 'post'
  })
}
