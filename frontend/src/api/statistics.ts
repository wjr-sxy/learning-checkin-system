import request from '../utils/request'

export const getDailyStats = (courseId: number, date?: string) => {
    return request({
        url: '/statistics/daily',
        method: 'get',
        params: { courseId, date }
    })
}

export const getCompletionTrend = (courseId: number, days: number = 30) => {
    return request({
        url: '/statistics/trend',
        method: 'get',
        params: { courseId, days }
    })
}

export const getStudentPortrait = (courseId: number, studentId: number) => {
    return request({
        url: '/statistics/student-portrait',
        method: 'get',
        params: { courseId, studentId }
    })
}

export const getLeaderboard = (courseId?: number, type: string = 'points', limit: number = 10) => {
    return request({
        url: '/statistics/leaderboard',
        method: 'get',
        params: { courseId, type, limit }
    })
}
