import request from '../utils/request'

export const searchUsers = (keyword: string) => {
    return request.get('/friend/search', { params: { keyword } })
}

export const sendFriendRequest = (targetUserId: number) => {
    return request.post('/friend/request', { targetUserId })
}

export const handleFriendRequest = (requestId: number, status: number) => {
    return request.post('/friend/handle', { requestId, status })
}

export const getFriendList = () => {
    return request.get('/friend/list')
}

export const getFriendRequests = () => {
    return request.get('/friend/requests')
}

export const deleteFriend = (friendId: number) => {
    return request.delete(`/friend/${friendId}`)
}
