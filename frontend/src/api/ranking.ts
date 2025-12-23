import request from '../utils/request'

export const getDailyRanking = () => {
    return request({
        url: '/ranking/daily',
        method: 'get'
    })
}

export const getWeeklyRanking = () => {
    return request({
        url: '/ranking/weekly',
        method: 'get'
    })
}
