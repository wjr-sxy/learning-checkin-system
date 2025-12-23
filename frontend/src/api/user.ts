import request from '../utils/request'

export const updateProfile = (data: any) => {
    return request({
        url: '/user/profile',
        method: 'put',
        data
    })
}

export const updatePassword = (data: any) => {
    return request({
        url: '/user/password',
        method: 'put',
        data
    })
}

export const getLoginLogs = (userId: number) => {
    return request({
        url: '/user/login-logs',
        method: 'get',
        params: { userId }
    })
}

export const getOperationLogs = (params: any) => {
    return request({
        url: '/user/operation-logs',
        method: 'get',
        params
    })
}

export const equipDecoration = (data: any) => {
    return request({
        url: '/user/equip',
        method: 'post',
        data
    })
}
