import request from '../utils/request'

export const exportCourseData = (courseId: number) => {
    // Because it's a file download, we might handle it differently or use window.open if no auth headers needed (but we need token usually)
    // Using axios with blob response type
    return request({
        url: `/export/course/${courseId}`,
        method: 'get',
        responseType: 'blob'
    })
}
