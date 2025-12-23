import request from '../utils/request'

export const getCoursePlans = (courseId: number, creatorId?: number) => {
    return request({
        url: `/study-plan/course/${courseId}`,
        method: 'get',
        params: { creatorId }
    })
}

export const createPlan = (data: any) => {
    return request({
        url: '/study-plan',
        method: 'post',
        data
    })
}

export const updatePlan = (data: any) => {
    return request({
        url: '/study-plan',
        method: 'put',
        data
    })
}

export const deletePlan = (id: number) => {
    return request({
        url: `/study-plan/${id}`,
        method: 'delete'
    })
}

export const distributePlan = (planId: number, courseId: number) => {
    return request({
        url: `/study-plan/${planId}/distribute`,
        method: 'post',
        data: { courseId }
    })
}

export const addTask = (planId: number, content: string) => {
    return request({
        url: `/study-plan/${planId}/tasks`,
        method: 'post',
        data: { content }
    })
}

export const getPlanTasks = (planId: number) => {
    return request({
        url: `/study-plan/${planId}/tasks`,
        method: 'get'
    })
}

export const deleteTask = (taskId: number) => {
    return request({
        url: `/study-plan/tasks/${taskId}`,
        method: 'delete'
    })
}
