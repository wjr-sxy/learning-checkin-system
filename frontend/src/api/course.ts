import request from '../utils/request'

export const createCourse = (teacherId: number, name: string, description: string, semester: string) => {
    return request({
        url: '/course/create',
        method: 'post',
        data: { teacherId, name, description, semester }
    })
}

export const getTeacherCourses = (teacherId: number) => {
    return request({
        url: `/course/teacher/${teacherId}`,
        method: 'get'
    })
}

export const getStudentCourses = (studentId: number) => {
    return request({
        url: `/course/student/${studentId}`,
        method: 'get'
    })
}

export const getCourseStudents = (courseId: number) => {
    return request({
        url: `/course/${courseId}/students`,
        method: 'get'
    })
}

export const getCourseStudentDetails = (courseId: number) => {
    return request({
        url: `/course/${courseId}/students/details`,
        method: 'get'
    })
}

export const removeStudent = (courseId: number, studentId: number) => {
    return request({
        url: `/course/${courseId}/remove`,
        method: 'post',
        data: { studentId }
    })
}

export const banStudent = (courseId: number, studentId: number) => {
    return request({
        url: `/course/${courseId}/ban`,
        method: 'post',
        data: { studentId }
    })
}

export const unbanStudent = (courseId: number, studentId: number) => {
    return request({
        url: `/course/${courseId}/unban`,
        method: 'post',
        data: { studentId }
    })
}

export const checkCourseName = (teacherId: number, name: string) => {
    return request({
        url: '/course/check-name',
        method: 'get',
        params: { teacherId, name }
    })
}

export const refreshCourseCode = (courseId: number) => {
    return request({
        url: `/course/${courseId}/refresh-code`,
        method: 'post'
    })
}

export const remindStudents = (courseId: number) => {
    return request({
        url: `/course/${courseId}/remind`,
        method: 'post'
    })
}

export const checkStudentsToRemind = (courseId: number) => {
    return request({
        url: `/course/${courseId}/remind/check`,
        method: 'post'
    })
}

export const sendBatchReminders = (courseId: number, subject: string, content: string, studentIds: number[]) => {
    return request({
        url: `/course/${courseId}/remind/send`,
        method: 'post',
        data: { subject, content, studentIds }
    })
}

export const joinCourse = (studentId: number, code: string) => {
    return request({
        url: '/course/join',
        method: 'post',
        data: { studentId, code }
    })
}
